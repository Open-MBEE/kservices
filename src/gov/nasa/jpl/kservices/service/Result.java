package gov.nasa.jpl.kservices.service;

import java.util.ArrayList;

import gov.nasa.jpl.mbee.util.MoreToString;
import gov.nasa.jpl.mbee.util.Utils;

/**
 * The result of a service request. This would include some object as a result
 * and any error information.
 */
public class Result<T> {
    ArrayList<String> errors;
    T value;
    Class<T> type;
    /**
     * @param errors
     * @param value
     * @param type
     */
    public Result( ArrayList< String > errors, T value, Class< T > type ) {
        super();
        this.errors = errors;
        this.value = value;
        this.type = type;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( MoreToString.Helper.toString( value ) );
        if ( !Utils.isNullOrEmpty( errors ) ) {
            sb.append( "\nERROR: " + MoreToString.Helper.toString( errors ) );
        }
        return sb.toString();
    }
}
