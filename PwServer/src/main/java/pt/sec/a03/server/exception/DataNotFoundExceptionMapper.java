package pt.sec.a03.server.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import pt.sec.a03.server.domain.ErrorMessage;



public class DataNotFoundExceptionMapper implements ExceptionMapper<DataNotFoundException> {

	@Override
	public Response toResponse(DataNotFoundException ex) {
		ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(), 404, "https://github.com/Daniel-Fermoselle/DependablePasswordManager");
		return Response.status(Status.NOT_FOUND)
				.entity(errorMessage)
				.build();
	}
}
