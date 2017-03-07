package pt.sec.a03.server.resources;

import java.sql.SQLException;
import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import pt.sec.a03.server.domain.Triplet;
import pt.sec.a03.server.service.VaultService;

@Path("/vault")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VaultResource {
	
	private VaultService vaultService = new VaultService();
	
	@POST
	//TODO PUT , POST or BOTH?
	public Response addPassword(@HeaderParam("public-key") String publicKey, Triplet t, @Context UriInfo uriInfo) throws Exception {
		Triplet newTriplet = vaultService.put(publicKey, t.getPassword(), t.getUsername(), t.getDomain());
		URI uri = uriInfo.getAbsolutePathBuilder().path(newTriplet.getTripletID() + "").build();
		return Response.created(uri)
				.entity(newTriplet)
				.build();
	}
	
	@GET
	public Triplet getPassword(@HeaderParam("public-key") String publicKey, @HeaderParam("domain") String domain, @HeaderParam("username") String username) 
			throws SQLException {
		System.out.println("PublicKey: " + publicKey + " Domain: " + domain + " Username: " + username);
		Triplet password = vaultService.get(publicKey, username, domain);
		return password;
	}
	
}
