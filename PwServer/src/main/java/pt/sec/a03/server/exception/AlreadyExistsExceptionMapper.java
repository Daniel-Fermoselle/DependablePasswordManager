package pt.sec.a03.server.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import pt.sec.a03.server.domain.ErrorMessage;


public class AlreadyExistsExceptionMapper implements ExceptionMapper<AlreadyExistsException>{


	@Override
	public Response toResponse(AlreadyExistsException exception) {
		ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), 409, "https://github.com/Daniel-Fermoselle/DependablePasswordManager");
		return Response.status(Status.CONFLICT)
				.entity(errorMessage)
				.build();
	}

}
