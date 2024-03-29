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
    private static final String DOMAIN_HEADER_NAME = "domain";
    private static final String USERNAME_HEADER_NAME = "username";
    private static final String AUTH_LINK_SIG = "auth-signature";
    private static final String ACK_HEADER_NAME = "ack";
    private static final String BONRR_HEADER_NAME = "bonrr";
    private static final String RID_HEADER_NAME = "rid";
    private static final String RANK_HEADER_NAME = "rank";

    private AuthLink authLink = new AuthLink();
    private VaultService vaultService = new VaultService();

    @POST
    @ManagedAsync
    public void addPassword(@Suspended final AsyncResponse asyncResponse,
                            @HeaderParam(AUTH_LINK_SIG) String authSig,
                            @HeaderParam(PUBLIC_KEY_HEADER_NAME) String publicKey,
                            @HeaderParam(SIGNATURE_HEADER_NAME) String signature,
                            @HeaderParam(NONCE_HEADER_NAME) String wts,
                            @HeaderParam(BONRR_HEADER_NAME) String bonrr,
                            @HeaderParam(RID_HEADER_NAME) String rid,
                            @HeaderParam(RANK_HEADER_NAME) String rank,
                            CommonTriplet t) {

        if(MyApplication.CRASH){
            System.exit(1);
        }

        System.out.println("Received Post packet addPassword");
        authLink.deliverWrite(publicKey, authSig, signature, bonrr, wts, t, rid, rank);

        Triplet triplet = new Triplet(t.getDomain(), t.getUsername(), t.getPassword(), t.getHash(), signature,
                Long.parseLong(wts), Long.parseLong(rid), Long.parseLong(rank));
        String[] response = vaultService.put(publicKey, triplet, bonrr);

        stop();
        
        asyncResponse.resume(Response.status(Status.CREATED)
                .header(AUTH_LINK_SIG, response[0])
                .header(ACK_HEADER_NAME, response[1])
                .header(NONCE_HEADER_NAME, response[2])
                .header(RID_HEADER_NAME, response[3])
                .header(DOMAIN_HEADER_NAME, response[4])
                .header(USERNAME_HEADER_NAME, response[5])
                .build());
    }

    @GET
    @ManagedAsync
    public void getPassword(@Suspended final AsyncResponse asyncResponse,
    							@HeaderParam(AUTH_LINK_SIG) String authSig,
                                @HeaderParam(PUBLIC_KEY_HEADER_NAME) String publicKey,
                                @HeaderParam(RID_HEADER_NAME) String rid,
                                @HeaderParam(DOMAIN_HEADER_NAME) String domain,
                                @HeaderParam(USERNAME_HEADER_NAME) String username,
                                @HeaderParam(BONRR_HEADER_NAME) String bonrr) {

        if(MyApplication.CRASH){
            System.exit(1);
        }

        System.out.println("Received Get packet getPassword");

        authLink.deliverRead(publicKey, authSig, bonrr, rid, domain, username);

        String[] response = vaultService.get(publicKey, username, domain, rid, bonrr);

        CommonTriplet triplet = new CommonTriplet();
        triplet.setDomain(response[4]);
        triplet.setUsername(response[5]);
        triplet.setPassword(response[6]);
        triplet.setHash(response[7]);
        
        stop();

        asyncResponse.resume(Response.status(Status.OK)
                .header(AUTH_LINK_SIG, response[0])
                .header(RID_HEADER_NAME, response[1])
                .header(NONCE_HEADER_NAME, response[2])
                .header(RANK_HEADER_NAME, response[3])
                .header(SIGNATURE_HEADER_NAME, response[8])
                .entity(triplet)
                .build());
    }
    
    private static void stop(){
        if(MyApplication.SLOW_BYZANTINE){
        	try {
				Thread.sleep(MyApplication.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
        }
    }
}
