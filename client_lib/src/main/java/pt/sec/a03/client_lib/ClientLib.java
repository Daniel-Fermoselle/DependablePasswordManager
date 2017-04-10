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

	// Attributes
	private PublicKey cliPubKey;
	private PrivateKey cliPrivKey;
	private Map<String, PublicKey> serversPubKey;
	private Map<String, Long> nonces;
	private Map<String, String> servers;
	private Map<String, WebTarget> serversTargets;

	public ClientLib(Map<String, String> hosts) {
		this.servers = hosts;
		this.serversTargets = new HashMap<String,WebTarget>();
		for (String s : hosts.keySet()) {
			Client client = ClientBuilder.newClient();
			serversTargets.put(s, client.target("http://" + hosts.get(s) + "/PwServer/"));
		}
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
	}

	public void register_user() {
		for (String alias : serversPubKey.keySet()) {
			String[] infoToSend = prepareForRegisterUser(alias);
			Response response = sendRegisterUser(infoToSend, alias);
			processRegisterUser(response, alias);
		}
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

	public Response sendRegisterUser(String[] infoToSend, String alias) {
		return getWebTargetToResource(alias, USERS_URI).request().header(SIGNATURE_HEADER_NAME, infoToSend[0])
				.header(PUBLIC_KEY_HEADER_NAME, infoToSend[1]).header(NONCE_HEADER_NAME, infoToSend[2])
				.post(Entity.json(null));
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

		for (String alias : serversPubKey.keySet()) {
			String[] infoToSend = prepareForSave(domain, username, password, alias);
			Response response = sendSavePassword(infoToSend, alias);
			processSavePassword(response, alias);
		}
	}

	public String[] prepareForSave(String domain, String username, String password, String alias) {
		try {
			// --------Initial hashs and timestamp
			byte[] hashDomain = Crypto.hashString(domain);
			byte[] hashUsername = Crypto.hashString(username);
			byte[] hashPassword = Crypto.hashString(password);
			String stringNonce = nonces.get(alias) + "";
			// --------

			// ---------Creation of the string used to make the signature to use
			// in the header
			String stringHashDomain = new String(hashDomain);
			String stringHashUsername = new String(hashUsername);

			// --------Ciphered hashs and string conversion for them
			byte[] cipherDomain = Crypto.cipherString(stringHashDomain, serversPubKey.get(alias));
			byte[] cipherUsername = Crypto.cipherString(stringHashUsername, serversPubKey.get(alias));
			byte[] cipherPassword = Crypto.cipherString(password, cliPubKey);
			byte[] cipherHashPassword = Crypto.cipherString(new String(hashPassword), cliPrivKey);
			byte[] cipherStringNonce = Crypto.cipherString(stringNonce, serversPubKey.get(alias));

			String StringCipheredDomain = Crypto.encode(cipherDomain);
			String StringCipheredUsername = Crypto.encode(cipherUsername);
			String StringCipheredPassword = Crypto.encode(cipherPassword);
			String headerHashPassword = Crypto.encode(cipherHashPassword);
			String encodedNonce = Crypto.encode(cipherStringNonce);
			// ---------

			String dataToSign = stringHashUsername + stringHashDomain + stringNonce + headerHashPassword
					+ StringCipheredPassword;

			String sig = Crypto.encode(Crypto.makeDigitalSignature(dataToSign.getBytes(), cliPrivKey));
			// ---------

			// -------
			String stringPubKey = Crypto.encode(cliPubKey.getEncoded());

			return new String[] { stringPubKey, sig, encodedNonce, headerHashPassword, StringCipheredPassword,
					StringCipheredUsername, StringCipheredDomain };

		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public Response sendSavePassword(String[] infoToSend, String alias) {
		CommonTriplet commonTriplet = new CommonTriplet(infoToSend[4], infoToSend[5], infoToSend[6]);
		return getWebTargetToResource(alias, VAULT_URI).request().header(PUBLIC_KEY_HEADER_NAME, infoToSend[0])
				.header(SIGNATURE_HEADER_NAME, infoToSend[1]).header(NONCE_HEADER_NAME, infoToSend[2])
				.header(HASH_PASSWORD_HEADER_NAME, infoToSend[3]).post(Entity.json(commonTriplet));
	}

	public void processSavePassword(Response postResponse, String alias) {
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
		} else if (postResponse.getStatus() == 403) {
			System.out.println(FORBIDEN_MSG);
			throw new UsernameAndDomainAlreadyExistException("This combination of username and domain already exists");
		} else if (postResponse.getStatus() == 404) {
			System.out.println(DATA_NOT_FOUND_MSG);
			throw new DataNotFoundException("This public key is not registered in the server");
		} else if (postResponse.getStatus() == 500) {
			System.out.println(SERVER_ERROR_MSG);
			throw new InternalServerErrorException(INTERNAL_SERVER_FAILURE_EXCEPTION_MSG);
		} else {
			System.out.println(ELSE_MSG);
			throw new UnexpectedErrorExeception(UNEXPECTED_ERROR_EXCEPTION_MSG);
		}
	}

	public String retrieve_password(String domain, String username) {
		if (domain == null || username == null) {
			throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
		}
		for (String alias : serversPubKey.keySet()) {
			String[] infoToSend = prepareForRetrievePassword(domain, username, alias);
			Response response = sendRetrievePassword(infoToSend, alias);
			return processRetrievePassword(response, alias);
		}
		throw new RuntimeException("Error throw reached in retrive_password");
	}

	public String[] prepareForRetrievePassword(String domain, String username, String alias) {
		try {
			// Hash domain and username
			byte[] hashDomain = Crypto.hashString(domain);
			byte[] hashUsername = Crypto.hashString(username);
			String stringHashDomain = new String(hashDomain);
			String stringHashUsername = new String(hashUsername);

			// Generate timestamp
			String stringNonce = nonces.get(alias) + "";

			// Cipher domain and username hash with server public key
			byte[] cipheredDomain = Crypto.cipherString(stringHashDomain, serversPubKey.get(alias));
			byte[] cipheredUsername = Crypto.cipherString(stringHashUsername, serversPubKey.get(alias));
			byte[] cipheredNonce = Crypto.cipherString(stringNonce, serversPubKey.get(alias));
			String encodedDomain = Crypto.encode(cipheredDomain);
			String encodedUsername = Crypto.encode(cipheredUsername);
			String encodeNonce = Crypto.encode(cipheredNonce);

			// Generate signature
			String tosign = stringHashUsername + stringHashDomain + stringNonce;
			String sig = Crypto.encode(Crypto.makeDigitalSignature(tosign.getBytes(), cliPrivKey));

			String stringPubKey = Crypto.encode(cliPubKey.getEncoded());

			return new String[] { stringPubKey, sig, encodeNonce, encodedDomain, encodedUsername };

		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public Response sendRetrievePassword(String[] infoToSend, String alias) {

		return getWebTargetToResource(alias, VAULT_URI).request().header(PUBLIC_KEY_HEADER_NAME, infoToSend[0])
				.header(SIGNATURE_HEADER_NAME, infoToSend[1]).header(NONCE_HEADER_NAME, infoToSend[2])
				.header(DOMAIN_HEADER_NAME, infoToSend[3]).header(USERNAME_HEADER_NAME, infoToSend[4]).get();
	}

	public String processRetrievePassword(Response getResponse, String alias) {
		try {

			if (getResponse.getStatus() == 400) {
				System.out.println(BAD_REQUEST_MSG);
				throw new BadRequestException(BAD_REQUEST_EXCEPTION_MSG);
			} else if (getResponse.getStatus() == 403) {
				System.out.println(FORBIDEN_MSG);
				throw new IllegalAccessExistException("This combination of username and domain already exists");
			} else if (getResponse.getStatus() == 404) {
				System.out.println(DATA_NOT_FOUND_MSG);
				throw new DataNotFoundException("This public key is not registered in the server");
			} else if (getResponse.getStatus() == 500) {
				System.out.println(SERVER_ERROR_MSG);
				throw new InternalServerErrorException(INTERNAL_SERVER_FAILURE_EXCEPTION_MSG);
			}

			// Decipher password
			String passwordReceived = getResponse.readEntity(CommonTriplet.class).getPassword();
			String password = Crypto.decipherString(Crypto.decode(passwordReceived), cliPrivKey);

			// Get headers info
			String sigToVerify = getResponse.getHeaderString(SIGNATURE_HEADER_NAME);
			String stringNonceCiph = getResponse.getHeaderString(NONCE_HEADER_NAME);
			String encodedHashReceived = getResponse.getHeaderString(HASH_PASSWORD_HEADER_NAME);

			// Check nonce freshness
			String stringNonce = Crypto.decipherString(Crypto.decode(stringNonceCiph), cliPrivKey);
			verifyNonce(stringNonce, alias);

			// Verify signature
			String sig = stringNonce + encodedHashReceived + passwordReceived;
			verifySignature(serversPubKey.get(alias), sigToVerify, sig);

			// Verify if password's hash is correct
			verifyPassHash(password, encodedHashReceived, cliPubKey);

			if (getResponse.getStatus() == 200) {
				System.out.println(SUCCESS_MSG);
				return password;
			} else {
				System.out.println(ELSE_MSG);
				throw new UnexpectedErrorExeception(UNEXPECTED_ERROR_EXCEPTION_MSG);
			}

		} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw new BadRequestException(e.getMessage());
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

	private void getMetaInfo(String alias) {
		try {
			String stringPubKey = Crypto.encode(cliPubKey.getEncoded());

			Response response = getWebTargetToResource(alias, USERS_URI).request()
					.header(PUBLIC_KEY_HEADER_NAME, stringPubKey).get();

			String stringNonceCiph = response.getHeaderString(NONCE_HEADER_NAME);
			String stringSig = response.getHeaderString(SIGNATURE_HEADER_NAME);

			String stringNonce = Crypto.decipherString(Crypto.decode(stringNonceCiph), cliPrivKey);
			verifySignature(serversPubKey.get(alias), stringSig, stringNonce);

			nonces.put(alias, Long.parseLong(stringNonce));

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		}

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
		return serversTargets.get(alias).path(resource);
	}
}
