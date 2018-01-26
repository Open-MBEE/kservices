package gov.nasa.jpl.kservices.k2apgen;

import java.util.*;

public class Activity {
    String name = null;
    Map<String, String> attributes = new TreeMap<>();
    LinkedHashMap<String, Parameter> parameters = new LinkedHashMap<String, Parameter>();
    Map<String, Parameter> creation = new TreeMap<>();
    StringBuffer modeling = new StringBuffer();
    StringBuffer decomposition  = new StringBuffer();

    /**
     * Assign the given value to an existing attribute, parameter, or creation
     * variable that has the input name.
     *
     * @param varName
     * @param value
     * @return whether a variable of the same name was found and assigned
     */
    public boolean assignValue(String varName, String value) {
        Map<String, Parameter> vars = null;
        if ( parameters.containsKey(varName) ) {
            vars = parameters;
        } else if ( creation.containsKey(varName) ) {
            vars = creation;
        }
        if ( vars != null ) {
            gov.nasa.jpl.kservices.k2apgen.Parameter p = vars.get(varName);
            p.value = value;
            return true;
        }
        if ( attributes.containsKey(varName) ) {
            attributes.put(varName, value);
            return true;
        }
        return false;
    }

    protected static Set<String> suppressedParams = new TreeSet<String>() {
        {
            add("begin");
            add("Duration");
            add("end");
//            add("startTime");
//            add("duration");
//            add("endTime");
        }
    };

    protected boolean appendParameterToString(StringBuffer sb, Parameter p) {
        if ( p.value == null && suppressedParams.contains(p.name) ) {
            return false;
        }
        String pStr = p.toString();
        if ( pStr != null && pStr.length() > 0 ) {
            sb.append(Util.indent(pStr + "\n", 12));
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("activity type " + name + "\n");
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
            for (Parameter p : parameters.values()) {
                boolean added = appendParameterToString(sb, p);
                if ( added ) ++numParms;
            }
        }
        if ( numParms == 0 ) {
            sb.append("            ();\n");
        }
        numParms = 0;
        if ( !creation.isEmpty() ) {
            sb.append("        creation\n");
            for (Parameter p : creation.values()) {
                boolean added = appendParameterToString(sb, p);
                if ( added ) ++numParms;
            }
            if ( numParms == 0 ) {
                sb.append("            ();\n");
            }
        }
        if ( modeling.length() > 0 ) {
            sb.append("        modeling\n");
            sb.append(Util.indent(modeling.toString(), 12));
        }
        if ( decomposition.length() > 0 ) {
            sb.append("        nonexclusive_decomposition\n");
            sb.append(Util.indent(decomposition.toString(), 12));
        }
        sb.append("    end activity type " + name + "\n");
        return sb.toString();
    }
}
