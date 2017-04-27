package pt.sec.a03.server.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ManagedAsync;
import pt.sec.a03.server.service.UserService;


@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final String PUBLIC_KEY_HEADER_NAME = "public-key";
    private static final String SIGNATURE_HEADER_NAME = "signature";
    private static final String NONCE_HEADER_NAME = "nonce-value";
    private static final String BONRR_HEADER_NAME = "bonrr";


    private UserService userService = new UserService();

    @POST
    @ManagedAsync
    public void addUser(@Suspended final AsyncResponse asyncResponse,
                        @HeaderParam(PUBLIC_KEY_HEADER_NAME) String publicKey,
                        @HeaderParam(SIGNATURE_HEADER_NAME) String signature,
                        @HeaderParam(NONCE_HEADER_NAME) String nonce) {
        System.out.println("Received Post packet addUser");

        String[] response = userService.addUser(publicKey, signature, nonce);
        asyncResponse.resume(Response.status(Status.CREATED)
                .header(SIGNATURE_HEADER_NAME, response[0])
                .header(NONCE_HEADER_NAME, response[1])
                .build());

    }

    @Path("/meta")
    @GET
    @ManagedAsync
    public void getUserMetaInfo(@Suspended final AsyncResponse asyncResponse,
                                @HeaderParam(PUBLIC_KEY_HEADER_NAME) String publicKey){
        System.out.println("Received Get packet getMetainfo");

        String[] response = userService.getUserMetaInfo(publicKey);
         asyncResponse.resume(Response.status(Status.OK)
                .header(NONCE_HEADER_NAME, response[0])
                .header(SIGNATURE_HEADER_NAME, response[1])
                .build());
        System.out.println("Sent Metadata");
    }

    @Path("/bonrr")
    @GET
    @ManagedAsync
    public void getBonrrID(@Suspended final AsyncResponse asyncResponse,
                                @HeaderParam(PUBLIC_KEY_HEADER_NAME) String publicKey,
                                @HeaderParam(SIGNATURE_HEADER_NAME) String signature,
                                @HeaderParam(NONCE_HEADER_NAME) String nonce) {
        System.out.println("Received Get packet getBonrrID");

        String[] response = userService.getBonrrID(publicKey, signature, nonce);
        System.out.println("Before sending bonrrID " + response[2]);
        asyncResponse.resume(Response.status(Status.OK)
                .header(SIGNATURE_HEADER_NAME, response[0])
                .header(NONCE_HEADER_NAME, response[1])
                .header(BONRR_HEADER_NAME, response[2])
                .build());
        System.out.println("Sent bonrrID " + response[2]);

    }

}
