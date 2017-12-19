package gov.nasa.jpl.kservices.k2apgen;

import gov.nasa.jpl.ae.event.*;
import gov.nasa.jpl.ae.xml.EventXmlToJava;
import gov.nasa.jpl.kservices.KtoJava;
import gov.nasa.jpl.mbee.util.HasName;
import gov.nasa.jpl.mbee.util.Pair;
import gov.nasa.jpl.mbee.util.Utils;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import k.frontend.*;
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
        // TODO -- translate instance from Main event instance after solved.
        String s = translate(ktoJava);

    }

    public APGenModel translate(Model model) {
        // TODO -- model.packageName() -- use package as prefix of on names of contained elements?
        Collection<TopDecl> decls = JavaConversions.asJavaCollection( model.decls() );
        for ( TopDecl d : decls ) {
            translate(d);
        }
        model.decls();
        model.packages();
        model.annotations();
        model.imports();
        return apgenModel;
    }

    public void translate(TopDecl d) {
        if ( d instanceof AnnotationDecl ) {
            translate( (AnnotationDecl)d );
        }
    }
    public void translate(AnnotationDecl d) {

    }

    public void translate(MemberDecl d) {
        if ( d instanceof TypeDecl ) {
            translate( (TypeDecl)d );
        } else if ( d instanceof PropertyDecl ) {
            translate( (PropertyDecl)d );
        } else if ( d instanceof FunDecl ) {
            translate( (FunDecl)d );
        } else if ( d instanceof ConstraintDecl ) {
            translate( (ConstraintDecl)d );
        } else if ( d instanceof ExpressionDecl ) {
            translate( (ExpressionDecl)d );
        }
    }
    public void translate(TypeDecl d) {
    }
    public void translate(PropertyDecl d) {
    }
    public void translate(FunDecl d) {
    }
    public void translate(ExpressionDecl d) {
        translate(d.exp());
    }
    public void translate(Exp d) {
        if ( d instanceof ParenExp ) {
            translate( (ParenExp)d );
        } else if ( d instanceof IdentExp ) {
            translate( (IdentExp)d );
        } else if ( d instanceof DotExp ) {
            translate( (DotExp)d );
        } else if ( d instanceof IndexExp ) {
            translate( (IndexExp)d );
        } else if ( d instanceof ClassExp ) {
            translate( (ClassExp)d );
        } else if ( d instanceof FunApplExp ) {
            translate( (FunApplExp)d );
        } else if ( d instanceof CtorApplExp ) {
            translate( (CtorApplExp)d );
        } else if ( d instanceof IfExp ) {
            translate( (IfExp)d );
        } else if ( d instanceof MatchExp ) {
            translate( (MatchExp)d );
        } else if ( d instanceof MatchCase ) {
            translate( (MatchCase)d );
        } else if ( d instanceof BlockExp ) {
            translate( (BlockExp)d );
        } else if ( d instanceof WhileExp ) {
            translate( (WhileExp)d );
        } else if ( d instanceof ForExp ) {
            translate( (ForExp)d );
        } else if ( d instanceof BinExp ) {
            translate( (BinExp)d );
        } else if ( d instanceof UnaryExp ) {
            translate( (UnaryExp)d );
        } else if ( d instanceof QuantifiedExp ) {
            translate( (QuantifiedExp)d );
        } else if ( d instanceof TupleExp ) {
            translate( (TupleExp)d );
        } else if ( d instanceof CollectionRangeExp ) {
            translate( (CollectionRangeExp)d );
        } else if ( d instanceof CollectionComprExp ) {
            translate( (CollectionComprExp)d );
        } else if ( d instanceof LambdaExp ) {
            translate( (LambdaExp)d );
        } else if ( d instanceof ReturnExp ) {
            translate( (ReturnExp)d );
        } else if ( d instanceof BreakExp ) {
            translate( (BreakExp)d );
        } else if ( d instanceof ContinueExp ) {
            translate( (ContinueExp)d );
        } else if ( d instanceof ResultExp ) {
            translate( (ResultExp)d );
        } else if ( d instanceof StarExp ) {
            translate( (StarExp)d );
        } else if ( d instanceof Argument ) {
            translate( (Argument)d );
        } else if ( d instanceof Literal ) {
            translate( (Literal)d );
        }
    }
    public void translate(ParenExp d) {
    }
    public void translate(IdentExp d) {
    }
    public void translate(ClassExp d) {
    }
    public void translate(CallApplExp d) {
        if (d instanceof FunApplExp) {
            translate((FunApplExp) d);
        } else if (d instanceof CtorApplExp) {
            translate((CtorApplExp) d);
        } else {
            // ???!!
        }
    }
    public void translate(FunApplExp d) {
    }
    public void translate(CtorApplExp d) {
    }

    public void translate(IfExp d) {
    }
    public void translate(MatchExp d) {
    }
    public void translate(MatchCase d) {
    }
    public void translate(BlockExp d) {
    }
    public void translate(WhileExp d) {
    }
    public void translate(ForExp d) {
    }
    public void translate(BinExp d) {
    }
    public void translate(UnaryExp d) {
    }
    public void translate(QuantifiedExp d) {
    }
    public void translate(TupleExp d) {
    }
    public void translate(CollectionRangeExp d) {
    }
    public void translate(CollectionComprExp d) {
    }
    public void translate(LambdaExp d) {
    }
    public void translate(ReturnExp d) {
    }
    public void translate(BreakExp d) {
    }
    public void translate(ContinueExp d) {
    }
    public void translate(ResultExp d) {
    }
    public void translate(StarExp d) {
    }
    public void translate(Argument d) {
        if ( d instanceof NamedArgument ) {
            translate((NamedArgument)d);
        } else if ( d instanceof PositionalArgument ) {
            translate((PositionalArgument)d);
        }
    }
    public void translate(NamedArgument d) {
    }
    public void translate(Literal d) {
        if (d instanceof IntegerLiteral) {
            translate((IntegerLiteral) d);
        } else if (d instanceof RealLiteral) {
            translate((RealLiteral) d);
        } else if (d instanceof CharacterLiteral) {
            translate((CharacterLiteral) d);
        } else if (d instanceof StringLiteral) {
            translate((StringLiteral) d);
        } else if (d instanceof BooleanLiteral) {
            translate((BooleanLiteral) d);
        } else if (d instanceof NullLiteral) {
            translate((NullLiteral) d);
        } else if (d instanceof ThisLiteral) {
            translate((ThisLiteral) d);
        }
    }
    public void translate(IntegerLiteral d) {
    }
    public void translate(RealLiteral d) {
    }
    public void translate(CharacterLiteral d) {
    }
    public void translate(StringLiteral d) {
    }
    public void translate(BooleanLiteral d) {
    }
    public void translate(NullLiteral d) {
    }
    public void translate(ThisLiteral d) {
    }


    public Model getModel() {
        if ( model == null && ktoJava != null ) {
            model = ktoJava.model();
        }
        return model;
    }


    public static String translate(KtoJava kToJava) {
        Pair<Activity, List<Resource>> p = translateDeclaration(kToJava.mainEvent, kToJava);
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
            String val = translate(durDep.getExpression());
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
            String val = translate(d.getExpression());
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
            Pair<Resource, String> pair = translate(c);
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
            decomposition.append("if (" + translate(c) + ") {\n");
        }
        Vector<EventInvocation> invocations = elaborationRule.getEventInvocations();
        for ( EventInvocation invocation : invocations ) {
            String invString = translate(invocation, kToJava);
            if ( c != null ) {
//                invString = indent(invString, 4);
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

    public static Pair<Resource, String> translate(ConstraintExpression c) {
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
        String val = "(" + translate(c) + ") ? \"true\" : \"false\"";
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
            return translate((Expression)o);
        }
        if ( o instanceof HasName ) {
            return "" + ((HasName)o).getName();
        }
        return "" + o;
    }

    public static String translate(Expression<?> expression) {
        // TODO -- need to rename startTime, endTime, and maybe duration
        return "" + expression;
    }

    public static gov.nasa.jpl.kservices.k2apgen.Parameter translateParameter(gov.nasa.jpl.ae.event.Parameter<?> p) {
        gov.nasa.jpl.kservices.k2apgen.Parameter pp =
                new gov.nasa.jpl.kservices.k2apgen.Parameter(p.getName(),
                                                             p.getType().getSimpleName(),
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
