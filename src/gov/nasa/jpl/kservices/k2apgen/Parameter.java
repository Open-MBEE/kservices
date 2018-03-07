package gov.nasa.jpl.kservices.k2apgen;

import gov.nasa.jpl.mbee.util.Debug;

import java.util.Set;
import java.util.TreeSet;

public class Parameter {
    String name = null;
    String type = null;
    String value = null;

    public Parameter(String name) {
        this.name = name;
    }

    public Parameter(String name, String type, String value) {
        this.name = name;
        this.type = translateTypeString(type);
        this.value = value;
    }

    @Override
    public String toString() {
        if ( !apgenTypes.contains(type) ) {
            Debug.error(true, false, "WARNING! not outputting parameter, " + name + ":" + type);
            return null;
        }
        String p;
        if ( type == null || type.length() == 0 ) {
            p = name + " = " + valueToString() + ";";
        } else {
            p = name + ": " + type + " default to " + valueToString() + ";";
        }
        return p;
    }

    public String toGlobalString() {
        if ( !apgenTypes.contains(type) ) {
            Debug.error(true, false, "WARNING! not outputting parameter, " + name + ":" + type);
            return null;
        }
        String p;
        if ( type != null && type.length() > 0 ) {
            p = "global " + type + " " + name + " = " + value + ";";
        } else {
            p = "global " + name + " = " + value + ";";
        }
        return p;
    }

    public String valueToString() {
        if ( (( type != null && type.toLowerCase().equals("string") ) ||
             ( type == null && value != null && value.length() > 0 &&
               !Character.isDigit(value.charAt(0)) ) ) &&
                (value == null || value.matches("^[0-9A-Za-z ._,]*$")) ) {
            if ( !value.startsWith("endTime") && !value.startsWith("startTime") ) {
                return "\"" + value + "\"";
            }
        }
        if ( value == null ) {
            return getDefaultForType( type );
        }
        return value;
    }

    public static String translateTypeString(String type) {
        String ltype = type.toLowerCase();
        if ( ltype.equals("double") ) return "float";
        if ( ltype.equals("long") ) return "integer";
        if ( apgenTypes.contains(ltype) ) return ltype;
        return type;
    }
    public static Set<String> apgenTypes = new TreeSet<String>() {
        {
            add("string");
            add("integer");
            add("float");
            add("duration");
            add("time");
            add("boolean");
            add("array");
        }
    };

    public static String getDefaultForType(String type) {
        if ( type == null ) return "null";  // TODO -- this isn't right, is it?
        //  float | integer | string| duration | time
        if ( type.equals("string") ) return "\"\"";
        if ( type.equals("integer") ) return "0";
        if ( type.equals("float") ) return "0.0";
        if ( type.equals("duration") ) return "00:00:01";
        if ( type.equals("time") ) return "2000-001T00:00:00.000";
        if ( type.equals("boolean") ) return "false";
        if ( type.equals("array") ) return "[]";
        return "null";  // TODO -- this isn't right, is it?
    }

}
