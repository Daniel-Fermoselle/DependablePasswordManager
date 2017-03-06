package pt.sec.a03.server.resources;

import java.net.URI;
import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import pt.sec.a03.server.domain.Triplet;
import pt.sec.a03.server.service.VaultService;

@Path("/vault")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VaultResource {
	
	private VaultService vaultService = new VaultService();
	
	@POST
	//TODO
	public Triplet addPassword(Triplet t, @Context UriInfo uriInfo) throws SQLException {
		/*vaultService.put(publicKey, t.getPassword(), t.getUsername(), t.getDomain());
		
		Need to call getPassword to get triplet to send
		URI uri = uriInfo.getAbsolutePathBuilder().path(newId).build();
		return Response.created(uri)
				.entity(newMessage)
				.build();
				*/
		return new Triplet();
	}
	
}
