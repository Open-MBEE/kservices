package gov.nasa.jpl.kservices.k2apgen;

import gov.nasa.jpl.ae.event.ConstraintExpression;
import gov.nasa.jpl.ae.event.Dependency;
import gov.nasa.jpl.ae.event.DurativeEvent;
import gov.nasa.jpl.ae.event.Expression;
import gov.nasa.jpl.ae.xml.EventXmlToJava;
import gov.nasa.jpl.kservices.KtoJava;
import gov.nasa.jpl.mbee.util.HasName;
import gov.nasa.jpl.mbee.util.Pair;
import gov.nasa.jpl.mbee.util.Utils;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.Parameter;
import k.frontend.EntityDecl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
public class KToAPGen extends KtoJava {
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

    public KToAPGen(String k, String pkgName, boolean processStdoutAndStderr) {
        super(k, pkgName, processStdoutAndStderr);
    }

    public KToAPGen(String k, String pkgName, boolean translate, boolean processStdoutAndStderr) {
        super(k, pkgName, translate, processStdoutAndStderr);
    }

    public static void main(String[] args) {
        KtoJava kToJava = KtoJava.runMain(args);
        String apgen = translate(kToJava);
        System.out.println(apgen);
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

    public static Pair< Activity, List< Resource > > translateDeclaration(DurativeEvent event, KtoJava kToJava) {
        Activity activity = new Activity();
        activity.name = event.getClass().getCanonicalName().replaceAll("[.]", "_");

        // Duration
        Dependency<?> durDep = event.getDependency(event.duration);
        if ( durDep != null ) {
            String val = translate(durDep.getExpression());
            activity.attributes.put("Duration", val);
        }

        // Constructors determine parameters -- the rest are dependencies
        Map<String, gov.nasa.jpl.ae.event.Parameter<?>> ctorParams =
                constructorParameters(kToJava, event);
        if (ctorParams != null && !ctorParams.isEmpty()) {
            for ( gov.nasa.jpl.ae.event.Parameter<?> p : ctorParams.values() ) {
                gov.nasa.jpl.kservices.k2apgen.Parameter pp = translateParmater(p);
                activity.parameters.put(pp.name, pp);
            }
        }

        for ( gov.nasa.jpl.ae.event.Parameter<?> p : event.getParameters() ) {
            String aeName = p.getName();
            if ( aeName != null && !aeName.isEmpty() && !ctorParams.containsKey( aeName ) ) {
                gov.nasa.jpl.kservices.k2apgen.Parameter pp = translateParmater(p);
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
            // Create a timeline and constraint
            Resource r = new Resource();
            resources.add(r);
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
            activity.modeling.append(cp.toString() + "\n");
            activity.modeling.append("use " + r.name +"(vName) from begin to end\n");

            // TODO -- APGen constraint to check that resource == "true"
        }

        Pair< Activity, List< Resource > > p = new Pair< Activity, List< Resource > >(activity, resources);
        return p;
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

    public static gov.nasa.jpl.kservices.k2apgen.Parameter translateParmater(gov.nasa.jpl.ae.event.Parameter<?> p) {
        gov.nasa.jpl.kservices.k2apgen.Parameter pp =
                new gov.nasa.jpl.kservices.k2apgen.Parameter(p.getName(),
                                                             p.getType().getSimpleName(),
                                                             null);
        return pp;
    }

    protected static Map<String, gov.nasa.jpl.ae.event.Parameter<?>> constructorParameters(KtoJava kToJava, DurativeEvent event) {
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
                for ( Parameter p: params ) {
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
        return null;
    }
}
