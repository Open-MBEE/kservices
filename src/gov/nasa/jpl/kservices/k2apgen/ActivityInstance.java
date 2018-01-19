package gov.nasa.jpl.kservices.k2apgen;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * Stub class for an activity instance
 */
public class ActivityInstance {

    protected static Map<String, Integer> autoIdCounters = new HashMap<String, Integer>();

    String id = null;
    String name = null;
    Activity type = null;
    String typeName = null;
    String abstractable = null;
    Map<String, String> attributes = new TreeMap<>();
    Map<String, Parameter> parameters = new TreeMap<>();

    public Activity getType() { return type; }

    public String getTypeName() {
        if (typeName == null && getType() != null ) {
            typeName =getType().name;
        }
        return typeName;
    }

    public String getName() {
        if ( name == null ) {
            name = getTypeName();
        }
        return name;
    }

    public String getID() {
        if ( id == null ) {
            String n = getName();
            if ( !autoIdCounters.keySet().contains(n) ) {
                autoIdCounters.put(n, 0);
            }
            int ct = autoIdCounters.get(n);
            id = n + "_" + String.format("%6d", ct);
            autoIdCounters.put(n, ct + 1);
        }
        return id;
    }

    @Override
    public String toString() {
        /**
         *
         # activity instance scenario_BradTest of type scenario_BradTest id scenario_BradTest_1
         #     begin
         #         decomposed into IS_ON_1;
         #         attributes
         #             “Start” = 1996-279T01:23:24.794;
         #             "Duration" = 134134;
         #         parameters
         #           (scenario_BradTest_actbegin,
         #            scenario_BradTest_actend);
         #     end activity instance scenario_BradTest
         #
         # activity instance IS_ON of type IS_ON id IS_ON_1
         #     begin
         #         abstractable into scenario_BradTest_1;
         #         decomposed into TO_ON_1;
         #         attributes
         #             “Start” = 1996-279T02:00:09.436;
         #         parameters
         #             (???);
         #     end activity instance IS_ON
         */
        // TODO!!!
        StringBuffer sb = new StringBuffer();
        sb.append("activity instance " + getName() + " of type " + getTypeName() + " id " + getID() + "\n");
        sb.append("    begin\n");
        sb.append("        attributes\n");
        if ( attributes.isEmpty() ) {
            sb.append("            ();\n");
        } else {
            for (Map.Entry<String, String> e : attributes.entrySet()) {
                String q = e.getKey().equals("Start") || e.getKey().equals("Duration") ? "" : "\"";
                sb.append(Util.indent("\"" + e.getKey() + "\" = " + q + e.getValue() + q + ";\n", 12));
            }
        }
        sb.append("        parameters\n");
        int numParms = 0;
        if ( !parameters.isEmpty() ) {
            boolean first = true;
            sb.append(Util.indent("(", 12));
            for (Parameter p : parameters.values()) {
                if ( p.value == null &&
                        // TODO -- make a static set instead of comparing each.
                        ( p.name.equals("startTime") || p.name.equals("begin") ||
                                p.name.equals("duration") || p.name.equals("Duration") ||
                                p.name.equals("endTime") || p.name.equals("end") ) ) {
                    continue;
                }
                if (first) first = false;
                else sb.append(", ");
                sb.append(p.valueToString());
                ++numParms;
            }
            sb.append(")\n");
        }
        if ( numParms == 0 ) {
            sb.append("            ();\n");
        }
        numParms = 0;
        sb.append("    end activity instance " + getName() + "\n");
        return sb.toString();
    }

}
