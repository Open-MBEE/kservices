package gov.nasa.jpl.kservices.k2apgen;

import java.util.LinkedHashMap;
import java.util.Map;

public class APGenModel {
    Map<String, Activity> activities = new LinkedHashMap<String, Activity>();
    Map<String, Resource> resources = new LinkedHashMap<String, Resource>();
    Map<String, Function> functions = new LinkedHashMap<String, Function>();
    //Map<String, Constraint> resources = new LinkedHashMap<String, Constraint>();
    Map<String, Parameter> parameters = new LinkedHashMap<String, Parameter>();
    Map<String, Parameter> instanceParameters = new LinkedHashMap<String, Parameter>();
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n# FUNCTIONS\n\n");
        for ( Function f : functions.values() ) {
            String fs = f.toString();
            sb.append(fs);
            sb.append("\n\n");
        }
        sb.append("\n# RESOURCES\n\n");
        for ( Resource r : resources.values() ) {
            String rs = r.toString();
            sb.append(rs);
            sb.append("\n\n");
        }
        sb.append("\n# ACTIVITIES\n\n");
        for ( Activity a : activities.values() ) {
            String as = a.toString();
            sb.append(as);
            sb.append("\n\n");
        }
        sb.append("\n# PARAMETERS\n\n");
        for ( Parameter p : parameters.values() ) {
            String ps = p.toString();
            sb.append(ps);
            sb.append("\n\n");
        }
        sb.append("\n# INSTANCE PARAMETERS\n\n");
        for ( Parameter p : instanceParameters.values() ) {
            String ps = p.toString();
            sb.append(ps);
            sb.append("\n\n");
        }
        return sb.toString();
    }
}
