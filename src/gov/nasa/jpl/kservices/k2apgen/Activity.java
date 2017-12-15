package gov.nasa.jpl.kservices.k2apgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Activity {
    String name = null;
    Map<String, String> attributes = new TreeMap<>();
    List<Parameter> parameters = new ArrayList<>();
    List<Parameter> creation = new ArrayList<>();
    StringBuffer modeling = new StringBuffer();
    StringBuffer decomposition  = new StringBuffer();

    // TODO -- move this to utility class
    public static String indent(String s, int numSpaces) {
        if ( s == null ) return "";
        StringBuffer k = new StringBuffer();
        for (int i=0; i<numSpaces; i++) {
            k.append(" ");
        }
        return s.replaceAll("^", k.toString()).replaceAll("\n", "\n" + k.toString());
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
            for (Map.Entry e : attributes.entrySet()) {
                sb.append(indent("\"" + e.getKey() + "\" = \"" + e.getValue() + "\";\n", 12));
            }
        }
        sb.append("        parameters\n");
        if ( parameters.isEmpty() ) {
            sb.append("            ();\n");
        } else {
            for (Parameter p : parameters) {
                sb.append(indent(p + "\n", 12));
            }
        }
        if ( !creation.isEmpty() ) {
            sb.append("        creation\n");
            for (Parameter p : creation) {
                sb.append(indent(p + "\n", 12));
            }
        }
        if ( modeling.length() > 0 ) {
            sb.append("        modeling\n");
            sb.append(indent(modeling.toString(), 12));
        }
        if ( decomposition.length() > 0 ) {
            sb.append("        nonexclusive_decomposition\n");
            sb.append(indent(decomposition.toString(), 12));
        }
        sb.append("    end activity type " + name + "\n");
        return sb.toString();
    }
}
