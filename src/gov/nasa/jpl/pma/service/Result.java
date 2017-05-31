package gov.nasa.jpl.pma.service;

import java.util.ArrayList;

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
}
