package pt.sec.a03.common_classes.exception;

public class InvalidReceivedPasswordException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7119018816057438561L;

	public InvalidReceivedPasswordException(String message) {
		super(message);
	}

}
