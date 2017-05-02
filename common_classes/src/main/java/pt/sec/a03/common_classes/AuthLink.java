package pt.sec.a03.common_classes;

import pt.sec.a03.common_classes.exception.InvalidSignatureException;
import pt.sec.a03.crypto.Crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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
	private static final String WTS_IN_MAP = "wts";
	private static final String SIGNATURE_IN_MAP = "signature";
	private static final String RID_IN_MAP = "map-rid";

    private static final String AUTH_LINK_SIG = "auth-signature";
    private static final String PUBLIC_KEY_HEADER_NAME = "public-key";
    private static final String BONRR_HEADER_NAME = "bonrr";
    private static final String RANK_HEADER_NAME = "rank";
    private static final String ACK_HEADER_NAME = "ack";
    private static final String NONCE_HEADER_NAME = "nonce-value";
    private static final String RID_HEADER_NAME = "rid";
    private static final String SIGNATURE_HEADER_NAME = "signature";
    private static final String DOMAIN_HEADER_NAME = "domain";
    private static final String USERNAME_HEADER_NAME = "username";
    private static final String RANK_IN_MAP = "map-rank";

	private static final String FORBIDEN_MSG = "Forbiden operation";
	private static final String BAD_REQUEST_MSG = "Invalid Request";
	private static final String DATA_NOT_FOUND_MSG = "Data Not Found";
	private static final String SERVER_ERROR_MSG = "Internal server error";
    private static final String SIG_TO_VERIFY = "sig-to-verify";

    private Bonrr bonrr;
	private PublicKey publicKey;
	private PrivateKey privateKey;

	public AuthLink() {}

	public AuthLink(Bonrr bonrr, PublicKey cliPubKey, PrivateKey cliPrivKey) {
		this.bonrr = bonrr;
		this.publicKey = cliPubKey;
		this.privateKey = cliPrivKey;
	}

	public void send(PublicKey serverPubKey, String uriToSend, long wts, long rid, long rank,
                     HashMap<String, byte[]> infoToSend, String bonrr) {

		CommonTriplet commonTriplet = new CommonTriplet(Crypto.encode(infoToSend.get(HASH_DOMAIN_IN_MAP)),
				Crypto.encode(infoToSend.get(HASH_USERNAME_IN_MAP)), Crypto.encode(infoToSend.get(PASSWORD_IN_MAP)),
				Crypto.encode(infoToSend.get(HASH_PASSWORD_IN_MAP)));

		Client client = ClientBuilder.newClient();
		WebTarget vault = client.target("http://" + uriToSend + "/PwServer/").path("vault");

		// Verify Signature
		String toSign = Crypto.encode(infoToSend.get(SIGNATURE_IN_MAP)) + bonrr + (wts + "") + (rid + "") + (rank + "");
		toSign = toSign + commonTriplet.getDomain();
		toSign = toSign + commonTriplet.getUsername();
		toSign = toSign + commonTriplet.getPassword();
		toSign = toSign + commonTriplet.getHash();
		byte[] authSig = makeSignature(privateKey, toSign);

		vault.request().header(AUTH_LINK_SIG, Crypto.encode(authSig))
				.header(PUBLIC_KEY_HEADER_NAME, Crypto.encode(publicKey.getEncoded()))
				.header(SIGNATURE_HEADER_NAME, Crypto.encode(infoToSend.get(SIGNATURE_IN_MAP)))
                .header(NONCE_HEADER_NAME, wts + "")
                .header(RID_HEADER_NAME, rid + "")
				.header(BONRR_HEADER_NAME, bonrr)
				.header(RANK_HEADER_NAME, rank).async()
				.post(Entity.json(commonTriplet), new InvocationCallback<Response>() {
					@Override
					public void completed(Response response) {

							System.out.println("Response of save password status code " + response.getStatus()
                                    + " received from " + uriToSend + ".");

							if(checkForErrors(response)){
							    return;
                            }

                            //Get Username and domain
							String encodedHashDomain = response.getHeaderString(DOMAIN_HEADER_NAME);
							String encodedHashUsername = response.getHeaderString(USERNAME_HEADER_NAME);
							String[] userAndDom = decipherUsernameAndDomain(encodedHashDomain, encodedHashUsername);

                            // Verify signature
                            String toVerify = response.getHeaderString(ACK_HEADER_NAME)
                                    + response.getHeaderString(NONCE_HEADER_NAME)
									+ response.getHeaderString(RID_HEADER_NAME)
									+ userAndDom[1] + userAndDom[0];
							verifySignature(serverPubKey, response.getHeaderString(AUTH_LINK_SIG), toVerify);

							AuthLink.this.bonrr.addToAckList(response.getHeaderString(ACK_HEADER_NAME),
									Long.parseLong(response.getHeaderString(NONCE_HEADER_NAME)),
									Long.parseLong(response.getHeaderString(RID_HEADER_NAME)),
									userAndDom[1], userAndDom[0]);

					}

					@Override
					public void failed(Throwable throwable) {
						System.out.println("Invocation failed in save password.");
						throwable.printStackTrace();
					}
				});
	}

    public void send(PublicKey serverPubKey, String uriToSend, long readId, HashMap<String, byte[]> infoToSend, String bonrr) {

		String hashedDomain = Crypto.encode(infoToSend.get(HASH_DOMAIN_IN_MAP));
		String hashedUsername = Crypto.encode(infoToSend.get(HASH_USERNAME_IN_MAP));

		Client client = ClientBuilder.newClient();
		WebTarget vault = client.target("http://" + uriToSend + "/PwServer/").path("vault");

		// Verify Signature
		String toSign = bonrr + readId;
		toSign = toSign + hashedDomain;
		toSign = toSign + hashedUsername;
		byte[] authSig = makeSignature(privateKey, toSign);

		vault.request().header(AUTH_LINK_SIG, Crypto.encode(authSig))
				.header(PUBLIC_KEY_HEADER_NAME, Crypto.encode(publicKey.getEncoded()))
				.header(RID_HEADER_NAME, readId + "")
				.header(BONRR_HEADER_NAME, bonrr)
				.header(DOMAIN_HEADER_NAME, hashedDomain)
				.header(USERNAME_HEADER_NAME, hashedUsername).async()
				.get(new InvocationCallback<Response>() {
					@Override
					public void completed(Response response) {

                            System.out.println("Response of save password status code " + response.getStatus()
                                    + " received from " + uriToSend + ".");

                            if(response.getStatus() == 404 && !AuthLink.this.bonrr.getReading()){
                                AuthLink.this.bonrr.addToReadList(null, readId);
                                return;
                            } else if (checkForErrors(response)){
                                return;
                            }

                            CommonTriplet t = response.readEntity(CommonTriplet.class);
                            String encodedHashDomain = t.getDomain();
                            String encodedHashUsername = t.getUsername();
                            String encodedCipheredPassword = t.getPassword();
                            String encodedCipheredHashPassword = t.getHash();
                            String encodedWriteSig = response.getHeaderString(SIGNATURE_HEADER_NAME);
                            long wts = Long.parseLong(response.getHeaderString(NONCE_HEADER_NAME));
                            long rid = Long.parseLong(response.getHeaderString(RID_HEADER_NAME));
                            long rank = Long.parseLong(response.getHeaderString(RANK_HEADER_NAME));

                            //Decipher username and domain
                            String[] userAndDom = decipherUsernameAndDomain(encodedHashDomain, encodedHashUsername);

                            // Verify authentication link signature
                            String toVerify = (rid + "") + (wts + "") + (rank + "") + userAndDom[1] + userAndDom[0]
                                    + encodedCipheredPassword + encodedCipheredHashPassword + encodedWriteSig;
                            verifySignature(serverPubKey, response.getHeaderString(AUTH_LINK_SIG), toVerify);

                            // Create verify write signature
                            String toVerifyWriteSig = bonrr + (wts + "") + (rank + "") + userAndDom[1] + userAndDom[0]
                                + encodedCipheredPassword + encodedCipheredHashPassword;

                            HashMap<String,String> value = new HashMap<String, String>();
                            value.put(HASH_DOMAIN_IN_MAP, userAndDom[1]);
                            value.put(HASH_USERNAME_IN_MAP, userAndDom[0]);
                            value.put(PASSWORD_IN_MAP, encodedCipheredPassword);
                            value.put(HASH_PASSWORD_IN_MAP, encodedCipheredHashPassword);
                            value.put(WTS_IN_MAP, wts + "");
                            value.put(SIGNATURE_IN_MAP, encodedWriteSig);
                            value.put(RID_IN_MAP, rid + "");
                            value.put(RANK_IN_MAP, rank + "");
                            value.put(SIG_TO_VERIFY, toVerifyWriteSig);
							AuthLink.this.bonrr.addToReadList(value, rid);

					}

					@Override
					public void failed(Throwable throwable) {
						System.out.println("Invocation failed in save password.");
						throwable.printStackTrace();
					}
				});
	}

	public void deliverWrite(String publicKey, String authSig, String signature, String bonrr, String wts,
							 CommonTriplet t, String rid, String rank) {
		try {
			String toVerify = signature + bonrr + wts + rid + rank + t.getDomain() + t.getUsername() + t.getPassword()
					+ t.getHash();
			PublicKey pubKey = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));
			verifySignature(pubKey, authSig, toVerify);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

    public void deliverRead(String publicKey, String authSig, String bonrr, String rid, String domain, String username) {
        try {
            String toVerify = bonrr + rid + domain + username;
            PublicKey pubKey = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));
            verifySignature(pubKey, authSig, toVerify);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());

        }
    }

    private boolean checkForErrors(Response response) {
        if (response.getStatus() == 400) {
            System.out.println(BAD_REQUEST_MSG);
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("400", "400");
            AuthLink.this.bonrr.addToErrorList(map);
            return true;
        } else if (response.getStatus() == 403) {
            System.out.println(FORBIDEN_MSG);
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("403", "403");
            AuthLink.this.bonrr.addToErrorList(map);
            return true;
        } else if (response.getStatus() == 404) {
            System.out.println(DATA_NOT_FOUND_MSG);
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("404", "404");
            AuthLink.this.bonrr.addToErrorList(map);
            return true;
        } else if (response.getStatus() == 500) {
            System.out.println(SERVER_ERROR_MSG);
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("500", "500");
            AuthLink.this.bonrr.addToErrorList(map);
            return true;
        } else{
            return false;
        }
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
				throw new InvalidSignatureException("Invalid Signature on Bonrr");
			}
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

    public String[] decipherUsernameAndDomain(String domain, String username) {
        try {
            byte[] byteDomain = Crypto.decode(domain);
            byte[] byteUsername = Crypto.decode(username);

            String hashedDomain = null;
            String hashedUsername = null;

            hashedDomain = Crypto.decipherString(byteDomain, this.privateKey);

            hashedUsername = Crypto.decipherString(byteUsername, this.privateKey);

            return new String[]{hashedUsername, hashedDomain};
        } catch (BadPaddingException | NoSuchAlgorithmException
                | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
