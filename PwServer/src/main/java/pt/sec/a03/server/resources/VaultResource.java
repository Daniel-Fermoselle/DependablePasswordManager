package pt.sec.a03.server.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.ManagedAsync;
import pt.sec.a03.common_classes.AuthLink;
import pt.sec.a03.common_classes.CommonTriplet;
import pt.sec.a03.server.MyApplication;
import pt.sec.a03.server.domain.Triplet;
import pt.sec.a03.server.service.VaultService;

import java.security.PrivateKey;

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
    private static final String AUTH_LINK_SIG = "auth-signature";
    private static final String ACK_HEADER_NAME = "ack";

    private AuthLink authLink = new AuthLink();
    private VaultService vaultService = new VaultService();


    @POST
    @ManagedAsync
    public void addPassword(@Suspended final AsyncResponse asyncResponse,
                            @HeaderParam(AUTH_LINK_SIG) String authSig,
                            @HeaderParam(PUBLIC_KEY_HEADER_NAME) String publicKey,
                            @HeaderParam(SIGNATURE_HEADER_NAME) String signature,
                            @HeaderParam(NONCE_HEADER_NAME) String wts,
                            CommonTriplet t, @Context UriInfo uriInfo) {

        System.out.println("Received Post packet addPassword");
        authLink.deliver(publicKey, authSig, signature, wts, t);

        String[] response = vaultService.put(publicKey, signature, wts, t.getHashPassword(), t.getPassword(), t.getUsername(), t.getDomain());

        asyncResponse.resume(Response.status(Status.CREATED)
                .header(SIGNATURE_HEADER_NAME, response[0])
                .header(NONCE_HEADER_NAME, response[1])
                .build());
    }

    @GET
    @ManagedAsync
    public void getPassword(@Suspended final AsyncResponse asyncResponse,
                                @HeaderParam(PUBLIC_KEY_HEADER_NAME) String publicKey,
                                @HeaderParam(SIGNATURE_HEADER_NAME) String stringSig,
                                @HeaderParam(NONCE_HEADER_NAME) String nonce,
                                @HeaderParam(DOMAIN_HEADER_NAME) String domain,
                                @HeaderParam(USERNAME_HEADER_NAME) String username) {

        System.out.println("Received Get packet getPassword");
        String[] response = vaultService.get(publicKey, username, domain, nonce, stringSig);
        CommonTriplet triplet = new CommonTriplet();
        triplet.setPassword(response[3]);

        asyncResponse.resume(Response.status(Status.OK)
                .header(SIGNATURE_HEADER_NAME, response[1])
                .header(NONCE_HEADER_NAME, response[0])
                .header(HASH_PASSWORD_HEADER_NAME, response[2])
                .entity(triplet)
                .build());
    }
}
