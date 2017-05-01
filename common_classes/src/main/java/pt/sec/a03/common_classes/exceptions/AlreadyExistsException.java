package pt.sec.a03.common_classes.exceptions;

public class AlreadyExistsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2558244749227270776L;

	public AlreadyExistsException(String message) {
		super(message);
	}

}
