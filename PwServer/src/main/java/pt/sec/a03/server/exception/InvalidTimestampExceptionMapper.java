package pt.sec.a03.server.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import pt.sec.a03.server.domain.ErrorMessage;

@Provider
public class InvalidTimestampExceptionMapper implements ExceptionMapper<InvalidTimestampException> {

	@Override
	public Response toResponse(InvalidTimestampException ex) {
		ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 400, "https://github.com/Daniel-Fermoselle/DependablePasswordManager");
		return Response.status(Status.BAD_REQUEST)
				.entity(errorMessage)
				.build();
	}
	
}
