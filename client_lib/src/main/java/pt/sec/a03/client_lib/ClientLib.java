package pt.sec.a03.client_lib;

import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;

import pt.sec.a03.common_classes.Bonrr;
import pt.sec.a03.common_classes.exception.AlreadyExistsException;
import pt.sec.a03.common_classes.exception.InvalidArgumentException;
import pt.sec.a03.common_classes.exception.InvalidSignatureException;
import pt.sec.a03.common_classes.exception.InvalidTimestampException;
import pt.sec.a03.common_classes.exception.UnexpectedErrorExeception;
import pt.sec.a03.crypto.Crypto;

public class ClientLib {

	// Connection related constants
	private static final String USERS_URI = "users";

	private static final String PUBLIC_KEY_HEADER_NAME = "public-key";
	private static final String SIGNATURE_HEADER_NAME = "signature";
	private static final String NONCE_HEADER_NAME = "nonce-value";

    private static final String HASH_DOMAIN_IN_MAP = "domain";
    private static final String HASH_USERNAME_IN_MAP = "username";
    private static final String PASSWORD_IN_MAP = "password";
    private static final String HASH_PASSWORD_IN_MAP = "hash-password";

	// Internal message constants
	private static final String SUCCESS_MSG = "Success";
	private static final String ALREADY_EXISTS_MSG = "Entity already exists";
	private static final String BAD_REQUEST_MSG = "Invalid Request";
	private static final String SERVER_ERROR_MSG = "Internal server error";
	private static final String ELSE_MSG = "Error";

	private static final String NULL_ARGUMENSTS_MSG = "One of the arguments was null";
	private static final String OVERSIZE_PASSWORD_MSG = "Password to big to the system 245 bytes maximum";
	private static final String INVALID_TIMESTAMP_EXCEPTION_MSG = "The timestamp received is invalid";
	private static final String BAD_REQUEST_EXCEPTION_MSG = "There were an problem with the headers of the request";
	private static final String INVALID_ARGUMENSTS_MSG = "One of the arguments was invalid";


	// Attributes
	private Client client;
	private PublicKey cliPubKey;
	private PrivateKey cliPrivKey;

	private Map<String, PublicKey> serversPubKey;
	private Map<String, Long> nonces;

	private Map<String, String> servers;
	private Bonrr bonrr;

	private ArrayList<String> responses;
	
	long rank;

	public ClientLib(Map<String, String> hosts, long rank) {
		this.servers = hosts;
		client = ClientBuilder.newClient();
		this.rank = rank;
	}

	public void init(KeyStore ks, String aliasForPubPrivKey, String keyStorePw) {
		if (ks == null || aliasForPubPrivKey == null || keyStorePw == null) {
			throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
		}
		readKeysFromKeyStore(ks, keyStorePw, aliasForPubPrivKey, servers.keySet());

		getNonces();

		bonrr = new Bonrr(cliPubKey, cliPrivKey, servers, serversPubKey, Crypto.encode(cliPubKey.getEncoded()), rank);

	}

	//----------------------------------------
	//			Register User Functions
	//----------------------------------------

	public void register_user() {
	    responses = new ArrayList<>();
		for (String alias : serversPubKey.keySet()) {
			String[] infoToSend = prepareForRegisterUser(alias);
			sendRegisterUser(infoToSend, alias);
		}
        processResponses();
    }

