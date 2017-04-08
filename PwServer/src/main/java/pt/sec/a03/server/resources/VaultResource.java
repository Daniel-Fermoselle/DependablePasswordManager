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

import pt.sec.a03.common_classes.CommonTriplet;
import pt.sec.a03.server.domain.Triplet;
import pt.sec.a03.server.service.VaultService;

@Path("/vault")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VaultResource {
	
	private VaultService vaultService = new VaultService();
	
	@POST
	public Response addPassword(@HeaderParam("public-key") String publicKey,
							@HeaderParam("signature") String signature,
							@HeaderParam("timestamp") String timestamp,
							@HeaderParam("hash-password") String hashPw, 
							Triplet t, @Context UriInfo uriInfo) {
		String[] answer = vaultService.put(publicKey, signature, timestamp, hashPw, t.getPassword(), t.getUsername(), t.getDomain());
		return Response.status(Status.CREATED)
				.header("signature", answer[0])
				.header("timestamp", answer[1])
				.build();
	}
	
	@GET
	public Response getPassword(@HeaderParam("public-key") String publicKey, 
							@HeaderParam("signature") String stringSig,
							@HeaderParam("timestamp") String stringTS,
							@HeaderParam("domain") 	  String domain, 
							@HeaderParam("username")  String username) {
		String[] content = vaultService.get(publicKey, username, domain, stringTS, stringSig);
		CommonTriplet triplet = new CommonTriplet();
		triplet.setPassword(content[3]);
		return Response.status(Status.OK)
				.header("signature", content[1])
				.header("timestamp", content[0])
				.header("hash-password", content[2])
				.entity(triplet)
				.build();
	}
	
}
