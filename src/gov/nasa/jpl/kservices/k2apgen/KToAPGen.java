package gov.nasa.jpl.kservices.k2apgen;

import java.io.File;
import java.lang.reflect.TypeVariable;
import java.time.Duration;
import gov.nasa.jpl.ae.event.*;
import gov.nasa.jpl.ae.util.ClassData;
import gov.nasa.jpl.ae.util.JavaToConstraintExpression;
import gov.nasa.jpl.ae.xml.EventXmlToJava;
import gov.nasa.jpl.kservices.KtoJava;
import gov.nasa.jpl.mbee.util.*;
import japa.parser.ast.body.ConstructorDeclaration;
//import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import k.frontend.*;
import scala.Option;
import scala.collection.JavaConversions;

import java.util.*;
import java.util.List;
import java.util.Collection;

import static com.sun.jmx.snmp.ThreadContext.contains;

/**
 * Translates a K model and partial solution to APGen input.
 *
 * APGen has activities and resources but no classes.  Classes will be
 * translated into activities whose start and end times are ignored.
 * Names of nested classes will translate to qualified names to avoid
 * name clashes.
 *
 * A TimeVaryingMap will translate to an APGen resource.  Effects will
 * be translated to APGen set and use effects.
 *
 * Dependencies and constraints of the form, x = ..., will translate to
 * assignment statements in APGen.
 *
 * Other constraints involving TimeVaryingMaps will translate to a
 * resource/constraint pairs.  The resource will be defined to capture the
 * the boolean evaluateion of the K constraint.  The APGen constraint will
 * check that the resource is always true.
 *
 * Other constraints will not be translated directly.  Instead, the resulting
 * values for variables in the solution will be captured in parameter values
 * or created variables.
 *
 */
public class KToAPGen {
//    /**
//     * The k model.
//     */
//    String k = null;
    /**
     * APGen activity and resource declaration output.
     */
    String aaf = null;
    /**
     * APGen activity and resource instance output.
     */
    String apf = null;

    String aafFileName = null;
    String apfFileName = null;

    KtoJava ktoJava = null;
    Model model = null;
    APGenModel apgenModel = new APGenModel();
    public boolean addingToDecomposition = false;

    public Map<String, List<Exp>> constructorCalls = new LinkedHashMap<String, List<Exp>>();

    public String getAafFileName() {
        if ( !Utils.isNullOrEmpty( aafFileName) ) return aafFileName;
        setApgenFileNames( !Utils.isNullOrEmpty(apfFileName) );
        return aafFileName;
    }
    public String getApfFileName() {
        if ( !Utils.isNullOrEmpty( apfFileName) ) return apfFileName;
        setApgenFileNames( !Utils.isNullOrEmpty(aafFileName) );
        return apfFileName;
    }
    public String setApgenFileNames(boolean onlyIfNull) {
        if ( onlyIfNull && Utils.isNullOrEmpty( aafFileName ) && Utils.isNullOrEmpty( aafFileName ) ) return aafFileName;
        if ( ktoJava == null ) return null;
        if ( Utils.isNullOrEmpty( ktoJava.kFiles ) ) return null;
        File kFile = null;
        String kFileName = null;
        for ( String f : ktoJava.kFiles ) {
            if ( !Utils.isNullOrEmpty( f ) ) {
                File kF = new File( f );
                if ( kF.exists() ) {
                    kFileName = f;
                    kFile = kF;
                    break;
                } else if ( kF == null ) {
                    kFileName = kF.getName();
                    kFile = kF;
                }
            }
        }
        if ( Utils.isNullOrEmpty( kFileName ) ) return null;
        if ( !onlyIfNull || aafFileName == null ) {
            aafFileName = kFileName.trim().replaceAll("[.]k$", "") + ".aaf";
        }
        if ( !onlyIfNull || apfFileName == null ) {
            apfFileName = kFileName.trim().replaceAll("[.]k$", "") + ".apf";
        }
        return aafFileName;
    }
    public boolean writeApgenFiles() {
        boolean succ = false;
        if ( aaf != null ) {
            succ = FileUtils.stringToFile( aaf, getAafFileName() );
        }
        if ( apf != null ) {
            boolean s = FileUtils.stringToFile( apf, getApfFileName() );
            succ = succ && s;
        }
        return succ;
    }

    public static void main(String[] args) {
        KtoJava kToJava = KtoJava.runMain(args);

        boolean crazy = false;
        if ( crazy ) {
            String apgen = translate(kToJava);
            System.out.println(apgen);
            return;
        }

        KToAPGen k2apgen = new KToAPGen();
        k2apgen.ktoJava = kToJava;
        k2apgen.model = kToJava.model();
        k2apgen.translate();
    }

    public void translate() {
        apgenModel = translate(getModel());
        addInheritedMembers(getModel());
        cleanupForConstructors();
        aaf = apgenModel.toString();
        apf = translateInstances(ktoJava);

        System.out.println("\n=========   k apgenModel   =========\n" + aaf);
        System.out.println("=========   end k apgenModel   =========\n");
        System.out.println("\n=========   apgen instances   =========\n" + apf);
        System.out.println("=========   end apgen instances   =========\n");

        writeApgenFiles();
    }

    protected Map<String, List<Exp>> getConstructorCalls() {
        if ( !constructorCalls.isEmpty() ) return constructorCalls;
        HasChildren h = getModel();
        return gatherConstructorCalls(h);
    }
    protected Map<String, List<Exp>> gatherConstructorCalls(HasChildren h) {
        if (h instanceof Exp && isConstructorAppl((Exp)h)) {
            String className = null;
            if ( h instanceof CtorApplExp )  {
                className = ((CtorApplExp) h).name();
            } else if ( h instanceof FunApplExp ) {
                FunApplExp fae = (FunApplExp) h;
                className = fae.name();
            }
            List<Exp> ctors = constructorCalls.get(className);
            if ( ctors == null ) {
                ctors = new ArrayList<Exp>();
                constructorCalls.put(className, ctors);
            }
            ctors.add( (Exp)h );
        }
        Collection<Object> children = JavaConversions.asJavaCollection(h.children());
        for ( Object c : children ) {
            if ( c instanceof HasChildren ) {
                gatherConstructorCalls((HasChildren)c);
            }
        }
        return constructorCalls;
    }

    protected void cleanupForConstructors() {
        // TODO -- sepaate creation parameters from constructor parameters
    }

    protected void addInheritedMembers(Model model) {
        Map<String, Set<String> > childClasses = new TreeMap<>();
        Map<String, Set<String> > superClasses = new TreeMap<>();
        Collection<EntityDecl> entityDecls = JavaConversions.asJavaCollection( model.allEntityDecls(model) );
        for ( EntityDecl e: entityDecls ) {
            Collection<Type> supers = JavaConversions.asJavaCollection(e.extending());
            String eName = e.ident();
            for ( Type t : supers ) {
                String name = null;
                if ( t instanceof IdentType ) {
                    Collection<String> names = JavaConversions.asJavaCollection(((IdentType)t).ident().names());
                    name = Utils.asList(names).get(names.size()-1);
                } else if ( t instanceof ClassType ) {
                    Collection<String> names = JavaConversions.asJavaCollection(((ClassType)t).ident().names());
                    name = Utils.asList(names).get(names.size()-1);
                }
                if ( !Utils.isNullOrEmpty(name) ) {
                    Utils.add(childClasses, name, eName);
                    Utils.add(superClasses, eName, name);
                }
            }
        }
        // Need to process from top of class hierarchy down so tha the parent already has its inherited members.
        // Get the top level superclasses (from childClasses) and walk down the children.
        HashSet<String> seen = new LinkedHashSet<String>();
        List<String> queue = new ArrayList<String>();
        // Find top-level classes and add to queue.
        for ( String parent : childClasses.keySet() ) {
            if ( !superClasses.containsKey(parent) ) {
                queue.add(parent);
            }
        }
        // Now walk tree recursively with queue.
        while ( !queue.isEmpty() ) {
            String c = queue.remove(0);
            if ( seen.contains(c) ) continue;
            seen.add(c);
            Set<String> parents = superClasses.get(c);
            if (parents != null) {
                for ( String p: parents ) {
                    addInheritedMembersTo(p, c);
                }
            }
            Set<String> children = childClasses.get(c);
            if ( children != null ) {
                queue.addAll(children);
            }
        }
    }

    private void addInheritedMembersTo( String parent, String child ) {
        Activity parentAct = apgenModel.activities.get(parent);
        Activity childAct = apgenModel.activities.get(child);
        if ( parentAct == null || childAct == null ) return;
        LinkedHashMap<String, Parameter> newParameters
                = new LinkedHashMap<String, Parameter>(parentAct.parameters);
        newParameters.putAll(childAct.parameters);
        childAct.parameters = newParameters;
    }

