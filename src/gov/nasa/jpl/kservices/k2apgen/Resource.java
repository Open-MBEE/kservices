package gov.nasa.jpl.kservices.k2apgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Resource {

    public enum Behavior { _abstract, consumable, nonconsumable, state }

    String name = null;
    List<String> arrayIndices = new ArrayList<String>();
    String type = null;
    Behavior behavior = Behavior.state;

    // Attributes
    String units = null;
    String interpolation = null;  // 0 | 1 | yes | no
    String resolution = null;  // duration format
    Map<String, String> otherAttributes = new TreeMap<>();

    List<Parameter> parameters = new ArrayList<Parameter>();

    List<String> states = new ArrayList<String>();

    String profile = null;

    String usage = null;

    // TODO -- move this to utility class -- it's copied from Activity
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
        sb.append("resource " + name + ": " + behaviorToString(behavior));
        if ( type != null ) sb.append( " " + type );
        sb.append("\n");
        sb.append("    begin\n");

        sb.append("        attributes\n");
        String attrString = attributesToString();
        if ( attrString == null || attrString.isEmpty()) {
            sb.append("            ();\n");
        } else {
            sb.append(indent(attrString, 12));
        }

        sb.append("        parameters\n");
        if ( parameters.isEmpty() ) {
            sb.append("            ();\n");
        } else {
            for (Parameter p : parameters) {
                sb.append(indent(p + "\n", 12));
            }
        }

        if ( states != null && !states.isEmpty() ) {
            sb.append("        states\n");
            for ( String s : states ) {
                sb.append(indent(s + "\n", 12));
            }
        }

        if ( profile != null && !profile.isEmpty() ) {
            sb.append("        profile\n");
            sb.append(indent(profile + "\n", 12));
        }

        if ( usage != null && !usage.isEmpty() ) {
            sb.append("        usage\n");
            sb.append(indent(usage+ "\n", 12));
        }

        sb.append("    end resource " + name + "\n");

        return sb.toString();
    }

    public String attributesToString() {
        if ( units == null && interpolation == null && resolution == null && otherAttributes.isEmpty() ) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        if ( units != null ) {
            sb.append("\"Units\" = \"" + units + "\";\n");
        }
        if ( interpolation != null && !interpolation.isEmpty() ) {
            String q = Character.isDigit(interpolation.charAt(0)) ? "\"" : "";
            sb.append("\"Interpolation\" = " + q + interpolation + q + ";\n");
        }
        if ( resolution != null ) {
            sb.append("\"Resolution\" = " + resolution + ";\n");
        }
        for (Map.Entry e : otherAttributes.entrySet()) {
            sb.append("\"" + e.getKey() + "\" = \"" + e.getValue() + "\";\n");
        }
        return sb.toString();
    }

    public static String behaviorToString(Behavior b) {
        if ( b == Behavior._abstract ) return "abstract";
        return "" + b;
    }
}
