package gov.nasa.jpl.kservices.k2apgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Resource {

    public enum Behavior { _abstract, consumable, nonconsumable, state }

    String name = null;
    List<String> arrayIndices = new ArrayList<String>();
    String type = "string";
    Behavior behavior = Behavior.state;

    // Attributes
    String units = null;
    String interpolation = null;  // 0 | 1 | yes | no
    String resolution = null;  // duration format
    Map<String, String> otherAttributes = new TreeMap<>();

    List<Parameter> parameters = new ArrayList<Parameter>();

    List<String> states = new ArrayList<String>();
    List<String> stateValues = new ArrayList<String>();

    String profile = null;

    String usage = null;

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("resource " + name + ": " + behaviorToString(behavior));
        if ( type != null ) sb.append( " " + type );
        sb.append("\n");
        sb.append("    begin\n");

        String attrString = attributesToString();
        if ( attrString == null || attrString.isEmpty()) {
            sb.append("        # no attributes\n");
            //sb.append("            ();\n");
        } else {
            sb.append("        attributes\n");
            sb.append(Util.indent(attrString, 12));
            sb.append("\n");
        }

        if ( parameters.isEmpty() ) {
            sb.append("        # no parameters\n");
            //sb.append("            ();\n");
        } else {
            sb.append("        parameters\n");
            for (Parameter p : parameters) {
                sb.append(Util.indent("" + p, 12));
                sb.append("\n");
            }
        }

        if ( states != null && !states.isEmpty() ) {
            sb.append("        states\n");
            StringBuffer ssb = new StringBuffer();
            boolean first = true;
            for ( String s : states ) {
                if ( first ) first = false;
                else ssb.append(", ");
                //ssb.append("\"" + s + "\"");
                ssb.append( s );
            }
            ssb.append(";");
            sb.append(Util.indent(ssb.toString(), 12));
            sb.append("\n");
        }

        if ( profile != null && !profile.isEmpty() ) {
            sb.append("        profile\n");
            //sb.append(Util.indent("\"" + profile + "\";", 12));
            sb.append(Util.indent(profile + ";", 12));
            sb.append("\n");
        }

        if ( usage != null && !usage.isEmpty() ) {
            sb.append("        usage\n");
            sb.append(Util.indent(usage + ";", 12));
            sb.append("\n");
        }

        sb.append("\n    end resource " + name + "\n");

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