    public APGenModel translate(Model model) {
        // Preprocess top level to find constraints that could be merged with property declarations.
        unitePropertiesAndConstraints( model ); // ok to call multiple times for same parent

        // TODO -- model.packageName() -- use package as prefix of on names of contained elements?
        Collection<PackageDecl> packages = JavaConversions.asJavaCollection( model.packages() );
        for ( PackageDecl p: packages ) {
            translate(p.model());
        }
//        Collection<EntityDecl> entityDecls = JavaConversions.asJavaCollection( model.entityDecls(model) );
//        for ( EntityDecl e: entityDecls ) {
//            translate(e, apgenModel);
//        }
        Collection<TopDecl> decls = JavaConversions.asJavaCollection( model.decls() );
        for ( TopDecl d : decls ) {
            translate(d);
        }
        Collection<AnnotationDecl> annotations = JavaConversions.asJavaCollection( model.annotations() );
        for ( AnnotationDecl a: annotations ) {
            translate(a, apgenModel);
        }
        // TODO -- model.imports();
        return apgenModel;
    }

    protected void translate(EntityDecl e, Object parent) {
        Activity activity = new Activity();
        activity.entityName = e.ident();
        activity.name = kToApgenClassName(e.ident());

        // Check and make sure this isn't a state mode or a resource
        if ( apgenModel.parameters.containsKey( activity.name ) ) {
            return;
        }
        if ( apgenModel.resources.containsKey( activity.name ) ) {
            return;
        }

        // Preprocess entity to find constraints that could be merged with property declarations.
        unitePropertiesAndConstraints( e ); // ok to call multiple times for same parent

        activity.parentScope = parent;
        String pattern = "^" + ktoJava.globalName + "[._]";
        String nameWithScope = classNameWithScope(e.ident()).replaceFirst(pattern, "");
        ;
        activity.attributes.put("Description", "activity for class " + nameWithScope);
        activity.attributes.put("Legend", activity.name);
        activity.attributes.put("Color", "Orange");

        Collection<Annotation> annotations = JavaConversions.asJavaCollection( e.annotations() );
        for ( Annotation a: annotations ) {
            translate(a, activity);
        }
        Collection<MemberDecl> members = JavaConversions.asJavaCollection( e.members() );
        for ( MemberDecl member : members ) {
            translate(member, activity);
        }
        APGenModel m = this.apgenModel;
        if ( parent instanceof APGenModel ) {
            m = (APGenModel)parent;
        }
        m.activities.put(activity.name, activity);

        // fix parameters to meet constructors
        fixParameters(e, activity, parent);
    }

    protected void fixParameters(EntityDecl e, Activity activity, Object parent) {
        String className = e.ident();
        // find the longest constructor
        List<Exp> ctors = getConstructorCalls().get(className);
        if ( Utils.isNullOrEmpty( ctors ) ) return;
        CallApplExp longestCtor = null;
        for ( Exp ctor : ctors ) {
            scala.collection.immutable.List<Argument> args = null;
            if ( ctor instanceof CallApplExp ) {
                args = ((CallApplExp) ctor).args();
            }
            if ( longestCtor == null || args.length() > longestCtor.args().length() ) {
                longestCtor = (CallApplExp)ctor;
            }
            // TODO -- check to verify that arguments match longest for the fewer args.
            // Give a warning if they don't
        }
        Collection<Argument> args = JavaConversions.asJavaCollection(longestCtor.args());
        if ( Utils.isNullOrEmpty( args ) ) return;
        LinkedHashMap<String, Parameter> oldParameters = activity.parameters;
        activity.parameters = new LinkedHashMap<>();
        for ( Argument arg : args ) {
            if ( arg instanceof NamedArgument ) {  // This should always be true.
                NamedArgument narg = (NamedArgument) arg;
                String name = narg.ident();
                if (oldParameters.containsKey(name)) {
                    Parameter p = oldParameters.get(name);
                    activity.parameters.put(name, p);
                    oldParameters.remove(name);
                } else {
                    if ( name.equals("startTime") || name.equals("endTime") || name.equals("duration") ) {
                        Parameter p =
                                new Parameter(name, "time", null);
                        // TODO -- What is the type for duration?!  We're assuming "time!"
                        activity.parameters.put(name, p);
                        // TODO -- HERE!! -- Need to set Start or Duration in the attributes.
                        String apgenName = name.equals("startTime") ? "Start" : name.equals("endTime") ? "Duration" : name.equals("duration") ? "Duration" : null;
                        String attr = activity.attributes.get(apgenName);
                        if ( attr != null ) {
                            // TODO -- WARNING -- OVERWRITING attribute
                        }
                        if ( name.equals("startTime") ) {
                            attr = name;
                        } else if ( name.equals("endTime") ) {
                            attr = "endTime - start";
                        }

                        activity.attributes.put(apgenName, attr);
                    } else {
                        Debug.error(true, true, "Couldn't find parameter, " + name + ", in activity, " + activity.name + ", for constructor call, " + longestCtor);
                    }
                }
            } else {
                Debug.error(true, true, "Unexpected positional argument, " + arg + ", in constructor call, " + longestCtor);
            }
        }
        // The remaining parameters are creation params.
        activity.creation.putAll(oldParameters);
    }

    public void translate(TopDecl d) {
        if ( d instanceof AnnotationDecl ) {
            translate( (AnnotationDecl)d, apgenModel );
        } else if ( d instanceof MemberDecl ) {
            translate( (MemberDecl)d, apgenModel );
        }
    }
    public void translate(AnnotationDecl d, Object parent) {
        // TODO ??
    }
    public void translate(Annotation d, Object parent) {
        // TODO ??
    }

    public void translate(MemberDecl d, Object parent) {
        String modeling = null;
        if ( d instanceof EntityDecl ) {
            translate((EntityDecl) d, parent);
        } else if ( d instanceof TypeDecl ) {
            translate( (TypeDecl)d, parent );
        } else if ( d instanceof PropertyDecl ) {
            modeling = translate( (PropertyDecl)d, parent );
        } else if ( d instanceof FunDecl ) {
            translate( (FunDecl)d, parent );
        } else if ( d instanceof ConstraintDecl ) {
            modeling = translate( (ConstraintDecl)d, parent );
        } else if ( d instanceof ExpressionDecl ) {
            modeling = translate( (ExpressionDecl)d, parent );
        }

        if ( !Utils.isNullOrEmpty(modeling) ) {
            if ( parent instanceof Activity ) {
                ((Activity)parent).modeling.append(modeling + "\n");
            }
        }
    }

    // This is like typedef: "type myType[T] = Map[T, Int]"
    public void translate(TypeDecl d, Object parent) {
        // TODO -- ???
        // TODO -- maybe keep a list of aliases and never explicitly translate but always replace with type on rhs.
        // TODO -- APGen has typedefs for structs and lists.
    }

    protected Map<String, Map<String, Pair<ConstraintDecl, Exp> > > propertyValuesInConstraints = new LinkedHashMap<>();
    protected Set< ConstraintDecl > constraintsInPropertDecls = new LinkedHashSet<>();

    /**
     * When a property is declared separate from it's value, put the value back into the property.<p>
     * More specifically, look for a constraint with the form<br>
     * <pre>
     * req x = someFunctionOrFormula()
     * </pre>
     * We remove the contraint and make <code>someFunctionOrFormula()}</code> the value in the property declaration of <code>x</code>.<p>
     *
     * So, we effectively replace
     * <pre>
     * var x : SomeType
     * req x = someFunctionOrFormula()
     * </pre>
     * with
     * <pre>
     * var x : SomeType = someFunctionOrFormula()
     * </pre>
     *
     * @param parent
     */
    protected void unitePropertiesAndConstraints( HasChildren parent ) {
        if ( parent == null ) return;
        String x = null;
        if ( parent instanceof EntityDecl ) {
            x = ((EntityDecl) parent).ident();
        } else {//if ( parent instanceof APGenModel ) {
            x = "Global";
        }
        if ( propertyValuesInConstraints.containsKey( x ) ) {
            // Already did this computation.
            return;
        }

        // Make maps for efficient, simple lookup
        Map<String, PropertyDecl > props = new LinkedHashMap<>();
        Map<String, ConstraintDecl > constrs = new LinkedHashMap<>();
        Map<String, Exp > constrRHSs = new LinkedHashMap<>();

        Collection<Object> children = JavaConversions.asJavaCollection(parent.children());
        for ( Object child : children ) {
            Exp e = null;
            if ( child instanceof ConstraintDecl ) {
                ConstraintDecl c = (ConstraintDecl) child;
                e = c.exp();
                if ( e instanceof BinExp && ((BinExp) e).op() instanceof EQ$ ) {
                    BinExp be = (BinExp) e;
                    if ( be.exp1() instanceof IdentExp ) {
                        String name = ((IdentExp)be.exp1()).ident();
                        constrs.put(name, c);
                        constrRHSs.put(name, be.exp2());
                    }
                }
            } else if ( child instanceof PropertyDecl ) {
                PropertyDecl p = (PropertyDecl) child;
                props.put(p.name(), p);
            }
        }

        // Now match constraints to the property, and if the property does not already have a value, put the right hand side of the constraint in as the value, and remove the constraint.
        for ( Map.Entry<String, Exp > entry : constrRHSs.entrySet() ) {
            String name = entry.getKey();
            PropertyDecl prop = props.get(name);
            if ( prop != null && get(prop.expr()) == null ) {
                Utils.put( propertyValuesInConstraints,
                           x,
                           name,
                           new Pair<>(constrs.get(name), entry.getValue()) );
//                        Option<Exp> val = Option.apply(entry.getValue());
//                        PropertyDecl newProp =
//                        new PropertyDecl(prop.modifiers(), prop.name(),
//                                prop.ty(), prop.multiplicity(), prop.assignment(),
//                                val);
            }
        }
    }


