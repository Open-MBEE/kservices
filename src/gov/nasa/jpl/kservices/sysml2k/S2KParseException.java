package gov.nasa.jpl.kservices.sysml2k;

/**
 * Thrown by SysMLtoK operations when input is not understood.
 */
@SuppressWarnings("serial")
public class S2KParseException extends S2KException {

	public S2KParseException() {
	}

	public S2KParseException(String message) {
		super(message);
	}

	public S2KParseException(Throwable cause) {
		super(cause);
	}

	public S2KParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public S2KParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
