package pt.sec.a03.server.resources;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import pt.sec.a03.server.domain.User;
import pt.sec.a03.server.service.UserService;


@Path("/PwServer/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
	
	private UserService userService = new UserService();
	
	@POST
	//TODO receive the pk correctly 
	public Response addUser(@HeaderParam("public-key") String publicKey, @HeaderParam("signature") String signature,
			@HeaderParam("timestamp") String timestamp, @Context UriInfo uriInfo) {
		userService.addUser(publicKey, signature, timestamp);
		return Response.status(Status.CREATED)
					.build();
	}
}
