package pt.sec.a03.common_classes;

import pt.sec.a03.crypto.Crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;

public class AuthLink {

	private static final String HASH_DOMAIN_IN_MAP = "domain";
	private static final String HASH_USERNAME_IN_MAP = "username";
	private static final String PASSWORD_IN_MAP = "password";
	private static final String HASH_PASSWORD_IN_MAP = "hash-password";
	private static final String WTS_IN_MAP = "wts";
	private static final String SIGNATURE_IN_MAP = "signature";

	private static final String PUBLIC_KEY_HEADER_NAME = "public-key";
	private static final String SIGNATURE_HEADER_NAME = "signature";
	private static final String NONCE_HEADER_NAME = "nonce-value";
	private static final String HASH_PASSWORD_HEADER_NAME = "hash-password";
	private static final String DOMAIN_HEADER_NAME = "domain";
	private static final String USERNAME_HEADER_NAME = "username";
	private static final String AUTH_LINK_SIG = "auth-signature";
	private static final String ACK_HEADER_NAME = "ack";
    private static final String RID_HEADER_NAME = "rid";
    private static final String BONRR_HEADER_NAME = "bonrr";

	private Bonrr bonrr;
	private PublicKey publicKey;
	private PrivateKey privateKey;

	public AuthLink(Bonrr bonrr) {
		this.bonrr = bonrr;
	}

	public AuthLink() {
	}