    public String translate(PropertyDecl d, Object parent) {
        Exp expr = get(d.expr()); // the property value

        // If there is an equals constraint with this property alone on the
        // left hand side, use the right hand side as the value and skip
        // processing the constraint later.
        if ( expr == null ) {
            Map<String, Exp > constrs = new LinkedHashMap<>();
            String name = null;
            if ( parent instanceof Activity ) {
                name = ((Activity) parent).entityName;
            } else if ( parent instanceof APGenModel ) {
                name = "Global";
            }
            Pair<ConstraintDecl, Exp> p =
                    Utils.get(propertyValuesInConstraints, name, d.name());
            if ( p != null ) {
                expr = p.second;
                if ( p.first != null ) {
                    constraintsInPropertDecls.add( p.first );
                }
            }
        }
        if ( expr != null && containsConstructorCall( expr ) ) {
            addDecomposition(expr, parent);
            return null;
        }

        String modeling = translateEffect(expr, parent);

        String exprString = translate(expr, parent);
        Parameter pp =
                new Parameter(d.name(),
                        translate(d.ty(), d.multiplicity()),
                        exprString);
        if ( parent instanceof APGenModel ) {
            ((APGenModel)parent).parameters.put(pp.name, pp);
        } else if ( parent instanceof Activity ) {
            ((Activity)parent).parameters.put(pp.name, pp);
        } else if ( parent instanceof Function ) {
            ((Function)parent).parameters.put(pp.name, pp);
        }
        Option<Exp> val = Option.apply(expr);
        PropertyDecl newProp = new PropertyDecl(d.modifiers(), d.name(),
                                d.ty(), d.multiplicity(), d.assignment(),
                                val);
        translateResource(newProp, parent);
        return modeling;
    }

    public void addAttributes( Resource resource, PropertyDecl propertyDecl ) {
        resource.otherAttributes.put("Description", "resource for class " + propertyDecl.toString().replaceAll("\"", "'").replaceAll("\n", " "));
        resource.otherAttributes.put("Legend", resource.name);
        resource.otherAttributes.put("Color", "Orange");
    }

    protected static Set<String> primTypes =
            new LinkedHashSet<>( Arrays.asList( "float", "integer", "time", "duration", "boolean" ) );

    /**
     * If the property declaration is for a state variable, then create an APGen resource for it.
     * @param d the property declaration
     * @param parent the parent APGen activity or {@link APGenModel}
     */
    public void translateResource(PropertyDecl d, Object parent) {
        if ( !ktoJava.isStateVariable( d ) ) {
            return;
        }
        Resource r = new Resource();
        String classPrefix = "";
        if ( parent instanceof Activity ) {
            classPrefix = kToApgenClassName( ((Activity) parent).name ) + "_";
        }
        r.name = classPrefix + d.name();
        String type = getResourceTypeName(d.ty());
        String ltype = type == null ? null : type.toLowerCase();
        r.type = "string";
        r.usage = "State";
        if ( type != null ) {
            r.behavior = Resource.Behavior.nonconsumable;
            String apgType = javaToApgenType(type);
            if ( primTypes.contains(apgType) ) {
                r.type = apgType;
            } else {
                r.behavior = Resource.Behavior.state;
            }
        }
        addAttributes(r, d);

        // If the variable is assigned to some expression, set the profile to that.
        r.profile = translate(d.expr(), parent);

        // Get the type of TimeVaryingMap to determine the type and values
        // (e.g. possible states) of the resource.
        addStatesOfTypeToResource(parent, type, r);
        if ( !Utils.isNullOrEmpty(r.states) ) {
            // create global array and State parameter
            String arrayName = classPrefix + type;

            // cleanup -- remove activities that are really state modes
            apgenModel.activities.remove( arrayName );

            String statesStr = statesArray( r.states );
            Parameter arrayProp =
                    new Parameter(arrayName, "array", statesStr);
            apgenModel.parameters.put(arrayName, arrayProp);
            Parameter p =
                    new Parameter("State", "string", arrayName + "[0]");
            r.parameters.add(p);
            // If no value is specified for the variable, make its default the first mode/state in the array.
            if ( Utils.isNullOrEmpty(r.profile) ) {
                r.profile = arrayName + "[0]";
            }
            r.states.clear();
            r.states.add(arrayName);
        } else {
            // TODO -- r.profile should be assigned default value as specified in TimeVaryingMap constructor.
            // TODO -- We need to handle the case where a resource is defined by a function instead of a constructor.
            // TODO -- How do we say that a resource is the sum of two other resources in APGen?
            if ( Utils.isNullOrEmpty(r.profile) ) {
                r.profile = Parameter.getDefaultForType(r.type);
            }
            Parameter p =
                    new Parameter("State", r.type, Parameter.getDefaultForType(r.type) );
            r.parameters.add( p );
        }
        APGenModel m = apgenModel;
        if ( parent instanceof APGenModel ) {
            m = (APGenModel) parent;
        }
        m.resources.put(r.name, r);
    }

    public String statesArray( List<String> states ) {
        StringBuffer sb = new StringBuffer();
        sb.append("[ ");
        boolean first = true;
        for ( String s : states ) {
            if ( first ) first = false;
            else sb.append(", ");
            sb.append("\"" + s + "\"");
        }
        sb.append(" ]");
        return sb.toString();
    }

    protected void addStatesOfTypeToResource(Object parent, String typeName, Resource r) {
        Class<?> cls = null;
        try {
            cls = ClassUtils.classForName(typeName);
        } catch (ClassNotFoundException e) {
        }
        if ( ( cls == null || !ClassUtils.isPrimitive( cls ) ) &&
             typeName != null && !typeName.equals("Time") && !typeName.equals("Duration") ) {
            // Object state variable
            // Getting already defined parameters in the
            Map<String, Parameter> params = null;
            if ( parent instanceof APGenModel ) {
                params = ((APGenModel) parent).parameters;
            } else if ( parent instanceof Activity ) {
                // REVIEW -- TODO -- Would all of these be known--couldn't it be in the middle of collecting these?
                params = ((Activity) parent).parameters;
            }
            for ( Parameter param : params.values() ) {
                if ( typeName.equals( param.type ) ) {  // TODO -- should check type is a superclass of type.
                    r.states.add(param.name);
                }
            }
            // If the states weren't found, maybe they are defined
            // in an outer scope--big assumption!!!
            if ( Utils.isNullOrEmpty( r.states ) ) {
                if ( parent instanceof Activity ) {
                    Object grandparent = ((Activity) parent).parentScope;
                    if ( grandparent != null ) {
                        addStatesOfTypeToResource(grandparent, typeName, r);
                    }
                }
            }
        }
        // Need to save this elsewhere since this will get overwritten by the array name.
        if ( r.states != null ) {
            r.stateValues.addAll(r.states);
        }
    }

    public String getResourceTypeName(Object o) {
        if ( o instanceof Class ) {
            return getResourceTypeName( (Class<?>)o );
        }
        if ( o instanceof Type ) {
            return getResourceTypeName( (Type)o );
        }
        return getResourceTypeName( "" + o, true );
    }
    public String getResourceTypeName(Class<?> cls) {
        if ( cls == null ) return null;
        TypeVariable<? extends Class<?>>[] parms = cls.getTypeParameters();
        if ( parms != null && parms.length > 0 ) {
            return parms[0].getName();
        }
        List<Class<?>> supers = ktoJava.getSuperClasses(cls);
        for ( Class<?> c : supers ) {
            String rt = getResourceTypeName( c );
            if ( !Utils.isNullOrEmpty( rt ) ) {
                return rt;
            }
        }
        return null;
    }
    public String getResourceTypeName(Type type) {
        String typeStr = type.toJavaString().trim();
        String rt = getResourceTypeName( typeStr, true );
        if ( !Utils.isNullOrEmpty( rt ) ) {
            return rt;
        }
        typeStr = type.toString().trim();
        rt = getResourceTypeName( typeStr, true );
        return rt;
    }
    public String getResourceTypeName(String typeStr, boolean deep) {
        int pos = typeStr.indexOf('<');
        if ( pos > 0 && typeStr.lastIndexOf('>') == typeStr.length() -1  ) {
            String rType = typeStr.substring( pos + 1, typeStr.length() - 1 );
            return rType;
        }
        if ( deep ) {
            List<Object> supers = ktoJava.getSuperClasses(typeStr);
            for (Object o : supers) {
                String rt = getResourceTypeName( o );
                if ( !Utils.isNullOrEmpty( rt ) ) {
                    return rt;
                }
            }
        }
        return null;
    }

    public boolean isConstructorAppl( Exp exp ) {
        return ktoJava.isConstructorCall( exp );
    }

    public boolean containsConstructorCall( HasChildren exp ) {
        if (exp instanceof Exp && isConstructorAppl((Exp)exp)) return true;
        if ( exp.children() != null ) {
            for ( Object x : JavaConversions.asJavaCollection( exp.children() ) ) {
                if ( x instanceof HasChildren &&
                     containsConstructorCall((HasChildren)x) ) {
                    return true;
                }
            }
        }
        return false;
    }

