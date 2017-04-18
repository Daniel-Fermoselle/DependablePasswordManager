package pt.sec.a03.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/hello")
@Produces(MediaType.TEXT_PLAIN)
public class HelloWorldResource {

	@GET
	//TODO receive the pk correctly 
	public String hello() {
		return "Hello!";
	}
	
}
