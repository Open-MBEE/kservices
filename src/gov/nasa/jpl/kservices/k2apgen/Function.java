package gov.nasa.jpl.kservices.k2apgen;

import java.util.Map;
import java.util.TreeMap;

public class Function {
    String name = null;
    Map<String, Parameter> parameters = new TreeMap<>();
    String body = null;

    // TODO -- move this to utility class
    public static String indent(String s, int numSpaces) {
        if ( s == null ) return "";
        StringBuffer k = new StringBuffer();
        for (int i=0; i<numSpaces; i++) {
            k.append(" ");
        }
        String indented = s.replaceAll("^", k.toString()).replaceAll("\n", "\n" + k.toString());
        if ( indented.endsWith(k.toString()) ) {
            indented = indented.replaceFirst(k.toString() + "$", "");
        }
        return indented;
    }

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
                sb.append(indent("" + p, 8));
                sb.append("\n");
                ++numParms;
            }
        }
        if ( numParms == 0 ) {
            sb.append("            ();\n");
        }
        sb.append("    {");
        sb.append(indent(body, 8));
        sb.append("    }");
        return sb.toString();
    }
}