    protected <T> T get(Option<T> o) {
        if ( o == null || o.isEmpty() ) {
            return null;
        }
        return o.get();
    }

    public String translate(Option<Exp> expr, Object parent) {
        if ( expr == null ) return null;
        Exp exp = get(expr);
        if ( exp == null ) return null;
        String estr = translate(exp, parent);
        return estr;
    }

    protected static String[] typePrecedence =
            new String[] { "array", "string", "time", "duration", "float", "integer", "boolean" };

    protected String mostSpecificCommonSuperclass(String type1,  String type2) {
        if ( type1 == null ) return type2;
        if ( type2 == null ) return type1;
        if ( type1.equals(type2) ) return type1;
        for ( String typeName : typePrecedence ) {
            if ( type1.equals(typeName) || type2.equals(typeName) ) {
                return typeName;
            }
        }
        // REVIEW -- this case is unexpected; add error?
        return type1;
    }

    public String javaToApgenType(String type) {
        if ( type == null || type.isEmpty() ) return null;
        String apgenType = type;
        String ltype = type.toLowerCase();
        if ( ltype.equals("real") || ltype.equals("double") || ltype.equals("float") ) {
            apgenType = "float";
        } else if (ltype.equals("int") || ltype.equals("integer") || ltype.equals("short") || ltype.equals("long") ) {
            apgenType = "integer";
        } else if (ltype.equals("time") ) {
            apgenType = "time";
        } else if (ltype.equals("duration") ) {
            apgenType = "duration";
        } else if (ltype.equals("string") || ltype.equals("character") ) {
            apgenType = "string";
        } else if (ltype.equals("boolean") || ltype.equals("bool") ) {
            apgenType = "boolean";
        }
        return apgenType;
    }

    public String translate(Type ty, Option<Multiplicity> multiplicity) {
        Multiplicity m = get(multiplicity);
        return translate(ty, m);
    }
    public String translate(Type ty, Multiplicity m) {
        String typeStr = ty.toString();
        if ( !typeStr.equals("Time") && !typeStr.equals("Duration") ) {
            typeStr = ty.toJavaString();
        }
        if ( m != null && m.exp1() != null &&
             ( ( m.exp2() != null && m.exp2().get() != null && !m.exp2().get().toJavaString().equals("1") )
               || !m.exp1().toJavaString().equals("1") ) ) {
            return typeStr + "[]";
        } else {
            typeStr = javaToApgenType(typeStr);
        }
        return typeStr;
    }

    public void translate(FunDecl d, Object parent) {
        Function f = new Function();
        f.name = d.ident();
        Collection<Param> params = JavaConversions.asJavaCollection( d.params() );

        // Preprocess entity to find constraints that could be merged with property declarations.
        unitePropertiesAndConstraints( d ); // ok to call multiple times for same parent


        if ( parent instanceof APGenModel ) {
            ((APGenModel)parent).functions.put(f.name, f);
        } else if ( parent instanceof Activity ) {
            // APGen activities can't have functions.
            apgenModel.functions.put(f.name, f);
        } else {
            // TODO -- ERROR
        }
    }

    public String translateEffect(ConstraintDecl d, Object parent) {
        return translateEffect(d.exp(), parent);
    }
    public String translateEffect(HasChildren exp, Object parent) {
        FunApplExp effect = ktoJava.getEffect( exp );
        String callNameNoArgs = effect == null ? null : "" + effect.exp();
        Exp scope = effect == null ? null : KtoJava.getScopeExp(effect);
        List<Argument> args = effect == null ? null : new ArrayList(JavaConversions.asJavaCollection(effect.args()));
        if ( effect != null && callNameNoArgs.endsWith("setValue") && scope != null && args != null && args.size() == 2) {
            String resourceStr = translate(scope, parent);  // TODO -- REVIEW -- Calling translate multiple times okay?!  Is it statelesss, or are things consumed in a special order?
            String timeExpStr = translate(args.get(0), parent);
            String valueStr = translate(args.get(1), parent);

            String className = ktoJava.globalName;
            if ( parent instanceof Activity ) {
                className = ((Activity) parent).entityName;
            }
            String pScope = ktoJava.getClassData().scopeForParameter(className, resourceStr, false, false);
            pScope = kToApgenClassName(pScope);

            String modeling =
                    ("start".equals(timeExpStr) ? "" : "wait for " + timeExpStr + " - start;\n") +
                            "set " + (pScope == null ? "" : pScope + "_") + resourceStr.replace(".currentval()", "") + "(" + valueStr + ");";
            return modeling;  // TODO - Do we want to continue and add a constraint for this, too?  If it's 'req foo = setValue(t,v)' and foo isn't used elsewhere, then no, else probably.
        } else {
            // TODO!!! add(), . . .
        }
        return null;
    }

    public String translate(ConstraintDecl d, Object parent) {
        if ( constraintsInPropertDecls.contains( d ) ) {
            return "";
        }
        // Create a timeline and constraint
        if ( d.toString().contains("Timepoint.set") ) {
            return "";
        }
        String modeling  = translateEffect(d, parent);
        if ( !Utils.isNullOrEmpty(modeling) ) {
            return modeling;  // TODO - Do we want to continue and add a constraint for this, too?  If it's 'req foo = setValue(t,v)' and foo isn't used elsewhere, then no, else probably.
        }

        // TODO -- need to create a resource for all of the non-resource variables in the constraint, but
        // there's a problem where each instance of an activity creates new variables, and resources
        // aren't meant to be created on-the-fly, but Pierre mentioned a capability to create a resource
        // on the fly.  We have all of the Event/object instances in the solution, so hopefully won't
        // need the on-the-fly resource creation.

        // So, if the constraint involves a variable that is not a resource, bail.
        String parentName = ktoJava.globalName;
        if ( parent instanceof Activity ) {
            parentName = ((Activity) parent).entityName;
        }
        if ( ktoJava.hasNonResourceVariable( d, parentName ) ) {
            return "";
        }

        Resource r = new Resource();
        if (get(d.name()) != null ) {
            r.name = d.name().get();
        } else {
            r.name = toIdentifier("res_" + d );
        }
        r.otherAttributes.put("Description", "resource for constraint, " + d.toString().replaceAll("^req ", "").replaceAll("\"", "'").replaceAll("\n", " "));
        r.otherAttributes.put("Legend", r.name);
        r.otherAttributes.put("Color", "Green");
        r.type = "string";
        r.behavior = Resource.Behavior.state;
        r.states = Utils.newList("\"active\"", "\"inactive\"");
        r.profile = "\"inactive\"";
        r.parameters.add(new Parameter("State", "string", "inactive"));
        r.usage = "State";

        Constraint c = new Constraint();
        c.name = "c_" + r.name.replace("res_", "");
        c.condition = (r.name + " == \"active\" && (" + translate(d.exp(), parent) + ")").replaceAll(".currentval[(][)]", "");
        c.message = ("failed assertion: " + c.condition).replaceAll("\"", "'").replaceAll("\n", " ").replaceAll(".currentval[(][)]", "");

        // effect on resource in activity
        String vName = "constraint_" + r.name.replace("res_", "");
        String val = "\"active\"";//"(" + translate(d.exp(), parent) + ") ? \"true\" : \"false\"";
//        Parameter cp =
//                new Parameter(vName, "string", val);
        modeling = //cp.toString() + "\n" +
                //"use " + r.name +"(" + vName + ") from start to finish\n";
                "use " + r.name.replace(".currentval()", "") +"(" + val + ") from start to finish;\n";
        if ( parent instanceof APGenModel ) {
            ((APGenModel) parent).resources.put(r.name, r);
            ((APGenModel) parent).constraints.put(c.name, c);
        } else if ( parent instanceof Activity ) {
            ((Activity)parent).modeling.append(modeling);
            apgenModel.resources.put(r.name, r);
            apgenModel.constraints.put(c.name, c);
        } else {
            // TODO -- ERROR
        }
        return modeling;
        //activity.modeling.append(cp.toString() + ";\n");
        //activity.modeling.append("use " + r.name +"(vName) from start to finish;\n");
    }

    private static LinkedHashMap<String, String> toIdentReplacements = new LinkedHashMap<String, String>() {
        {
            put("[ _]*>=[ _]*", " gte ");
            put("[ _]*>=[ _]*", " gte ");
            put("[ _]*>[ _]*", " gt ");
            put("[ _]*<=[ _]*", " lte ");
            put("[ _]*<[ _]*", " lt ");
            put("[ _]*==?[ _]*", " eq ");
            put("[ _]*[+][ _]*", " plus ");
            put("[ _]*-[ _]*", " minus ");
            put("[ _]*[*][ _]*", " times ");
            put("[ _]*[/][ _]*", " div ");
            put("[ _]*[%][ _]*", " mod ");
            put("[^0-9A-Za-z_][^0-9A-Za-z_]*", "_");
        }
    };

    protected String toIdentifier( String s ) {
        String i = s;
        for ( Map.Entry<String, String> e : toIdentReplacements.entrySet() ) {
            i = i.replaceAll(e.getKey(), e.getValue());
        }
        return i;
    }

