package pt.sec.a03.server.exception;

public class InvalidSignatureException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 178964194422489405L;

	public InvalidSignatureException(String message) {
		super(message);
	}
	
}
