package gov.nasa.jpl.kservices.k2apgen;

public class Parameter {
    String name = null;
    String type = null;
    String value = null;

    public Parameter(String name) {
        this.name = name;
    }

    public Parameter(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        String p;
        if ( type == null || type.length() == 0 ) {
            p = name + " = " + valueToString() + ";";
        } else {
            p = name + ": " + type + " default to " + valueToString() + ";";
        }
        return p;
    }

    public String valueToString() {
        if ( ( type != null && type.toLowerCase().equals("string") ) ||
             ( type == null && value != null && value.length() > 0 &&
               !Character.isDigit(value.charAt(0)) ) ) {
            return "\"" + value + "\"";
        }
        if ( value == null ) {
            return getDefaultForType( type );
        }
        return value;
    }

    private String getDefaultForType(String type) {
        if ( type == null ) return "null";  // TODO -- this isn't right, is it?
        //  float | integer | string| duration | time
        if ( type.equals("string") ) return "\"\"";
        if ( type.equals("integer") ) return "0";
        if ( type.equals("float") ) return "0.0";
        if ( type.equals("duration") ) return "00:00:01";
        if ( type.equals("time") ) return "2000-001T00:00:00.000";
        return "null";  // TODO -- this isn't right, is it?
    }

}
