package gov.nasa.jpl.kservices.k2apgen;

import gov.nasa.jpl.ae.event.Dependency;
import gov.nasa.jpl.ae.event.DurativeEvent;
import gov.nasa.jpl.ae.event.Expression;
import gov.nasa.jpl.ae.xml.EventXmlToJava;
import gov.nasa.jpl.kservices.KtoJava;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.Parameter;
import k.frontend.EntityDecl;

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
        translate(kToJava);
    }

    public static void translate(KtoJava kToJava) {
        Activity act = translateDeclaration(kToJava, kToJava.mainEvent);
        for ( EntityDecl c : kToJava.getTopLevelClasses() ) {

        }
    }

    public static Activity translateDeclaration(KtoJava kToJava, DurativeEvent event) {
        Activity activity = new Activity();
        activity.name = event.getClass().getCanonicalName().replaceAll("[.]", "_");
        Dependency<?> durDep = event.getDependency(event.duration);
        String val = translate(durDep.getExpression());
        activity.attributes.put("Duration", val);

        // Constructors determine parameters -- the rest are dependencies
        Map<String, gov.nasa.jpl.ae.event.Parameter<?>> ctorParams =
                constructorParameters(kToJava, event);
        if (ctorParams != null && !ctorParams.isEmpty()) {
            for ( gov.nasa.jpl.ae.event.Parameter<?> p : ctorParams.values() ) {
                gov.nasa.jpl.kservices.k2apgen.Parameter pp = translateParmater(p);
                activity.parameters.add(pp);
            }
        }
        return activity;
    }

    private static String translate(Expression<?> expression) {
        // TODO -- need to rename startTime, endTime, and maybe duration
        return "" + expression;
    }

    private static gov.nasa.jpl.kservices.k2apgen.Parameter translateParmater(gov.nasa.jpl.ae.event.Parameter<?> p) {
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
