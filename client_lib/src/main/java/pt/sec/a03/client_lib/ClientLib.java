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
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;

import pt.sec.a03.client_lib.exception.AlreadyExistsException;
import pt.sec.a03.client_lib.exception.DataNotFoundException;
import pt.sec.a03.client_lib.exception.IllegalAccessExistException;
import pt.sec.a03.client_lib.exception.InvalidArgumentException;
import pt.sec.a03.client_lib.exception.InvalidReceivedPasswordException;
import pt.sec.a03.client_lib.exception.InvalidSignatureException;
import pt.sec.a03.client_lib.exception.InvalidTimestampException;
import pt.sec.a03.client_lib.exception.UnexpectedErrorExeception;
import pt.sec.a03.client_lib.exception.UsernameAndDomainAlreadyExistException;
import pt.sec.a03.common_classes.Bonrr;
import pt.sec.a03.common_classes.CommonTriplet;
import pt.sec.a03.crypto.Crypto;

public class ClientLib {

	// Connection related constants
	private static final String VAULT_URI = "vault";
	private static final String USERS_URI = "users";

	private static final String PUBLIC_KEY_HEADER_NAME = "public-key";
	private static final String SIGNATURE_HEADER_NAME = "signature";
	private static final String NONCE_HEADER_NAME = "nonce-value";
	private static final String HASH_PASSWORD_HEADER_NAME = "hash-password";
	private static final String DOMAIN_HEADER_NAME = "domain";
	private static final String USERNAME_HEADER_NAME = "username";

	// Internal message constants
	private static final String SUCCESS_MSG = "Success";
	private static final String FORBIDEN_MSG = "Forbiden operation";
	private static final String ALREADY_EXISTS_MSG = "Entity already exists";
	private static final String DATA_NOT_FOUND_MSG = "Data Not Found";
	private static final String BAD_REQUEST_MSG = "Invalid Request";
	private static final String SERVER_ERROR_MSG = "Internal server error";
	private static final String ELSE_MSG = "Error";

	private static final String NULL_ARGUMENSTS_MSG = "One of the arguments was null";
	private static final String OVERSIZE_PASSWORD_MSG = "Password to big to the system 245 bytes maximum";
	private static final String INVALID_TIMESTAMP_EXCEPTION_MSG = "The timestamp received is invalid";
	private static final String BAD_REQUEST_EXCEPTION_MSG = "There were an problem with the headers of the request";
	private static final String INTERNAL_SERVER_FAILURE_EXCEPTION_MSG = "There were an problem with the server";
	private static final String UNEXPECTED_ERROR_EXCEPTION_MSG = "There was an unexpected error";
	private static final String INVALID_ARGUMENSTS_MSG = "One of the arguments was invalid";
	private static final String HASH_DOMAIN_IN_MAP = "domain";
	private static final String HASH_USERNAME_IN_MAP = "username";
	private static final String PASSWORD_IN_MAP = "password";
	private static final String HASH_PASSWORD_IN_MAP = "hash-password";
	private static final String BONRR_HEADER_NAME = "bonrr";

	// Attributes
	private PublicKey cliPubKey;
	private PrivateKey cliPrivKey;
	private Map<String, PublicKey> serversPubKey;
	private Map<String, Long> nonces;
	private Map<String, String> bonnrs;
	private Map<String, String> servers;
	private Client client;
	private Bonrr bonrr;

	public ClientLib(Map<String, String> hosts) {
		this.servers = hosts;
		client = ClientBuilder.newClient();
	}

	public void init(KeyStore ks, String aliasForPubPrivKey, String keyStorePw) {
		if (ks == null || aliasForPubPrivKey == null || keyStorePw == null) {
			throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
		}
		readKeysFromKeyStore(ks, keyStorePw, aliasForPubPrivKey, servers.keySet());

		nonces = new HashMap<String, Long>();
		for (String alias : serversPubKey.keySet()) {
			getMetaInfo(alias);
		}

		while (nonces.size() <= ((servers.keySet().size() + Bonrr.FAULT_NUMBER) / 2)){}

		if(userRegistered()){
			createBonrr();
		}
	}

	private boolean userRegistered() {
		int i = 0;
		for (String s : nonces.keySet()) {
			if (nonces.get(s) > 0){
				i++;
			}
		}
		if(i > ((servers.keySet().size() + Bonrr.FAULT_NUMBER) / 2)){
			return true;
		}
		else {
			return false;
		}
	}

