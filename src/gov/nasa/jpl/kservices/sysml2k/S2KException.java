package gov.nasa.jpl.kservices.sysml2k;

/**
 * Thrown by SysMLtoK when something goes wrong.
 */
@SuppressWarnings("serial")
public class S2KException extends Exception {
  public S2KException() {
  }
  
  public S2KException(String message) {
    super(message);
  }
  
  public S2KException(Throwable cause) {
    super(cause);
  }
  
  public S2KException(String message, Throwable cause) {
     super(message, cause);
  }
  
  public S2KException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
