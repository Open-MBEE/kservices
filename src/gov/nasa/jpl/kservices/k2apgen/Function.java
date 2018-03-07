package gov.nasa.jpl.kservices.k2apgen;

import java.util.LinkedHashMap;
import java.util.Map;

public class Function {
    String name = null;
    Map<String, Parameter> parameters = new LinkedHashMap<>();
    String body = null;

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("function " + name);
        sb.append("(");
        if ( !parameters.isEmpty() ) {
            boolean first = true;
            for (String p : parameters.keySet()) {
                if ( first ) first = false;
                else sb.append(", ");
                sb.append(p);
            }
        }
        sb.append(")");
        sb.append("\n");
        sb.append("    parameters\n");
        int numParms = 0;
        if ( !parameters.isEmpty() ) {
            for (Parameter p : parameters.values()) {
                sb.append(Util.indent("" + p, 8));
                sb.append("\n");
                ++numParms;
            }
        }
        if ( numParms == 0 ) {
            sb.append("            ();\n");
        }
        sb.append("    {\n");
        sb.append(Util.indent(body, 8));
        sb.append("\n    }");
        return sb.toString();
    }
}
