package pt.sec.a03.common_classes;

import pt.sec.a03.crypto.Crypto;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import java.security.*;
import java.util.HashMap;

public class AuthLink {

    private static final String HASH_DOMAIN_IN_MAP = "domain";
    private static final String HASH_USERNAME_IN_MAP = "username";
    private static final String PASSWORD_IN_MAP = "password";
    private static final String HASH_PASSWORD_IN_MAP = "hash-password";

    private static final String PUBLIC_KEY_HEADER_NAME = "public-key";
    private static final String SIGNATURE_HEADER_NAME = "signature";
    private static final String NONCE_HEADER_NAME = "nonce-value";
    private static final String HASH_PASSWORD_HEADER_NAME = "hash-password";
    private static final String DOMAIN_HEADER_NAME = "domain";
    private static final String USERNAME_HEADER_NAME = "username";
    private static final String AUTH_LINK_SIG = "auth-signature";
    private static final String ACK_HEADER_NAME = "ack";

    private Bonrr bonrr;

    public AuthLink(Bonrr bonrr){
        this.bonrr = bonrr;
    }

    public AuthLink(){}

    public void send(PrivateKey cliPrivKey, PublicKey cliPubKey, String uriToSend, PublicKey publicKey, byte[] sig, int wts, HashMap<String, byte[]> infoToSend) {
        CommonTriplet commonTriplet = new CommonTriplet(Crypto.encode(infoToSend.get(PASSWORD_IN_MAP)),
                Crypto.encode(infoToSend.get(HASH_USERNAME_IN_MAP)), Crypto.encode(infoToSend.get(HASH_DOMAIN_IN_MAP)));

        Client client = ClientBuilder.newClient();
        WebTarget vault = client.target("http://" + uriToSend + "/PwServer/").path("vault");

        byte[] authSig = makeSignature(Crypto.encode(sig), wts, infoToSend, cliPrivKey);

        vault.request()
                .header(AUTH_LINK_SIG, Crypto.encode(authSig))
                .header(PUBLIC_KEY_HEADER_NAME, Crypto.encode(cliPubKey.getEncoded()))
                .header(SIGNATURE_HEADER_NAME, Crypto.encode(sig))
                .header(NONCE_HEADER_NAME, wts + "")
                .header(HASH_PASSWORD_HEADER_NAME, Crypto.encode(infoToSend.get(HASH_PASSWORD_IN_MAP)))
                .async().post(Entity.json(commonTriplet), new InvocationCallback<Response>() {
                    @Override
                    public void completed(Response response) {
                        System.out.println("Response of save password status code " + response.getStatus() + " received.");

                        //Verify signature
                        verifySignature(publicKey, response.getHeaderString(AUTH_LINK_SIG),
                                response.getHeaderString(NONCE_HEADER_NAME), response.getHeaderString(ACK_HEADER_NAME));

                        bonrr.addToAckList(response.getHeaderString(ACK_HEADER_NAME),
                                Integer.parseInt(response.getHeaderString(NONCE_HEADER_NAME)));
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        System.out.println("Invocation failed in save password.");
                        throwable.printStackTrace();
                    }
                });
    }

    private byte[] makeSignature(String signature, int wts, HashMap<String, byte[]> infoToSend, PrivateKey cliPrivKey) {
        String toSign = signature + wts;
        toSign = toSign + Crypto.encode(infoToSend.get(HASH_DOMAIN_IN_MAP));
        toSign = toSign + Crypto.encode(infoToSend.get(HASH_USERNAME_IN_MAP));
        toSign = toSign + Crypto.encode(infoToSend.get(PASSWORD_IN_MAP));
        toSign = toSign + Crypto.encode(infoToSend.get(HASH_PASSWORD_IN_MAP));
        try {
            return Crypto.makeDigitalSignature(toSign.getBytes(), cliPrivKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private void verifySignature(PublicKey publicKey, String signature, String akc, String wts) {
        String toVerify = akc + wts;
        try {
            if(!Crypto.verifyDigitalSignature(Crypto.decode(signature), toVerify.getBytes(), publicKey)){
                //throw new InvalidSignatureException();
                throw new RuntimeException("Invalid Signature on Bonrr");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
