package pt.sec.a03.server.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pt.sec.a03.server.domain.ErrorMessage;

@Provider
public class ForbiddenAccessExceptionMapper implements ExceptionMapper<ForbiddenAccessException>{

	@Override
	public Response toResponse(ForbiddenAccessException exception) {
		ErrorMessage errorMessage = new ErrorMessage(exception.getMessage(), 403, "https://github.com/Daniel-Fermoselle/DependablePasswordManager");
		return Response.status(Status.FORBIDDEN)
				.entity(errorMessage)
				.build();
	}

}
