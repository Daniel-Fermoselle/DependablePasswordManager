package pt.sec.a03.client_lib.exception;

public class InvalidReceivedPasswordException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7119018816057438561L;

	public InvalidReceivedPasswordException(String message) {
		super(message);
	}

}