	public void register_user() {
		for (String alias : serversPubKey.keySet()) {
			String[] infoToSend = prepareForRegisterUser(alias);
			Future<Response> response = sendRegisterUser(infoToSend, alias);
			try {
				processRegisterUser(response.get(), alias);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}
		createBonrr();
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
					public void completed(Response response) {
						System.out.println("Response of register user status code "
								+ response.getStatus() + " received.");
					}

					@Override
					public void failed(Throwable throwable) {
						System.out.println("Invocation failed in resgister user.");
						throwable.printStackTrace();
					}
				});
	}

	public void processRegisterUser(Response postResponse, String alias) {
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
			System.out.println(SUCCESS_MSG);

		} else if (postResponse.getStatus() == 400) {
			System.out.println(BAD_REQUEST_MSG);
			throw new BadRequestException(BAD_REQUEST_EXCEPTION_MSG);
		} else if (postResponse.getStatus() == 409) {
			System.out.println(ALREADY_EXISTS_MSG);
			throw new AlreadyExistsException("This public key already exists in the server");
		} else if (postResponse.getStatus() == 500) {
			System.out.println(SERVER_ERROR_MSG);
			throw new InternalServerErrorException(INTERNAL_SERVER_FAILURE_EXCEPTION_MSG);
		} else {
			System.out.println(ELSE_MSG);
			throw new UnexpectedErrorExeception(UNEXPECTED_ERROR_EXCEPTION_MSG);
		}
	}

	public void save_password(String domain, String username, String password) {
		if (domain == null || username == null || password == null) {
			throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
		}
		if (password.length() >= 246) {
			throw new InvalidArgumentException(OVERSIZE_PASSWORD_MSG);
		}

		System.out.println("Bonrr id is" + bonrr.getBonrrID());

		HashMap<String, byte[]> infoToSend = prepareForSave(domain, username, password);
		System.out.println(bonrr.write(infoToSend));
	}

	public HashMap prepareForSave(String domain, String username, String password) {
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

	public String retrieve_password(String domain, String username) {
		if (domain == null || username == null) {
			throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
		}
		HashMap<String, byte[]> infoToSend = prepareForRetrievePassword(domain, username);
		String pass = bonrr.read(infoToSend);
		return pass;
	}

	public HashMap prepareForRetrievePassword(String domain, String username) {
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

	public void close() {

	}

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

	private void createBonrr() {

		bonnrs = new HashMap<String, String>();
		for (String alias : serversPubKey.keySet()) {
			getBonrrID(alias);
		}

		while (bonnrs.size() <= ((servers.keySet().size() + Bonrr.FAULT_NUMBER) / 2)){}

		bonrr = new Bonrr(cliPubKey, cliPrivKey, servers, serversPubKey, getMaxBonrrID());
	}

	private void getBonrrID(String alias) {
		try {
			// Generate Nonce
			String stringNonce = nonces.get(alias) + "";

			byte[] cipheredNonce = Crypto.cipherString(stringNonce, serversPubKey.get(alias));

			String stringPubKey = Crypto.encode(cliPubKey.getEncoded());
			String encodedNonce = Crypto.encode(cipheredNonce);

			// Generate signature
			String tosign = stringNonce + stringPubKey;
			String sig = Crypto.encode(Crypto.makeDigitalSignature(tosign.getBytes(), cliPrivKey));

			getWebTargetToResource(alias, USERS_URI).path("/bonrr").request()
					.header(SIGNATURE_HEADER_NAME, sig)
					.header(PUBLIC_KEY_HEADER_NAME, stringPubKey)
					.header(NONCE_HEADER_NAME, encodedNonce)
					.async().get(new InvocationCallback<Response>() {
						@Override
						public void completed(Response response) {
							try {

								String stringNonceCiph = response.getHeaderString(NONCE_HEADER_NAME);
								String stringSig = response.getHeaderString(SIGNATURE_HEADER_NAME);
								String bonrrID = response.getHeaderString(BONRR_HEADER_NAME);

								String stringNonce = Crypto.decipherString(Crypto.decode(stringNonceCiph), cliPrivKey);
								verifySignature(serversPubKey.get(alias), stringSig, stringNonce + bonrrID);

								addToNonces(alias, Long.parseLong(stringNonce));
								addToBonrrs(alias, bonrrID);

							} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
									| BadPaddingException | IllegalBlockSizeException e) {
								e.printStackTrace();
								throw new RuntimeException(e.getMessage());
							}
						}

						@Override
						public void failed(Throwable throwable) {
							System.out.println("Invocation failed in resgister user.");
							throwable.printStackTrace();
						}
					});

		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	private synchronized void addToBonrrs(String alias, String bonrrID) {
		bonnrs.put(alias, bonrrID);
	}

	private void getMetaInfo(String alias) {
		String stringPubKey = Crypto.encode(cliPubKey.getEncoded());

		getWebTargetToResource(alias, USERS_URI).path("/meta").request()
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
				throwable.printStackTrace();
			}
		});
	}

	private synchronized void addToNonces(String alias, long l) {
		nonces.put(alias, l);
	}

	private String getMaxBonrrID() {
		String s = null;
		for (String s1 : bonnrs.values()) {
			if(s == null){
				s = s1;
			}
			else if(s1.compareTo(s) == 1) {
				s = s1;
			}
		}
		return s +  "";
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

	private void verifyPassHash(String password, String encodedHashReceived, PublicKey pubKeyClient) {
		try {
			byte[] hashToVerify = Crypto.hashString(password);
			byte[] cipheredHashReceived = Crypto.decode(encodedHashReceived);
			String hashReceived = Crypto.decipherString(cipheredHashReceived, pubKeyClient);

			if (!hashReceived.equals(new String(hashToVerify))) {
				throw new InvalidReceivedPasswordException(
						"Password received was different than the one sent to the server");
			}

		} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	private WebTarget getWebTargetToResource(String alias, String resource) {
		return 	client.target("http://" + servers.get(alias) + "/PwServer/").path(resource);
	}


}