    public String translate(ExpressionDecl d, Object parent) {
        String modeling = translateEffect(d.exp(), parent);
        if ( !Utils.isNullOrEmpty(modeling) ) {
            return modeling;
        }
        String s = translate(d.exp(), parent);
        if ( parent instanceof APGenModel ) {
            // TODO -- ERROR -- No place to put an expression
        } else if ( parent instanceof Activity ) {
            // TODO -- ERROR -- No place to put an expression
        } else {
            // TODO -- ERROR?
        }
        return null;
    }

    public String translate(Exp d, Object parent) {
        if ( d == null ) return null;
        if ( d instanceof ParenExp ) {
            return translate( (ParenExp)d, parent );
        } else if ( d instanceof IdentExp ) {
            return translate( (IdentExp)d, parent );
        } else if ( d instanceof DotExp ) {
            return translate( (DotExp)d, parent );
        } else if ( d instanceof IndexExp ) {
            return translate( (IndexExp)d, parent );
        } else if ( d instanceof ClassExp ) {
            return translate( (ClassExp)d );
        } else if ( d instanceof FunApplExp ) {
            return translate( (FunApplExp)d, parent );
        } else if ( d instanceof CtorApplExp ) {
            return translate( (CtorApplExp)d, parent );
        } else if ( d instanceof IfExp ) {
            return translate( (IfExp)d, parent );
        } else if ( d instanceof MatchExp ) {
            return translate( (MatchExp)d, parent );
        } else if ( d instanceof MatchCase ) {
            return translate( (MatchCase)d, parent );
        } else if ( d instanceof BlockExp ) {
            return translate( (BlockExp)d, parent );
        } else if ( d instanceof WhileExp ) {
            return translate( (WhileExp)d, parent );
        } else if ( d instanceof ForExp ) {
            return translate( (ForExp)d, parent );
        } else if ( d instanceof BinExp ) {
            return translate( (BinExp)d, parent );
        } else if ( d instanceof UnaryExp ) {
            return translate( (UnaryExp)d, parent );
        } else if ( d instanceof QuantifiedExp ) {
            return translate( (QuantifiedExp)d, parent );
        } else if ( d instanceof TupleExp ) {
            return translate( (TupleExp)d, parent );
        } else if ( d instanceof CollectionRangeExp ) {
            return translate( (CollectionRangeExp)d, parent );
        } else if ( d instanceof CollectionComprExp ) {
            return translate( (CollectionComprExp)d, parent );
        } else if ( d instanceof LambdaExp ) {
            return translate( (LambdaExp)d, parent );
        } else if ( d instanceof ReturnExp ) {
            return translate( (ReturnExp)d, parent );
//        } else if ( d instanceof BreakExp ) {
//            translate( (BreakExp)d );
//        } else if ( d instanceof ContinueExp ) {
//            translate( (ContinueExp)d );
//        } else if ( d instanceof ResultExp ) {
//            translate( (ResultExp)d );
//        } else if ( d instanceof StarExp ) {
//            translate( (StarExp)d );
        } else if ( d instanceof Argument ) {
            return translate( (Argument)d, parent );
        } else if ( d instanceof Literal ) {
            return translate( (Literal)d );
        }
        return null;
    }
    public String translate(ParenExp d, Object parent) {
        return "(" + translate(d.exp(), parent) + ")";
    }
    public String translate(IdentExp d, Object parent) {
        if ( "startTime".equals(d.ident()) ) return "start";
        if ( "endTime".equals(d.ident()) ) return "finish";
        String svString = translateAsStateVariable(d, parent);
        if ( !Utils.isNullOrEmpty( svString ) ) {
            return svString;
        }
        String type = getType(d, parent);
        String name = d.toJavaString();
        // If the object is a mode/state value (often an object and not a primitive), translate to a string value.
        if ( (type == null || !Parameter.apgenTypes.contains(type)) && !Utils.valuesEqual(name, type) && isResourceState( name ) ) {
            return "\"" + d.toJavaString() + "\"";
        }
        return d.toJavaString();
    }

