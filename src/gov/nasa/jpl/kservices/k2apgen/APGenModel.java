package gov.nasa.jpl.kservices.k2apgen;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class APGenModel {
    Map<String, Activity> activities = new LinkedHashMap<String, Activity>();
    Map<String, ActivityInstance> activityInstances = new LinkedHashMap<String, ActivityInstance>();
    Map<String, Resource> resources = new LinkedHashMap<String, Resource>();
    Map<String, Constraint> constraints = new LinkedHashMap<String, Constraint>();
    Map<String, Function> functions = new LinkedHashMap<String, Function>();
    Map<String, Parameter> parameters = new LinkedHashMap<String, Parameter>();
    Map<String, Parameter> instanceParameters = new LinkedHashMap<String, Parameter>();

    public ActivityInstance addActivityInstance(String n, String t) {
        ActivityInstance a = new ActivityInstance();
        a.id = n;
        a.typeName = t;
        a.type = activities.get(t);
        if (a.type == null) {
            a.type = activities.get(n);
        }
        activityInstances.put(a.getID(), a);
        return a;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Date d = new Date();
        sb.append("apgen version \"generated_" +
                  d.toString().replaceAll("[^A-Za-z0-9_]+", "_") + "\"\n\n");
        sb.append("\n# PARAMETERS\n\n");
        for ( Parameter p : parameters.values() ) {
            String ps = p.toGlobalString();
            if ( ps != null ) {
                sb.append(ps);
                sb.append("\n\n");
            }
        }
        sb.append("\n# ACTIVITIES\n\n");
        for ( Activity a : activities.values() ) {
            String as = a.toString();
            if ( as != null ) {
                sb.append(as);
                sb.append("\n\n");
            }
        }
        sb.append("\n# RESOURCES\n\n");
        for ( Resource r : resources.values() ) {
            String rs = r.toString();
            if ( rs != null ) {
                sb.append(rs);
                sb.append("\n\n");
            }
        }
        sb.append("\n# CONSTRAINTS\n\n");
        for ( Constraint c : constraints.values() ) {
            String cs = c.toString();
            if ( cs != null ) {
                sb.append(cs);
                sb.append("\n\n");
            }
        }
        sb.append("\n# FUNCTIONS\n\n");
        for ( Function f : functions.values() ) {
            String fs = f.toString();
            if ( fs != null ) {
                sb.append(fs);
                sb.append("\n\n");
            }
        }
        sb.append("\n# INSTANCE PARAMETERS\n\n");
        for ( Parameter p : instanceParameters.values() ) {
            String ps = p.toString();
            if ( ps != null ) {
                sb.append(ps);
                sb.append("\n\n");
            }
        }
        sb.append("\n# ACTIVITY INSTANCES\n\n");
        for ( ActivityInstance a : activityInstances.values() ) {
            String as = a.toString();
            if ( as != null ) {
                sb.append(as);
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }
}
