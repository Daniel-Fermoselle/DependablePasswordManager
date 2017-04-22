package pt.sec.a03.common_classes;

import pt.sec.a03.crypto.Crypto;

import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
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

    public AuthLink(Bonrr bonrr) {
        this.bonrr = bonrr;
    }

    public AuthLink() {
    }

    public void send(PrivateKey cliPrivKey, PublicKey cliPubKey, String uriToSend, PublicKey publicKey, byte[] sig, int wts, HashMap<String, byte[]> infoToSend) {
        CommonTriplet commonTriplet = new CommonTriplet(Crypto.encode(infoToSend.get(PASSWORD_IN_MAP)),
                Crypto.encode(infoToSend.get(HASH_USERNAME_IN_MAP)), Crypto.encode(infoToSend.get(HASH_DOMAIN_IN_MAP)),
                Crypto.encode(infoToSend.get(HASH_PASSWORD_IN_MAP)));

        Client client = ClientBuilder.newClient();
        WebTarget vault = client.target("http://" + uriToSend + "/PwServer/").path("vault");

        //Verify Signature
        String toSign = Crypto.encode(sig) + wts;
        toSign = toSign + commonTriplet.getDomain();
        toSign = toSign + commonTriplet.getUsername();
        toSign = toSign + commonTriplet.getPassword();
        toSign = toSign + commonTriplet.getHashPassword();
        byte[] authSig = makeSignature(cliPrivKey, toSign);

        vault.request()
                .header(AUTH_LINK_SIG, Crypto.encode(authSig))
                .header(PUBLIC_KEY_HEADER_NAME, Crypto.encode(cliPubKey.getEncoded()))
                .header(SIGNATURE_HEADER_NAME, Crypto.encode(sig))
                .header(NONCE_HEADER_NAME, wts + "")
                .async().post(Entity.json(commonTriplet), new InvocationCallback<Response>() {
            @Override
            public void completed(Response response) {
                System.out.println("Response of save password status code " + response.getStatus() + " received.");

                String toVerify = response.getHeaderString(NONCE_HEADER_NAME) +
                        response.getHeaderString(ACK_HEADER_NAME);

                //Verify signature
                verifySignature(publicKey, response.getHeaderString(AUTH_LINK_SIG), toVerify);

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

    private byte[] makeSignature(PrivateKey cliPrivKey, String toSign) {
        try {
            return Crypto.makeDigitalSignature(toSign.getBytes(), cliPrivKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private void verifySignature(PublicKey publicKey, String signatureToVer, String signature) {
        try {
            if (!Crypto.verifyDigitalSignature(Crypto.decode(signatureToVer), signature.getBytes(), publicKey)) {
                //TODO throw new InvalidSignatureException();
                throw new RuntimeException("Invalid Signature on Bonrr");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void deliver(String publicKey, String authSig, String signature, String wts, CommonTriplet t) {
        try {
            String toVerify = signature + wts + t.getDomain() + t.getUsername() + t.getPassword() + t.getHashPassword();
            PublicKey pubKey = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));
            verifySignature(pubKey, authSig, toVerify);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