    public boolean isResourceState( String ident ) {
        Collection<Resource> resources = apgenModel.resources.values();
        for ( Resource r : resources ) {
            if ( r.states != null && r.stateValues.contains(ident) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * The Java type of the K expression.  This is not complete for all expression types.
     * @param exp an AST element of parsed K; the classes of these elements are found in {@link k.frontend}.AbstractSyntax.scala
     * @param parent the parent scope, i.e. the name of the class immediately surrounding the expression
     * @return name of the Java type/class
     */
    public String getType(Object exp, Object parent) {
        LinkedHashSet<String> classNames = getPossibleScopes( parent );
        String type = null;
        for ( String className : classNames ) {
            type = getType(exp, className);
            if ( type != null && !type.isEmpty() ) break;
        }
        return type;
    }

    /**
     * The Java type of the K expression.  This is not complete for all expression types.
     * @param exp an AST element of parsed K; the classes of these elements are found in {@link k.frontend}.AbstractSyntax.scala
     * @param parent the parent scope, i.e. the name of the class immediately surrounding the expression
     * @return name of the Java type/class
     */
    public String getType(Object exp, String parent) {
        String javaType = ktoJava.getType(exp, parent);
        String apgenType = javaToApgenType( javaType );
        if ( apgenType != null ) {
            return apgenType;
        }
        return javaType;
    }

    protected LinkedHashSet<String> getPossibleScopes( Object parent ) {
        LinkedHashSet<String> classNames = new LinkedHashSet<>();
        if ( parent instanceof Activity ) {
            classNames.add(((Activity) parent).entityName);
        }
        classNames.add(ktoJava.globalName);
        classNames.add(ktoJava.getClassData().getCurrentClass());
        return classNames;
    }

    /**
     * Try to translate the expression as a reference to a state variable.
     * @param exp a reference to a state variable; this may be and IdentExp or a DotExp
     * @param parent the scope in which the variable is referenced
     * @return the translated name of the state variable or null if not a state variable.
     */
    public String translateAsStateVariable(Exp exp, Object parent) {
        LinkedHashSet<String> classNames = getPossibleScopes( parent );
        ClassData.Param p = null;
        for ( String className : classNames ) {
            ClassData.Param p1 = ktoJava.getMember(exp, className);
            if (p1 != null && (p == null || (p.type == null && p1.type != null))) {
                p = p1;
            }
        }
        if ( p != null && p.type != null && ktoJava.isStateVariableType(p.type)) {
            // FIXME -- TODO -- This assumes only one instance of the state variable.
            String name = null;
            if ( !Utils.isNullOrEmpty( p.scope ) ) {
                name = kToApgenClassName( p.scope ) + "_" + p.name + ".currentval()";
            } else {
                name = p.name + ".currentval()";
            }
            return name;
        }
        return null;
    }

    public String translate(DotExp d, Object parent) {
        String svString = translateAsStateVariable(d, parent);
        if ( !Utils.isNullOrEmpty( svString ) ) {
            return svString;
        }
        StringBuffer sb = new StringBuffer();
        String scope = translate(d.exp(), parent);
        sb.append(scope);
        sb.append(".");
        sb.append(d.ident());

        return sb.toString();
    }
    public String translate(IndexExp d, Object parent) {
        return d.toJavaString();
    }
    public String translate(ClassExp d) {
        return d.toJavaString();
    }
    public String translate(CallApplExp d, Object parent) {
        if (d instanceof FunApplExp) {
            return translate((FunApplExp) d, parent);
        } else if (d instanceof CtorApplExp) {
            return translate((CtorApplExp) d, parent);
        } else {
            // ???!!
        }
        return null;
    }

    public String classNameWithScope(String name) {
        String nws = ktoJava.getClassData().getClassNameWithScope(name, false);
        if ( nws != null ) {
            return nws;
        }
        return name;
    }

    public String kToApgenClassName(String name) {
        if ( Utils.isNullOrEmpty(name) ) return null;
        String n = name;

        // remove eclosing object scope
        int pos = name.lastIndexOf('.');
        if ( pos >= 0 ) {
            String scope = name.substring(0, pos);
            if ( !ktoJava.getClassData().isClassName(scope) ) {
                String simpleClassName = name.substring(pos+1);
                if ( ktoJava.getClassData().isClassName(simpleClassName) ) {
                    n = simpleClassName;
                }
            }
        }

        n = classNameWithScope(n);
        if ( Utils.isNullOrEmpty(n) ) n = name;
        n = n.replaceAll("[.]", "_");
        n = n.replaceFirst("^Global_", "");
        return n;
    }

    public void addDecomposition(Exp d, Object parent) {
        if ( parent instanceof Activity ) {
            Activity a = (Activity)parent;
            String s = translate(d, parent);
            String ctorStr = s.replaceFirst("^new ", "");
            a.decomposition.append( ctorStr + ";\n" );// + " at start;\n");
        } else {
            // TODO -- Should instantiations at the global level be wrapped in a global activity?
            Debug.error(true, false, "constructor at top level ignored! " + d.toJavaString());
        }
    }
    public String translate(FunApplExp d, Object parent) {
        boolean isCtor = isConstructorAppl(d);
            // create decmposition
        if ( addingToDecomposition && isCtor ) {
            addDecomposition(d, parent);
            return null;
        }

        String n = translate(d.exp1(), parent);
        if ( isCtor ) {
            n = kToApgenClassName(n);
        }
        Collection<Argument> args = JavaConversions.asJavaCollection(d.arguments());
        return translateCall(n, args, parent, isCtor);
//        StringBuffer sb = new StringBuffer();
//        sb.append(n + "(");
//        boolean first = true;
//        String startAt = "start";
//        for (Argument arg : args) {
//            if ( first ) first = false;
//            else sb.append(", ");
//            sb.append(translate(arg, parent));
//            if ( arg instanceof NamedArgument ) {
//                NamedArgument narg = (NamedArgument)arg;
//                if ( "startTime".equals(narg.ident()) ) {
//                    // WARNING! REVIEW -- Is it olay to call translate on the same expression again?
//                    startAt = translate( narg.exp(), parent );
//                }
//            }
//        }
//        sb.append(")");
//        if ( isCtor ) {
//            sb.append(" at " + startAt);
//        }
//        return sb.toString();
    }

    public String translate(CtorApplExp d, Object parent) {
        if ( addingToDecomposition ) {
            // create decmposition
            addDecomposition(d, parent);
            return null;
        }
        String n = d.ty().toJavaString();
        n = kToApgenClassName(n);
        Collection<Argument> args = JavaConversions.asJavaCollection(d.arguments());
        return translateCall(n, args, parent, true);
//        boolean first = true;
//        for (Argument arg : args) {
//            if ( first ) first = false;
//            else sb.append(", ");
//            sb.append(translate(arg, parent));
//        }
//        sb.append(") at start");
//        return sb.toString();
    }

    protected String translateCall(String n, Collection<Argument> args, Object parent, boolean isCtor ) {
        StringBuffer sb = new StringBuffer();
        sb.append(n + "(");
        boolean first = true;
        String startAt = "start";
        for (Argument arg : args) {
            if ( first ) first = false;
            else sb.append(", ");
            sb.append(translate(arg, parent));
            if ( isCtor && arg instanceof NamedArgument ) {
                NamedArgument narg = (NamedArgument)arg;
                if ( "startTime".equals(narg.ident()) ) {
                    // WARNING! REVIEW -- Is it olay to call translate on the same expression again?
                    startAt = translate( narg.exp(), parent );
                }
            }
        }
        sb.append(")");
        if ( isCtor ) {
            sb.append(" at " + startAt);
        }
        return sb.toString();
    }

    public String addIfTheElseFunction(String type) {
        String fName = "ifThenElse" + Utils.capitalize(type);
        if ( !apgenModel.functions.containsKey(fName) ) {
            Function f = new Function();
            f.name = fName;
            Parameter pc = new Parameter("c", "boolean", null);
            Parameter pt = new Parameter("t", type, null);
            Parameter pf = new Parameter("f", type, null);
            f.parameters.put("c", pc);
            f.parameters.put("t", pt);
            f.parameters.put("f", pf);
            f.body = "if ( c ) {\n  return t;\n}\nreturn f;";
            apgenModel.functions.put(fName, f);
        }
        return fName;
    }

    public String translate(IfExp d, Object parent) {
        StringBuffer sb = new StringBuffer();
//        String parentName = ktoJava.globalName;
//        if ( parent instanceof Activity ) {
//            parentName = ((Activity)parent).entityName;
//        }
        String tbType = getType(d.trueBranch(), parent);
        String fbType = getType(get(d.falseBranch()), parent);
        String type = mostSpecificCommonSuperclass( tbType, fbType );
        String fName = addIfTheElseFunction(type);
        sb.append( fName );
        sb.append("(");
        //sb.append("if (");
        sb.append( translate(d.cond(), parent) );
        sb.append( ", " );
        //sb.append(") {\n    ");
        sb.append( translate(d.trueBranch(), parent) );
        sb.append( ", " );
        Exp fb = get(d.falseBranch());
        if ( fb == null ) {
            sb.append("null");
        } else {
            //sb.append(";\n} else {\n    ");
            sb.append(translate(d.falseBranch(), parent));
        }
        sb.append( ")" );
        //sb.append(";\n}");

        return sb.toString();
    }

    // TODO??
    public String translate(MatchExp d, Object parent) {
        return d.toJavaString();
    }
    public String translate(MatchCase d, Object parent) {
        return d.toJavaString();
    }
    public String translate(BlockExp d, Object parent) {
        return d.toJavaString();
    }
    public String translate(WhileExp d, Object parent) {
        return d.toJavaString();
    }
    public String translate(ForExp d, Object parent) {
        return d.toJavaString();
    }

    public String translate(BinExp d, Object parent) {
        // remove "from begin to end X" which is translated to
        // "time >= start && time < finish => X" since it's redundant with resource == "active"
        if ( KtoJava.isFromBeginToEnd( d ) ) {
            return translate( d.exp2(), parent );
        }
        StringBuffer sb = new StringBuffer();
        sb.append(translate(d.exp1(), parent));
        sb.append(" " + translateBinOp(d.op()) + " ");
        sb.append(translate(d.exp2(), parent));
        return sb.toString();
    }

    public String translateBinOp(BinaryOp op) {
        if ( op instanceof EQ$ ) {
            return "==";
        }
        return "" + op;
    }

    // TODO??
    public String translate(UnaryExp d, Object parent) {
        return "" + d.op() + translate(d.exp(), parent);
    }
    public String translate(QuantifiedExp d, Object parent) {
        return d.toJavaString();
    }
    public String translate(TupleExp d, Object parent) {
        return d.toJavaString();
    }
    public String translate(CollectionRangeExp d, Object parent) {
        return d.toJavaString();
    }
    public String translate(CollectionComprExp d, Object parent) {
        return d.toJavaString();
    }
    public String translate(LambdaExp d, Object parent) {
        return d.toJavaString();
    }
    public String translate(ReturnExp d, Object parent) {
        return d.toJavaString();
    }
    public String translate(BreakExp d) {
        return d.toJavaString();
    }
    public String translate(ContinueExp d) {
        return d.toJavaString();
    }
    public String translate(ResultExp d, Object parent) {
        return d.toJavaString();
    }
    public String translate(StarExp d, Object parent) {
        return d.toJavaString();
    }
    public String translate(Argument d, Object parent) {
        if ( d instanceof NamedArgument ) {
            return translate((NamedArgument)d, parent);
        } else if ( d instanceof PositionalArgument ) {
            return translate(((PositionalArgument)d).exp(), parent);
        }
        return null;
    }
    public String translate(NamedArgument d, Object parent) {
        return d.toJavaString();
    }
    public String translate(Literal d) {
        if (d instanceof IntegerLiteral) {
            return translate((IntegerLiteral) d);
        } else if (d instanceof RealLiteral) {
            return translate((RealLiteral) d);
        } else if (d instanceof CharacterLiteral) {
            return translate((CharacterLiteral) d);
        } else if (d instanceof StringLiteral) {
            return translate((StringLiteral) d);
        } else if (d instanceof BooleanLiteral) {
            return translate((BooleanLiteral) d);
        } else if (d instanceof DateLiteral) {
            return translate((DateLiteral) d);
        } else if (d instanceof DurationLiteral) {
            return translate((DurationLiteral) d);
//        } else if (d instanceof NullLiteral) {
//            translate((NullLiteral) d);
//        } else if (d instanceof ThisLiteral) {
//            translate((ThisLiteral) d);
        }
        return null;
    }
    public String translate(IntegerLiteral d) {
        // Don't use toJavaString() here since it adds 'L'
        // to the end of the number to indicate it is long.
        return d.toString();
    }
    public String translate(RealLiteral d) {
        return d.toJavaString();
    }
    public String translate(CharacterLiteral d) {
        return d.toJavaString();
    }
    public String translate(StringLiteral d) {
        return d.toJavaString();
    }
    public String translate(BooleanLiteral d) {
        return d.toJavaString();
    }
    public String translate(NullLiteral d) {
        return d.toJavaString();
    }
    public String translate(ThisLiteral d) {
        return d.toJavaString();
    }
    public String translate(DateLiteral d) {
        return d.toString();
    }
    public String translate(DurationLiteral dl) {
        Duration dur = Duration.parse(dl.toString());
        String s = TimeUtils.nanosToDurationStringHHMMSS(dur.toNanos());
        return s;
    }

    public Model getModel() {
        if ( model == null && ktoJava != null ) {
            model = ktoJava.model();
        }
        return model;
    }

    public String translateInstances(KtoJava kToJava) {
        // activity instances
        List<ActivityInstance> list = translateInstance(kToJava.mainEvent);
        // these are already added on creation
//        for ( ActivityInstance a : list ) {
//            apgenModel.activityInstances.put(a.getName(), a);
//        }
        StringBuffer sb = new StringBuffer();
        Date d = new Date();
        sb.append("apgen version \"generated_" +
                d.toString().replaceAll("[^A-Za-z0-9_]+", "_") + "\"\n\n");

        for ( ActivityInstance i : list ) {
            sb.append(i.toString() + "\n");
        }
        return sb.toString();
    }

    public static String translate(KtoJava kToJava) {
        Pair<Activity, List<Resource>> p =
                translateDeclaration(kToJava.mainEvent, kToJava);
        if ( p == null ) {
            // TODO -- error
            return null;
        }
        Activity act = p.first;
        List<Resource> resources = p.second;

        StringBuffer sb = new StringBuffer();

        if ( resources != null ) {
            for ( Resource r : resources ) {
                sb.append(r.toString() + "\n");
            }
        }
        if ( act != null ) {
            sb.append( act.toString() );
        }
        return sb.toString();
//        for ( EntityDecl c : kToJava.getTopLevelClasses() ) {
//
//        }
    }

    public static Pair< Activity, List< Resource > > translateDeclaration(ParameterListenerImpl event, KtoJava kToJava) {
        if ( event == null || kToJava == null ) {
            System.out.println("ERROR!  One of these is null: event = " + event + "; kToJava = " + kToJava);
        }
        TypeDeclaration type =
                EventXmlToJava.getTypeDeclaration(event.getClass().getName(),
                                                  kToJava.getClassData());
        Activity activity = new Activity();
        activity.entityName = event.getClass().getCanonicalName();
        activity.name = activity.entityName.replaceAll("[.]", "_");

        // Duration
        gov.nasa.jpl.ae.event.Parameter<Long> duration =
                (gov.nasa.jpl.ae.event.Parameter<Long>)event.getParameter("duration");
        Dependency<?> durDep = duration == null ? null : event.getDependency(duration);
        if ( durDep != null ) {
            String val = translate(durDep.getExpression(), kToJava);
            activity.attributes.put("Duration", val);
        }

        // Constructors determine parameters -- the rest are dependencies
        Map<String, gov.nasa.jpl.ae.event.Parameter<?>> ctorParams =
                constructorParameters(kToJava, event);
        if (ctorParams != null && !ctorParams.isEmpty()) {
            for ( gov.nasa.jpl.ae.event.Parameter<?> p : ctorParams.values() ) {
                Parameter pp = translateParameter(p);
                activity.parameters.put(pp.name, pp);
            }
        }

        for ( gov.nasa.jpl.ae.event.Parameter<?> p : event.getParameters() ) {
            String aeName = p.getName();
            if ( aeName != null && !aeName.isEmpty() && !ctorParams.containsKey( aeName ) ) {
                Parameter pp = translateParameter(p);
                activity.creation.put(pp.name, pp);
            }
        }

        for ( Dependency d : event.getDependencies() ) {
            if ( durDep != null && d.getParameter().getName().equals("duration") ) {
                continue;
            }
            String pName = d.getParameter().getName();
            String val = translate(d.getExpression(), kToJava);
            activity.assignValue(pName, val);
        }

        List<ConstraintExpression> otherConstraints = new ArrayList<ConstraintExpression>();
        for ( ConstraintExpression c : event.getConstraintExpressions() ) {
            if ( c.canBeDependency() ) {
                Pair<gov.nasa.jpl.ae.event.Parameter<?>, Object> p = c.dependencyLikeVar();
                if ( p == null || p.first == null | p.first.getName() == null || p.first.getName().isEmpty() ) continue;
                String val = translate(p.second, kToJava);
                activity.assignValue(p.first.getName(), val);
            } else {
                otherConstraints.add( c );
            }
        }


        // Constraints may be tracked with resources.
        List< Resource > resources = new ArrayList<Resource>();
        // TODO -- List< Constraint > constraints = new ArrayList<Constraint>();
        // TODO -- maybe make a common interface for the apgen stub classes and return a single list.

        for ( ConstraintExpression c : otherConstraints ) {
            Pair<Resource, String> pair = translate(c, kToJava);
            Resource r = pair == null ? null : pair.first;
            String modeling = pair == null ? null : pair.second;
            if ( r != null ) {
                resources.add(r);
            }
            if ( modeling != null ) {
                activity.modeling.append(modeling);
            }
        }

        Pair< Activity, List< Resource > > p = new Pair< Activity, List< Resource > >(activity, resources);
        return p;
    }

    public List<ActivityInstance> translateInstance(ParameterListenerImpl event) {
        Map<String, ActivityInstance> instances = new LinkedHashMap<>();
        if ( event == null ) return Utils.asList(instances.values());
//        String n = event.getName();
//        String t = event.getClass().getSimpleName();
//        ActivityInstance a = apgenModel.addActivityInstance(n, t);
//        instances.add(a);
////        HashSet<HasEvents> seen = new LinkedHashSet<HasEvents>();
////        seen.add(this);
        DurativeEvent durEvent = null;
        if ( event instanceof ParameterListenerImpl ) {
            Set<ParameterListenerImpl> events = ((ParameterListenerImpl) event).getObjects(true, null);
            events.add(event);
            for ( ParameterListenerImpl e : events ) {
                Set<ParameterListenerImpl> children = new LinkedHashSet<>();
                for ( ParameterListenerImpl c : events ) {
                    if ( Expression.valuesEqual( c.getOwningObject(), e, ParameterListenerImpl.class ) ) {
                        children.add( c );
                    }
                }
                Object owner = e.getOwningObject();
                ActivityInstance a = translateInstance( owner, e, children );
                if ( a != null ) {
                    instances.put(a.getName(), a);
                }
            }
        }

        // remove "abstractable into" when parent actvity does not exist
        for ( ActivityInstance a : instances.values() ) {
            if ( a.abstractable != null && !instances.containsKey( a.abstractable ) ) {
                a.abstractable = null;
            }
        }

        return Utils.asList(instances.values());
    }

    public ActivityInstance translateInstance(Object parent,
                                              ParameterListenerImpl event,
                                              Set<ParameterListenerImpl> children ) {
        if ( event == null ) return null;
        String n = event.getName();
        String t = kToApgenClassName( event.getClass().getSimpleName() );
        ActivityInstance a = apgenModel.addActivityInstance(n, t);
        //a.attributes.put("Start", Timepoint.getEpochTimepoint().toTimestamp());

        if ( a == null ) return null;

        // attributes -- defaults are probably already set, including Duration
        if ( event instanceof Event ) {
            gov.nasa.jpl.ae.event.Duration d = ((Event) event).getDuration();
            if ( d == null || d.getValue(false) == null ) {
                Debug.error(true, false,
                        "Warning!  KToAPGen.translateInstance(): Missing or ungrounded start time for " + event );
            } else {
                String dur = formatDuration(d.toMillis());
                if (dur != null) {
                    a.attributes.put("Duration", dur);
                }
            }
            Timepoint s = ((Event) event).getStartTime();
            if ( s == null || s.getValue(false) == null ) {
                Debug.error(true, false,
                        "Warning!  KToAPGen.translateInstance(): Missing or ungrounded start time for " + event );
            } else {
                String st = formatTimestamp(s);
                if (st != null) {
                    a.attributes.put("Start", st);
                }
            }
        }

        // parameters
        Activity act = a.getType();
        if ( act != null && !Utils.isNullOrEmpty(act.parameters) ) {
            // TODO -- make separate functions for the loop and the inside of the loop.
            for ( Map.Entry<String, Parameter> entry : act.parameters.entrySet() ) {
                String pName = entry.getKey();
                Parameter apgenParam = entry.getValue();
                String apgenType = apgenParam == null ? null : apgenParam.type;
                if ( !Parameter.apgenTypes.contains(apgenType) ) {
                    // REVIEW -- bother add warning?  this is expected and the same warnings are given for Parameter. 
                    continue;
                }
                boolean added = false;
                gov.nasa.jpl.ae.event.Parameter<?> p = event.getParameter(pName);
                String pVal = null;
                if (p != null && p.getValue(false) != null) {
                    if (p instanceof Timepoint) {
                        pVal = formatTimestamp((Timepoint)p);
                    } else if (p instanceof gov.nasa.jpl.ae.event.Duration) {
                        pVal = ((gov.nasa.jpl.ae.event.Duration) p).toShortFormattedStringForIdentifier();
                    } else {
                        pVal = MoreToString.Helper.toShortString(p.getValue(false));
                        if (!Utils.isNullOrEmpty(pVal) && apgenType != null) {
                            if (apgenType.equals("time")) {
                                try {
                                    Long tLong = Long.parseLong(pVal);
                                    pVal = formatTimestamp(tLong);
                                } catch (NumberFormatException nfe) {
                                    nfe.printStackTrace();
                                }
                            } else if (apgenType.equals("duration")) {
                                try {
                                    Long tLong = Long.parseLong(pVal);
                                    Long millis = gov.nasa.jpl.ae.event.Duration.durationToMillis(tLong);
                                    pVal = formatDuration(millis);
                                } catch (NumberFormatException nfe) {
                                    nfe.printStackTrace();
                                }
                            }
                        }
                    }
                }
                if (pVal != null) {
                    a.parameters.add(pVal);
                    added = true;
                } else if ( !added ) {
                    Debug.error(true, false, "Could not find a value for parameter " + pName + " in activity " + event );
                    // TODO? -- REVIEW -- consider adding a default value
                }
            }
        }

        // owner/parent
        if ( parent instanceof ParameterListenerImpl ) {
            a.abstractable = ((ParameterListenerImpl)parent).getName();
        }
        StringBuffer sb = new StringBuffer();

        // decomposition
        ArrayList<String> childNames = new ArrayList<>();
        for ( ParameterListenerImpl c : children ) {
            childNames.add( c.getName() );
        }
        if ( !childNames.isEmpty() ) {
            a.decomposedInto = String.join( ", ", childNames );
        }

        return a;
    }

    public static String formatTimestamp(Timepoint tp) {
        String v = tp.toDoyTimestamp();
        v = v.replaceFirst("[+-]0000?$", "");
        v = v.replaceFirst("[.]000$", "");
        v = v.replaceFirst("([0-9])[.]([0-9][0-9]).([0-9][0-9])($|[.][0-9][0-9][0-9])?", "$1:$2:$3$4");
        return v;
    }
    public static String formatTimestamp(long t) {
        String v = Timepoint.toDoyTimestamp(t);
        v = v.replaceFirst("[+-]0000?$", "");
        v = v.replaceFirst("[.]000$", "");
        v = v.replaceFirst("([0-9])[.]([0-9][0-9]).([0-9][0-9])($|[.][0-9][0-9][0-9])?", "$1:$2:$3$4");
        return v;
    }

    public static String formatDuration(long millis) {
        String pVal = gov.nasa.jpl.ae.event.Duration.toShortFormattedStringForIdentifier(millis);
        if ( millis < 365 * 24 * 3600 * 1000 ) {
            pVal = pVal.replaceFirst("^001T", "");
        }
        pVal = pVal.replaceFirst("[+]0000?$", "");
        pVal = pVal.replaceFirst("[.]000$", "");
        pVal = pVal.replaceFirst("([0-9])[.]([0-9][0-9]).([0-9][0-9])($|[.][0-9][0-9][0-9])?", "$1:$2:$3$4");
        return pVal;
    }

    public static Pair< Activity, List< Resource > > translateDeclaration(DurativeEvent event, KtoJava kToJava) {
        Pair< Activity, List< Resource > > pair =
                translateDeclaration( (ParameterListenerImpl) event, kToJava);
        if ( pair == null ) return pair;  // TODO -- ERROR?
        Activity activity = pair.first;
        // TODO -- effects

        // TODO -- elaborations
        for ( Map.Entry<ElaborationRule, Vector<Event>> e : event.getElaborations().entrySet() ) {
            if ( e.getKey() == null ) continue;
            String decomposition = translate( e.getKey(), kToJava );
            activity.decomposition.append(decomposition);
        }
        return pair;
    }

    public static String translate(ElaborationRule elaborationRule, KtoJava kToJava) {
        StringBuffer decomposition = new StringBuffer();

        Expression<Boolean> c = elaborationRule.getCondition();
        if ( c != null ) {
            decomposition.append("if (" + translate(c, kToJava) + ") {\n");
        }
        Vector<EventInvocation> invocations = elaborationRule.getEventInvocations();
        for ( EventInvocation invocation : invocations ) {
            String invString = translate(invocation, kToJava);
            if ( c != null ) {
//                invString = Util.indent(invString, 4);
            }
            decomposition.append(invString + ";\n");
        }

        if ( c != null ) {
            decomposition.append("}\n");
        }

        return decomposition.toString();
    }

    public static String translate(EventInvocation invocation, KtoJava kToJava) {
        StringBuffer inv = new StringBuffer();
        inv.append(invocation.getEventName() + "(");
        Vector<String> args = new Vector<String>();
        for ( Object a : invocation.getArguments() ) {
            String arg = translate(a, kToJava);
            args.add(arg);
        }
        inv.append( Utils.join(args, ", ") );
        inv.append(")");
        return inv.toString();
    }

    // TODO -- update this to be consistent with translate(ConstraintDecl, Object)
    public static Pair<Resource, String> translate(ConstraintExpression c, KtoJava kToJava) {
        // Create a timeline and constraint
        Resource r = new Resource();
        if (!Utils.isNullOrEmpty(c.getName()) ) {
            r.name = c.getName();
        } else {
            r.name = "res_" + ("" + c).replaceAll("[^0-9A-Za-z_][^0-9A-Za-z_]*", "_");
        }
        r.otherAttributes.put("Description", "resource for constraint, " + ("" + c).replaceAll("\"", "'").replaceAll("\n", " "));
        r.otherAttributes.put("Legend", r.name);
        r.otherAttributes.put("Color", "Green");
        r.type = "string";
        r.behavior = Resource.Behavior.state;
        r.states = Utils.newList("\"false\"", "\"true\"");
        r.profile = "\"true\"";
        r.parameters.add(new Parameter("State", "string", "true"));
        r.usage = "State";

        // effect on resource in activity
        String vName = r.name.replace("res_", "constraint_");
        String val = "(" + translate(c.getExpression(), kToJava) + ") ? \"true\" : \"false\"";
        Parameter cp =
                new Parameter(vName, "string", val);
        String modeling = //cp.toString() + "\n" +
                          "use " + r.name +"(" + vName + ") from start to finish;\n";
        //activity.modeling.append(cp.toString() + "\n");
        //activity.modeling.append("use " + r.name +"(vName) from start to finish\n");

        // TODO -- APGen constraint to check that resource == "true"
        return new Pair<Resource, String>(r, modeling);
    }


    public static String translate(Object o, KtoJava kToJava) {
        if ( o == null ) return null;
        if ( o instanceof Expression ) {
            return translate((Expression)o, kToJava);
        }
        if ( o instanceof Call ) {
            return translate((Call)o, kToJava);
        }
        if ( o instanceof HasName ) {
            return "" + ((HasName)o).getName();
        }
        return "" + o;
    }

    public static String translate(Expression<?> expression, KtoJava kToJava) {
        if ( expression == null ) return "null";
        String s = null;
        switch(expression.getForm()) {
            case Function:
                if (expression.expression instanceof FunctionCall) {
                    return translate((FunctionCall)expression.expression, kToJava);
                }
            case Constructor:
                if (expression.expression instanceof ConstructorCall) {
                    return translate((ConstructorCall)expression.expression, kToJava);
                }
            case Parameter:
                if (expression.expression instanceof gov.nasa.jpl.ae.event.Parameter) {
                    return translate((gov.nasa.jpl.ae.event.Parameter)expression.expression, kToJava);
                }
            case Value:
            case None:
            default:
                return translate(expression.expression, kToJava);
        }
        //return "" + expression;
    }

    public static String translate(gov.nasa.jpl.ae.event.Parameter parameter, KtoJava kToJava) {
        // TODO -- just need to print name
        if ( "startTime".equals(parameter.getName()) ) return "start";
        if ( "endTime".equals(parameter.getName()) ) return "finish";
        if ( "duration".equals(parameter.getName()) ) return "Duration";
        return translate((Object)parameter, kToJava);
    }

    public static String translateFunctionName(String callName) {
        //if ( "".equals(callName) )
        return callName;
    }

    public static String translate(ConstructorCall call, KtoJava kToJava) {
        // TODO?
        return translate((Call)call, kToJava);
    }

    public static String translate(Call call, KtoJava kToJava) {
        // TODO?
        StringBuffer sb = new StringBuffer();
        if ( call.getObject() != null ) {
            sb.append(translateFunctionName(call.getName()));
        }
        sb.append("(");
        boolean first = true;
        for (Object arg : call.getArguments()) {
            if ( first ) first = false;
            else sb.append(", ");
            sb.append(translate(arg, kToJava));
        }
        sb.append(")");
        return sb.toString();
        //return translate((Object)call, kToJava);
    }

    public static Parameter translateParameter(gov.nasa.jpl.ae.event.Parameter<?> p) {
        if ( p == null ) return null;
        String type = p.getType() == null ? null : p.getType().getSimpleName();
        Parameter pp =
                new Parameter(p.getName(),
                                                             type,
                                                             null);
        return pp;
    }

    protected static Map<String, gov.nasa.jpl.ae.event.Parameter<?>> constructorParameters(KtoJava kToJava, ParameterListener event) {
        // Constructors determine parameters -- the rest are dependencies
        List<ConstructorDeclaration> ctors =
                EventXmlToJava.getConstructors(event.getClass().getSimpleName(), kToJava.getClassData());
        if ( ctors == null || ctors.isEmpty() ) {
            ctors = EventXmlToJava.getConstructors(event.getClass().getCanonicalName(), kToJava.getClassData());
        }
        if ( ctors != null && ctors.size() > 1 ) {
            // TODO -- Warning!  Parameters are unclear!
        }
        Map<String, gov.nasa.jpl.ae.event.Parameter<?>> constructorParameters =
                new LinkedHashMap<String, gov.nasa.jpl.ae.event.Parameter<?>>();
        if ( ctors != null ) {
            for ( ConstructorDeclaration ctor : ctors ) {
                List<japa.parser.ast.body.Parameter> params = ctor.getParameters();
                if ( params != null ) for ( japa.parser.ast.body.Parameter p: params ) {
                    String name = p.getId().getName();
                    gov.nasa.jpl.ae.event.Parameter<?> pp = event.getParameter(name);
                    if ( pp == null ) {
                        // TODO -- error!
                    } else {
                        constructorParameters.put(name, pp);
                    }
                }
            }
        }
        return constructorParameters;
    }

}