    public String[] prepareForRegisterUser(String alias) {
		try {
			// Generate Nonce
			String stringNonce = nonces.get(alias) + "";

			byte[] cipheredNonce = Crypto.cipherString(stringNonce, serversPubKey.get(alias));

			String stringPubKey = Crypto.encode(cliPubKey.getEncoded());
			String encodedNonce = Crypto.encode(cipheredNonce);

			// Generate signature
			String tosign = stringNonce + stringPubKey;
			String sig = Crypto.encode(Crypto.makeDigitalSignature(tosign.getBytes(), cliPrivKey));

			return new String[] { sig, stringPubKey, encodedNonce };

		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public Future<Response> sendRegisterUser(String[] infoToSend, String alias) {
		return getWebTargetToResource(alias, USERS_URI).request()
				.header(SIGNATURE_HEADER_NAME, infoToSend[0])
				.header(PUBLIC_KEY_HEADER_NAME, infoToSend[1])
				.header(NONCE_HEADER_NAME, infoToSend[2])
				.async().post(Entity.json(null), new InvocationCallback<Response>() {
					@Override
					public void completed(Response postResponse) {
						System.out.println("Response of save password status code " + postResponse.getStatus()
								+ " received from " + servers.get(alias) + ".");

                        if (postResponse.getStatus() == 201) {
                            String sigToVerify = postResponse.getHeaderString(SIGNATURE_HEADER_NAME);
                            String encodedNonce = postResponse.getHeaderString(NONCE_HEADER_NAME);
                            try {

                                String stringNonce = Crypto.decipherString(Crypto.decode(encodedNonce), cliPrivKey);

                                verifyNonce(stringNonce, alias);

                                String sig = stringNonce;
                                verifySignature(serversPubKey.get(alias), sigToVerify, sig);

                            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException
                                    | BadPaddingException e) {
                                throw new BadRequestException(e.getMessage());
                            }
                            responses.add(SUCCESS_MSG);

                        } else if (postResponse.getStatus() == 400) {
                            responses.add(BAD_REQUEST_MSG);
                        } else if (postResponse.getStatus() == 409) {
                            responses.add(ALREADY_EXISTS_MSG);
                        } else if (postResponse.getStatus() == 500) {
                            responses.add(SERVER_ERROR_MSG);
                        } else {
                            responses.add(ELSE_MSG);
                        }
					}
					@Override
					public void failed(Throwable throwable) {
						System.out.println("Invocation failed in resgister user.");
					}
				});
	}

    public void processResponses() {
        while (responses.size() <= ((servers.keySet().size() + Bonrr.FAULT_NUMBER) / 2)) {}

        HashMap<String, Integer> nbResponses = new HashMap<>();
        for (String response : responses) {
            if(nbResponses.get(response) == null){
                nbResponses.put(response, 1);
            }
            else{
                nbResponses.put(response, nbResponses.get(response) + 1);
            }
        }

        int number = 0;
        String response = null;
        for (String s : nbResponses.keySet()) {
            if(nbResponses.get(s) > number){
                number = nbResponses.get(s);
                response = s;
            }
        }

        if(number <=  ((servers.keySet().size() + Bonrr.FAULT_NUMBER) / 2)){
            throw new RuntimeException("No quorum achieved");
        }
        else{
            if(response.equals(SUCCESS_MSG)){
                System.out.println(SUCCESS_MSG);
            }
            else if(response.equals(BAD_REQUEST_MSG)){
                System.out.println(BAD_REQUEST_EXCEPTION_MSG);
                throw new BadRequestException(BAD_REQUEST_EXCEPTION_MSG);
            }
            else if(response.equals(ALREADY_EXISTS_MSG)){
                System.out.println(ALREADY_EXISTS_MSG);
                throw new AlreadyExistsException(ALREADY_EXISTS_MSG);
            }
            else if(response.equals(SERVER_ERROR_MSG)){
                System.out.println(SERVER_ERROR_MSG);
                throw new InternalServerErrorException(SERVER_ERROR_MSG);
            } else{
                System.out.println(ELSE_MSG);
                throw new UnexpectedErrorExeception(ELSE_MSG);
            }
        }
    }

	//----------------------------------------
	//			Save Password Functions
	//----------------------------------------

	public void save_password(String domain, String username, String password) {
		if (domain == null || username == null || password == null) {
			throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
		}
		if (password.length() >= 246) {
			throw new InvalidArgumentException(OVERSIZE_PASSWORD_MSG);
		}

		HashMap<String, byte[]> infoToSend = prepareForSave(domain, username, password);
		System.out.println(bonrr.write(infoToSend));
	}

	public HashMap<String, byte[]> prepareForSave(String domain, String username, String password) {
		try {
			// --------Initial hashs and timestamp
			byte[] hashDomain = Crypto.hashString(domain);
			byte[] hashUsername = Crypto.hashString(username);
			byte[] hashPassword = Crypto.hashString(password);
			// --------
			
			// --------Ciphered hashs and string conversion for them
			byte[] cipherPassword = Crypto.cipherString(password, cliPubKey);
			byte[] cipherHashPassword = Crypto.cipherString(new String(hashPassword), cliPrivKey);
			// ---------

			HashMap<String, byte[]> stringHashMap = new HashMap<String, byte[]>();

			stringHashMap.put(HASH_DOMAIN_IN_MAP, hashDomain);
			stringHashMap.put(HASH_USERNAME_IN_MAP, hashUsername);
			stringHashMap.put(PASSWORD_IN_MAP, cipherPassword);
			stringHashMap.put(HASH_PASSWORD_IN_MAP, cipherHashPassword);

			return stringHashMap;

		} catch (NoSuchAlgorithmException | InvalidKeyException  | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	//----------------------------------------
	//			Retrieve Password Functions
	//----------------------------------------

	public String retrieve_password(String domain, String username) {
		if (domain == null || username == null) {
			throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
		}
		HashMap<String, byte[]> infoToSend = prepareForRetrievePassword(domain, username);
		String pass = bonrr.read(infoToSend);
		return pass;
	}

	public HashMap<String, byte[]> prepareForRetrievePassword(String domain, String username) {
		try {
			// Hash domain and username
			byte[] hashDomain = Crypto.hashString(domain);
			byte[] hashUsername = Crypto.hashString(username);

			HashMap<String, byte[]> stringHashMap = new HashMap<String, byte[]>();

			stringHashMap.put(HASH_DOMAIN_IN_MAP, hashDomain);
			stringHashMap.put(HASH_USERNAME_IN_MAP, hashUsername);

			return stringHashMap;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public void close() {}

	//----------------------------------------
	//			Initialization Functions
	//----------------------------------------

	private void readKeysFromKeyStore(KeyStore ks, String keyStorePw, String aliasForPubPrivKey,
									  Set<String> aliasForServers) {
		try {

			Certificate cert = ks.getCertificate(aliasForPubPrivKey);
			if (cert == null) {
				throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
			}
			cliPubKey = Crypto.getPublicKeyFromCertificate(cert);
			cliPrivKey = Crypto.getPrivateKeyFromKeystore(ks, aliasForPubPrivKey, keyStorePw);

			serversPubKey = new HashMap<String, PublicKey>();
			for (String alias : aliasForServers) {
				Certificate cert2 = ks.getCertificate(alias);
				if (cert2 == null) {
					throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
				}
				serversPubKey.put(alias, Crypto.getPublicKeyFromCertificate(cert2));
			}

		} catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
			throw new InvalidArgumentException(INVALID_ARGUMENSTS_MSG);
		}
	}

	private void getNonces() {
		nonces = new HashMap<String, Long>();
		for (String alias : serversPubKey.keySet()) {
			getMetaInfo(alias);
		}

		while (nonces.size() <= ((servers.keySet().size() + Bonrr.FAULT_NUMBER) / 2)){}
	}

	private void getMetaInfo(String alias) {
		String stringPubKey = Crypto.encode(cliPubKey.getEncoded());

		getWebTargetToResource(alias, USERS_URI).request()
				.header(PUBLIC_KEY_HEADER_NAME, stringPubKey).async().get(new InvocationCallback<Response>() {
			@Override
			public void completed(Response response) {
				try {

					String stringNonceCiph = response.getHeaderString(NONCE_HEADER_NAME);
					String stringSig = response.getHeaderString(SIGNATURE_HEADER_NAME);

					String stringNonce = Crypto.decipherString(Crypto.decode(stringNonceCiph), cliPrivKey);
					verifySignature(serversPubKey.get(alias), stringSig, stringNonce);

					addToNonces(alias,  Long.parseLong(stringNonce));

				} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
						| BadPaddingException | IllegalBlockSizeException e) {
					e.printStackTrace();
					throw new RuntimeException(e.getMessage());
				}
			}

			@Override
			public void failed(Throwable throwable) {
				System.out.println("Invocation failed.");
			}
		});
	}

	//----------------------------------------
	//		Auxiliary functions Functions
	//----------------------------------------

	private synchronized void addToNonces(String alias, long l) {
		nonces.put(alias, l);
	}

	private WebTarget getWebTargetToResource(String alias, String resource) {
		return 	client.target("http://" + servers.get(alias) + "/PwServer/").path(resource);
	}

	private void verifyNonce(String stringNonce, String alias) {
		long receivedNonce = Long.parseLong(stringNonce);
		if (!(receivedNonce > nonces.get(alias))) {
			throw new InvalidTimestampException(INVALID_TIMESTAMP_EXCEPTION_MSG);
		} else {
			nonces.put(alias, Long.parseLong(stringNonce));
		}
	}

	private void verifySignature(PublicKey publicKey, String signature, String clientSideTosign) {
		try {
			byte[] serverSideSig = Crypto.decode(signature);

			if (!Crypto.verifyDigitalSignature(serverSideSig, clientSideTosign.getBytes(), publicKey)) {
				throw new InvalidSignatureException("Invalid Signature");
			}
		} catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	public void setResponses(ArrayList a){
	    responses = a;
    }

}