	public void send(PrivateKey cliPrivKey, PublicKey cliPubKey, String uriToSend, byte[] sig, long wts,
			HashMap<String, byte[]> infoToSend, String bonrr) {

		CommonTriplet commonTriplet = new CommonTriplet(Crypto.encode(infoToSend.get(HASH_DOMAIN_IN_MAP)),
				Crypto.encode(infoToSend.get(HASH_USERNAME_IN_MAP)), Crypto.encode(infoToSend.get(PASSWORD_IN_MAP)),
				Crypto.encode(infoToSend.get(HASH_PASSWORD_IN_MAP)));

		Client client = ClientBuilder.newClient();
		WebTarget vault = client.target("http://" + uriToSend + "/PwServer/").path("vault");

		// Verify Signature
		String toSign = Crypto.encode(sig) + wts;
		toSign = toSign + commonTriplet.getDomain();
		toSign = toSign + commonTriplet.getUsername();
		toSign = toSign + commonTriplet.getPassword();
		toSign = toSign + commonTriplet.getHash();
		byte[] authSig = makeSignature(cliPrivKey, toSign);

		vault.request().header(AUTH_LINK_SIG, Crypto.encode(authSig))
				.header(PUBLIC_KEY_HEADER_NAME, Crypto.encode(cliPubKey.getEncoded()))
				.header(SIGNATURE_HEADER_NAME, Crypto.encode(sig))
                .header(NONCE_HEADER_NAME, wts + "")
				.header(BONRR_HEADER_NAME, bonrr).async()
				.post(Entity.json(commonTriplet), new InvocationCallback<Response>() {
					@Override
					public void completed(Response response) {
						try {

							System.out.println(
									"Response of save password status code " + response.getStatus() + " received.");

							String toVerify = response.getHeaderString(ACK_HEADER_NAME)
									+ response.getHeaderString(NONCE_HEADER_NAME);

							PublicKey serverPubKey = Crypto
									.getPubKeyFromByte(Crypto.decode(response.getHeaderString(PUBLIC_KEY_HEADER_NAME)));

							// Verify signature
							verifySignature(serverPubKey, response.getHeaderString(AUTH_LINK_SIG), toVerify);

							AuthLink.this.bonrr.addToAckList(response.getHeaderString(ACK_HEADER_NAME),
									Long.parseLong(response.getHeaderString(NONCE_HEADER_NAME)));

						} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
							e.printStackTrace();
							throw new RuntimeException(e.getMessage());
						}
					}

					@Override
					public void failed(Throwable throwable) {
						System.out.println("Invocation failed in save password.");
						throwable.printStackTrace();
					}
				});
	}

	public void send(PrivateKey cliPrivKey, PublicKey cliPubKey, String uriToSend, long readId,
			HashMap<String, byte[]> infoToSend, String bonrr) {
	    this.privateKey = cliPrivKey;
	    this.publicKey = cliPubKey;

		String hashedDomain = Crypto.encode(infoToSend.get(HASH_DOMAIN_IN_MAP));
		String hashedUsername = Crypto.encode(infoToSend.get(HASH_USERNAME_IN_MAP));

		Client client = ClientBuilder.newClient();
		WebTarget vault = client.target("http://" + uriToSend + "/PwServer/").path("vault");

		// Verify Signature
		String toSign = "" + readId;
		toSign = toSign + hashedDomain;
		toSign = toSign + hashedUsername;
		byte[] authSig = makeSignature(cliPrivKey, toSign);

		vault.request().header(AUTH_LINK_SIG, Crypto.encode(authSig))
				.header(PUBLIC_KEY_HEADER_NAME, Crypto.encode(cliPubKey.getEncoded()))
				.header(RID_HEADER_NAME, readId + "")
				.header(BONRR_HEADER_NAME, bonrr)
				.header(DOMAIN_HEADER_NAME, hashedDomain)
				.header(USERNAME_HEADER_NAME, hashedUsername).async()
				.get(new InvocationCallback<Response>() {
					@Override
					public void completed(Response response) {
						try {

                            CommonTriplet t = response.readEntity(CommonTriplet.class);
                            String encodedHashDomain = t.getDomain();
                            String encodedHashUsername = t.getUsername();
                            String encodedCipheredPassword = t.getPassword();
                            String encodedCipheredHashPassword = t.getHash();
                            String encodedWriteSig = response.getHeaderString(SIGNATURE_HEADER_NAME);
                            long wts = Long.parseLong(response.getHeaderString(NONCE_HEADER_NAME));
                            long rid = Long.parseLong(response.getHeaderString(RID_HEADER_NAME));

							System.out.println(
									"Response of save password status code " + response.getStatus() + " received.");

							String toVerify = (rid + "") + (wts + "") + encodedHashDomain + encodedHashUsername + encodedCipheredPassword
                                    + encodedCipheredHashPassword + encodedWriteSig;

							PublicKey serverPubKey = Crypto
									.getPubKeyFromByte(Crypto.decode(response.getHeaderString(PUBLIC_KEY_HEADER_NAME)));

							System.out.println("Verify auth");
							// Verify authentication link signature
							verifySignature(serverPubKey, response.getHeaderString(AUTH_LINK_SIG), toVerify);

                            String[] userAndDom = decipherUsernameAndDomain(encodedHashDomain, encodedHashUsername);

                            String toVerifyWriteSig = bonrr + wts + userAndDom[1] + userAndDom[0]
                                    + encodedCipheredPassword + encodedCipheredHashPassword;

                            System.out.println("Verify writeSig");
                            // Verify write signature
                            verifySignature(AuthLink.this.publicKey, encodedWriteSig, toVerifyWriteSig);

                            HashMap<String,String> value = new HashMap<String, String>();
                            value.put(HASH_DOMAIN_IN_MAP, userAndDom[1]);
                            value.put(HASH_USERNAME_IN_MAP, userAndDom[0]);
                            value.put(PASSWORD_IN_MAP, encodedCipheredPassword);
                            value.put(HASH_PASSWORD_IN_MAP, encodedCipheredHashPassword);
                            value.put(WTS_IN_MAP, wts + "");
                            value.put(SIGNATURE_IN_MAP, encodedWriteSig);
							AuthLink.this.bonrr.addToReadList(value, rid);

						} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
							e.printStackTrace();
							throw new RuntimeException(e.getMessage());
						}
					}

					@Override
					public void failed(Throwable throwable) {
						System.out.println("Invocation failed in save password.");
						throwable.printStackTrace();
					}
				});
	}

	public void deliverWrite(String publicKey, String authSig, String signature, String wts, CommonTriplet t) {
		try {
			String toVerify = signature + wts + t.getDomain() + t.getUsername() + t.getPassword() + t.getHash();
			PublicKey pubKey = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));
			verifySignature(pubKey, authSig, toVerify);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

    public void deliverRead(String publicKey, String authSig, String rid, String domain, String username) {
        try {
            String toVerify = rid + domain + username;
            PublicKey pubKey = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));
            verifySignature(pubKey, authSig, toVerify);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());

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
				// TODO throw new InvalidSignatureException();
				throw new RuntimeException("Invalid Signature on Bonrr");
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
