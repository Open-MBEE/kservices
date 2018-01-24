package gov.nasa.jpl.kservices.k2apgen;

import gov.nasa.jpl.ae.event.*;
import gov.nasa.jpl.ae.xml.EventXmlToJava;
import gov.nasa.jpl.kservices.KtoJava;
import gov.nasa.jpl.mbee.util.Debug;
import gov.nasa.jpl.mbee.util.HasName;
import gov.nasa.jpl.mbee.util.Pair;
import gov.nasa.jpl.mbee.util.Utils;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import k.frontend.*;
import scala.Option;
import scala.collection.JavaConversions;

import java.util.*;
import java.util.List;
import java.util.Collection;

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

    KtoJava ktoJava = null;
    Model model = null;
    APGenModel apgenModel = new APGenModel();
    public boolean addingToDecomposition = false;

    public Map<String, List<Exp>> constructorCalls = new LinkedHashMap<String, List<Exp>>();

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
        System.out.println(k2apgen.aaf);
        System.out.println(k2apgen.apf);

    }

    public void translate() {
        apgenModel = translate(getModel());
        addInheritedMembers(getModel());
        cleanupForConstructors();
        // TODO -- translate instance from Main event instance after solved.
        String s = translateInstances(ktoJava);
        System.out.println("\n=========   k apgenModel   =========\n" + apgenModel);
        System.out.println("=========   end k apgenModel   =========\n");
        System.out.println("\n=========   bae apgenModel   =========\n" + s);
        System.out.println("=========   end bae apgenModel   =========\n");
        //System.out.println("this KToAPGen = " + this);
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
        HashSet<String> seen = new HashSet<String>();
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
        LinkedHashMap<String, gov.nasa.jpl.kservices.k2apgen.Parameter> newParameters
                = new LinkedHashMap<String, gov.nasa.jpl.kservices.k2apgen.Parameter>(parentAct.parameters);
        newParameters.putAll(childAct.parameters);
        childAct.parameters = newParameters;
    }

    public APGenModel translate(Model model) {
        // TODO -- model.packageName() -- use package as prefix of on names of contained elements?
        Collection<PackageDecl> packages = JavaConversions.asJavaCollection( model.packages() );
        for ( PackageDecl p: packages ) {
            translate(p.model());
        }
        Collection<EntityDecl> entityDecls = JavaConversions.asJavaCollection( model.entityDecls(model) );
        for ( EntityDecl e: entityDecls ) {
            translate(e, apgenModel);
        }
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

    private void translate(EntityDecl e, Object parent) {
        Activity activity = new Activity();
        activity.name = kToApgenClassName(e.ident());
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
        LinkedHashMap<String, gov.nasa.jpl.kservices.k2apgen.Parameter> oldParameters = activity.parameters;
        activity.parameters = new LinkedHashMap<>();
        for ( Argument arg : args ) {
            if ( arg instanceof NamedArgument ) {  // This should always be true.
                NamedArgument narg = (NamedArgument) arg;
                String name = narg.ident();
                if (oldParameters.containsKey(name)) {
                    gov.nasa.jpl.kservices.k2apgen.Parameter p = oldParameters.get(name);
                    activity.parameters.put(name, p);
                    oldParameters.remove(name);
                } else {
                    if ( name.equals("startTime") || name.equals("endTime") || name.equals("duration") ) {
                        gov.nasa.jpl.kservices.k2apgen.Parameter p =
                                new gov.nasa.jpl.kservices.k2apgen.Parameter(name, "time", null);
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
        if ( d instanceof EntityDecl ) {
            translate((EntityDecl) d, parent);
        } else if ( d instanceof TypeDecl ) {
            translate( (TypeDecl)d, parent );
        } else if ( d instanceof PropertyDecl ) {
            translate( (PropertyDecl)d, parent );
        } else if ( d instanceof FunDecl ) {
            translate( (FunDecl)d, parent );
        } else if ( d instanceof ConstraintDecl ) {
            translate( (ConstraintDecl)d, parent );
        } else if ( d instanceof ExpressionDecl ) {
            translate( (ExpressionDecl)d, parent );
        }
    }

    // This is like typedef: "type myType[T] = Map[T, Int]"
    public void translate(TypeDecl d, Object parent) {
        // TODO -- ???
        // TODO -- maybe keep a list of aliases and never explicitly translate but always replace with type on rhs.
        // TODO -- APGen has typedefs for structs and lists.
    }
    public void translate(PropertyDecl d, Object parent) {
        Exp expr = get(d.expr());
        if ( expr != null && containsConstructorCall( expr ) ) {
            addDecomposition(expr, parent);
            return;
        }
        String exprString = translate(expr, parent);
        gov.nasa.jpl.kservices.k2apgen.Parameter pp =
                new gov.nasa.jpl.kservices.k2apgen.Parameter(d.name(),
                        translate(d.ty(), d.multiplicity()),
                        exprString);
        if ( parent instanceof APGenModel ) {
            ((APGenModel)parent).parameters.put(pp.name, pp);
        } else if ( parent instanceof Activity ) {
            ((Activity)parent).parameters.put(pp.name, pp);
        } else if ( parent instanceof Function ) {
            ((Function)parent).parameters.put(pp.name, pp);
        }
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

    public String translate(Type ty, Option<Multiplicity> multiplicity) {
        Multiplicity m = get(multiplicity);
        if ( m != null && m.exp1() != null && ((m.exp2() != null && m.exp2().get() != null && !m.exp2().get().toJavaString().equals("1")) || !m.exp1().toJavaString().equals("1"))) {
            return ty.toJavaString() + "[]";
        }
        return ty.toJavaString();
    }

    public void translate(FunDecl d, Object parent) {
        Function f = new Function();
        f.name = d.ident();
        Collection<Param> params = JavaConversions.asJavaCollection( d.params() );

        if ( parent instanceof APGenModel ) {
            ((APGenModel)parent).functions.put(f.name, f);
        } else if ( parent instanceof Activity ) {
            // APGen activities can't have functions.
            apgenModel.functions.put(f.name, f);
        } else {
            // TODO -- ERROR
        }
    }
    public String translate(ConstraintDecl d, Object parent) {
        // Create a timeline and constraint
        if ( d.toString().contains("Timepoint.set") ) {
            return "";
        }
        Resource r = new Resource();
        if (get(d.name()) != null ) {
            r.name = d.name().get();
        } else {
            r.name = "res_" + ("" + d).replaceAll("[^0-9A-Za-z_][^0-9A-Za-z_]*", "_");
        }
        r.otherAttributes.put("Description", "resource for constraint, " + d);
        r.otherAttributes.put("Legend", "constraint");
        r.otherAttributes.put("Color", "Green");
        r.type = "string";
        r.behavior = Resource.Behavior.state;
        r.states = Utils.newList("false", "true");
        r.profile = "true";
        r.parameters.add(new gov.nasa.jpl.kservices.k2apgen.Parameter("State", "string", "true"));
        r.usage = "State";

        // effect on resource in activity
        String vName = "constraint_" + r.name.replace("res_", "");
        String val = "(" + translate(d.exp(), parent) + ") ? \"true\" : \"false\"";
        gov.nasa.jpl.kservices.k2apgen.Parameter cp =
                new gov.nasa.jpl.kservices.k2apgen.Parameter(vName, "string", val);
        String modeling = cp.toString() + "\n" +
                "use " + r.name +"(vName) from begin to end\n";
        if ( parent instanceof APGenModel ) {
            ((APGenModel) parent).resources.put(r.name, r);
            // TODO -- ERROR -- what to do with modeling?  return as String?
        } else if ( parent instanceof Activity ) {
            ((Activity)parent).modeling.append(modeling);
            apgenModel.resources.put(r.name, r);
        } else {
            // TODO -- ERROR
        }
        return modeling;
        //activity.modeling.append(cp.toString() + "\n");
        //activity.modeling.append("use " + r.name +"(vName) from begin to end\n");
    }

    public void translate(ExpressionDecl d, Object parent) {
        String s = translate(d.exp(), parent);
        if ( parent instanceof APGenModel ) {
            // TODO -- ERROR -- No place to put an expression
        } else if ( parent instanceof Activity ) {
            // TODO -- ERROR -- No place to put an expression
        } else {
            // TODO -- ERROR?
        }
    }

    public String translate(Exp d, Object parent) {
        if ( d == null ) return null;
        if ( d instanceof ParenExp ) {
            return translate( (ParenExp)d, parent );
        } else if ( d instanceof IdentExp ) {
            return translate( (IdentExp)d );
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
    public String translate(IdentExp d) {
        if ( "startTime".equals(d.ident()) ) return "begin";
        if ( "endTime".equals(d.ident()) ) return "end";
        return d.toJavaString();
    }
    public String translate(DotExp d, Object parent) {
        StringBuffer sb = new StringBuffer();
        sb.append(translate(d.exp(), parent));
        sb.append(".");
        sb.append(d.ident());
        return sb.toString();
    }
    public String translate(IndexExp d, Object parent) { return d.toJavaString(); }
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
            a.decomposition.append( ctorStr + "\n" );// + " at start;\n");
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

        StringBuffer sb = new StringBuffer();
        String n = translate(d.exp1(), parent);
        if ( isCtor ) {
            n = kToApgenClassName(n);
        }
        sb.append(n + "(");
        Collection<Argument> args = JavaConversions.asJavaCollection(d.arguments());
        boolean first = true;
        for (Argument arg : args) {
            if ( first ) first = false;
            else sb.append(", ");
            sb.append(translate(arg, parent));
        }
        sb.append(")");
        if ( isCtor ) {
            sb.append(" at start");
        }
        return sb.toString();
    }

    public String translate(CtorApplExp d, Object parent) {
        if ( addingToDecomposition ) {
            // create decmposition
            addDecomposition(d, parent);
            return null;
        }
        StringBuffer sb = new StringBuffer();
        String n = d.ty().toJavaString();
        n = kToApgenClassName(n);
        sb.append(n + "(");
        sb.append(d.ty().toJavaString() + "(");
        Collection<Argument> args = JavaConversions.asJavaCollection(d.arguments());
        boolean first = true;
        for (Argument arg : args) {
            if ( first ) first = false;
            else sb.append(", ");
            sb.append(translate(arg, parent));
        }
        sb.append(") at start");
        return sb.toString();
    }

    public String translate(IfExp d, Object parent) {
        StringBuffer sb = new StringBuffer();
        sb.append("if (");
        sb.append( translate(d.cond(), parent) );
        sb.append(") {\n    ");
        sb.append( translate(d.trueBranch(), parent) );
        Exp fb = get(d.falseBranch());
        if ( fb != null ) {
            sb.append(";\n} else {\n    ");
            sb.append(translate(d.falseBranch(), parent));
        }
        sb.append(";\n}");

        return sb.toString();
    }

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
        StringBuffer sb = new StringBuffer();
        sb.append(translate(d.exp1(), parent));
        sb.append(" " + d.op() + " ");
        sb.append(translate(d.exp2(), parent));
        return sb.toString();
    }

    public String translate(UnaryExp d, Object parent) {
        return d.toJavaString();
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
        return d.toJavaString();
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
    public String translate(DurationLiteral d) {
        return d.toString();
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
        StringBuffer sb = new StringBuffer();
        for ( ActivityInstance i : list ) {
            sb.append(i.toString());
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
        activity.name = event.getClass().getCanonicalName().replaceAll("[.]", "_");

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
                gov.nasa.jpl.kservices.k2apgen.Parameter pp = translateParameter(p);
                activity.parameters.put(pp.name, pp);
            }
        }

        for ( gov.nasa.jpl.ae.event.Parameter<?> p : event.getParameters() ) {
            String aeName = p.getName();
            if ( aeName != null && !aeName.isEmpty() && !ctorParams.containsKey( aeName ) ) {
                gov.nasa.jpl.kservices.k2apgen.Parameter pp = translateParameter(p);
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

    public List<ActivityInstance> translateInstance(Event event) {
        List<ActivityInstance> instances = new ArrayList<ActivityInstance>();
        if ( event == null ) return instances;
        String n = event.getName();
        String t = event.getClass().getSimpleName();
        ActivityInstance a = apgenModel.addActivityInstance(n, t);
        instances.add(a);
//        HashSet<HasEvents> seen = new HashSet<HasEvents>();
//        seen.add(this);
        DurativeEvent durEvent = null;
        if ( event instanceof HasEvents ) {
            Set<Event> events = ((HasEvents) event).getEvents(true, null);
            for ( Event e : events) {
                instances.addAll(translateInstance(e));
            }
        }
        return instances;
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

    public static Pair<Resource, String> translate(ConstraintExpression c, KtoJava kToJava) {
        // Create a timeline and constraint
        Resource r = new Resource();
        if (!Utils.isNullOrEmpty(c.getName()) ) {
            r.name = c.getName();
        } else {
            r.name = "res_" + ("" + c).replaceAll("[^0-9A-Za-z_][^0-9A-Za-z_]*", "_");
        }
        r.otherAttributes.put("Description", "resource for constraint, " + c);
        r.otherAttributes.put("Legend", "constraint");
        r.otherAttributes.put("Color", "Green");
        r.type = "string";
        r.behavior = Resource.Behavior.state;
        r.states = Utils.newList("false", "true");
        r.profile = "true";
        r.parameters.add(new gov.nasa.jpl.kservices.k2apgen.Parameter("State", "string", "true"));
        r.usage = "State";

        // effect on resource in activity
        String vName = r.name.replace("res_", "constraint_");
        String val = "(" + translate(c.getExpression(), kToJava) + ") ? \"true\" : \"false\"";
        gov.nasa.jpl.kservices.k2apgen.Parameter cp =
                new gov.nasa.jpl.kservices.k2apgen.Parameter(vName, "string", val);
        String modeling = cp.toString() + "\n" +
                          "use " + r.name +"(vName) from begin to end\n";
        //activity.modeling.append(cp.toString() + "\n");
        //activity.modeling.append("use " + r.name +"(vName) from begin to end\n");

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
        // TODO -- need to rename startTime, endTime, and maybe duration
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
        if ( "startTime".equals(parameter.getName()) ) return "begin";
        if ( "endTime".equals(parameter.getName()) ) return "end";
        if ( "duration".equals(parameter.getName()) ) return "Duration";
        return translate((Object)parameter, kToJava);
    }

    public static String translateFunctionName(String callName) {
        //if ( "".equals(callName) )
        return callName;
    }

    public static String translate(ConstructorCall call, KtoJava kToJava) {
        // TODO
        return translate((Call)call, kToJava);
    }

    public static String translate(Call call, KtoJava kToJava) {
        // TODO
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

    public static gov.nasa.jpl.kservices.k2apgen.Parameter translateParameter(gov.nasa.jpl.ae.event.Parameter<?> p) {
        if ( p == null ) return null;
        String type = p.getType() == null ? null : p.getType().getSimpleName();
        gov.nasa.jpl.kservices.k2apgen.Parameter pp =
                new gov.nasa.jpl.kservices.k2apgen.Parameter(p.getName(),
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
                List<Parameter> params = ctor.getParameters();
                if ( params != null ) for ( Parameter p: params ) {
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
