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

    private static final String PUBLIC_KEY_HEADER_NAME = "public-key";
    private static final String SIGNATURE_HEADER_NAME = "signature";
    private static final String NONCE_HEADER_NAME = "nonce-value";
    private static final String HASH_PASSWORD_HEADER_NAME = "hash-password";
    private static final String DOMAIN_HEADER_NAME = "domain";
    private static final String USERNAME_HEADER_NAME = "username";

    private VaultService vaultService = new VaultService();

    @POST
    public Response addPassword(@HeaderParam(PUBLIC_KEY_HEADER_NAME) String publicKey,
                                @HeaderParam(SIGNATURE_HEADER_NAME) String signature,
                                @HeaderParam(NONCE_HEADER_NAME) String nonce,
                                @HeaderParam(HASH_PASSWORD_HEADER_NAME) String hashPw,
                                Triplet t, @Context UriInfo uriInfo) {

        vaultService.put(publicKey, signature, nonce, hashPw, t.getPassword(), t.getUsername(), t.getDomain());

        return Response.status(Status.CREATED)
                .build();
    }

    @GET
    public Response getPassword(@HeaderParam(PUBLIC_KEY_HEADER_NAME) String publicKey,
                                @HeaderParam(SIGNATURE_HEADER_NAME) String stringSig,
                                @HeaderParam(NONCE_HEADER_NAME) String nonce,
                                @HeaderParam(DOMAIN_HEADER_NAME) String domain,
                                @HeaderParam(USERNAME_HEADER_NAME) String username) {

        String[] content = vaultService.get(publicKey, username, domain, nonce, stringSig);
        CommonTriplet triplet = new CommonTriplet();
        triplet.setPassword(content[3]);

        return Response.status(Status.OK)
                .header(SIGNATURE_HEADER_NAME, content[1])
                .header(NONCE_HEADER_NAME, content[0])
                .header(HASH_PASSWORD_HEADER_NAME, content[2])
                .entity(triplet)
                .build();
    }

}
