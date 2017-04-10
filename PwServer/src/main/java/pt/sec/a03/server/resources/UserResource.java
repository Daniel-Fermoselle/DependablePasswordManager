package pt.sec.a03.server.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import pt.sec.a03.server.service.UserService;


@Path("/PwServer/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
	
    private static final String PUBLIC_KEY_HEADER_NAME = "public-key";
    private static final String SIGNATURE_HEADER_NAME = "signature";
    private static final String NONCE_HEADER_NAME = "nonce-value";

    private UserService userService = new UserService();

    @POST
    public Response addUser(@HeaderParam(PUBLIC_KEY_HEADER_NAME) String publicKey,
                            @HeaderParam(SIGNATURE_HEADER_NAME) String signature,
                            @HeaderParam(NONCE_HEADER_NAME) String nonce) {

        System.out.println("Received Post packet addUser");
        String [] response = userService.addUser(publicKey, signature, nonce);

        return Response.status(Status.CREATED)
                .header(SIGNATURE_HEADER_NAME, response[0])
                .header(NONCE_HEADER_NAME, response[1])
                .build();
    }

    @GET
    public Response getUserMetaInfo(@HeaderParam("public-key") String publicKey) {
        System.out.println("Received Get packet getMetainfo");

        String[] response = userService.getUserMetaInfo(publicKey);
        return Response.status(Status.OK)
                .header(NONCE_HEADER_NAME, response[0])
                .header(SIGNATURE_HEADER_NAME, response[1])
                .build();
    }

}
