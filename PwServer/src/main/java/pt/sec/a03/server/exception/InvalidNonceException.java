package pt.sec.a03.server.exception;

public class InvalidNonceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -979033759936970719L;

	
	public InvalidNonceException(String message) {
		super(message);
	}
}
