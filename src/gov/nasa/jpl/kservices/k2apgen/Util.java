package gov.nasa.jpl.kservices.k2apgen;

public class Util {
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

}
