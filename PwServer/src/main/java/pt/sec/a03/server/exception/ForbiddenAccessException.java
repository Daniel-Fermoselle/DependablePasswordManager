package pt.sec.a03.server.exception;

public class ForbiddenAccessException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6027002373308255020L;

	public ForbiddenAccessException(String message) {
		super(message);
	}


}
