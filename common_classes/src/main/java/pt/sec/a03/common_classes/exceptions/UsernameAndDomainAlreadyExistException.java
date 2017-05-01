package pt.sec.a03.common_classes.exceptions;

public class UsernameAndDomainAlreadyExistException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1551564804992170865L;

	public UsernameAndDomainAlreadyExistException(String message) {
		super(message);
	}

}
