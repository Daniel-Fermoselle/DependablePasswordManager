package pt.sec.a03.server.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import pt.sec.a03.server.domain.Triplet;
import pt.sec.a03.server.service.VaultService;

@Path("/vault")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VaultResource {
	
	private VaultService vaultService = new VaultService();
	
	@POST
	public Response addPassword(@HeaderParam("public-key") String publicKey, Triplet t, @Context UriInfo uriInfo,
			@HeaderParam("signature") String signature,@HeaderParam("timestamp") String timestamp,
			@HeaderParam("hash-password") String hashPw) {
		vaultService.put(publicKey, signature, timestamp, hashPw, t.getPassword(), t.getUsername(), t.getDomain());
		return Response.status(Status.CREATED)
				.build();
	}
	
	@GET
	public Triplet getPassword(@HeaderParam("public-key") String publicKey, @HeaderParam("domain") String domain, @HeaderParam("username") String username) {
		System.out.println("PublicKey: " + publicKey + " Domain: " + domain + " Username: " + username);
		Triplet password = vaultService.get(publicKey, username, domain);
		return password;
	}
	
}
