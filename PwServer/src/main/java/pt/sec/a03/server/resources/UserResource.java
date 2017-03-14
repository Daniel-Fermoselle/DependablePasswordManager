package pt.sec.a03.server.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import pt.sec.a03.server.domain.User;
import pt.sec.a03.server.service.UserService;


@Path("users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
	
	private UserService userService = new UserService();
	
	@GET
	//TODO receive the pk correctly 
	public User getUserByPK(@HeaderParam("public-key") String publicKey) {
		return userService.getUserByPK(publicKey);
	}
	
	@GET
	@Path("/{userId}")
	public User getUserByID(@PathParam("userId") String id) {
		return userService.getUserByID(id);
	}
	
	@POST
	//TODO receive the pk correctly 
	public Response addUser(@HeaderParam("public-key") String publicKey, @HeaderParam("signature") String signature,
			@HeaderParam("timestamp") String timestamp) {
		userService.addUser(publicKey, signature, timestamp);
		return Response.status(Status.CREATED)
					.build();
	}
	
	@PUT//Depricated needs signature
	@Path("/{userId}")
	//TODO receive the pk correctly 
	public Response updateUserWithID(@PathParam("userId") String id, @HeaderParam("public-key") String publicKey) {
		userService.updateUserWithID(id, publicKey);
		return Response.status(Status.OK)
					.build();
	}
}
