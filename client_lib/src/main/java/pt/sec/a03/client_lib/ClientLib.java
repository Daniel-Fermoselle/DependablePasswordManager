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
import java.sql.Timestamp;
import java.text.ParseException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;

import pt.sec.a03.client_lib.exception.AlreadyExistsException;
import pt.sec.a03.client_lib.exception.DataNotFoundException;
import pt.sec.a03.client_lib.exception.InvalidArgumentException;
import pt.sec.a03.client_lib.exception.UsernameAndDomainAlreadyExistException;
import pt.sec.a03.client_lib.filters.LoggingFilter;
import pt.sec.a03.common_classes.CommonTriplet;
import pt.sec.a03.crypto.Crypto;

public class ClientLib {

	// Keys related constants
	private static final String ALIAS_FOR_SERVER_PUB_KEY = "server";

	// Connection related constants
	private static final String BASE_TARGET_URI = "http://localhost:8080/PwServer/webapi/";
	private static final String VAULT_URI = "vault";
	private static final String USERS_URI = "users";

	private static final String PUBLIC_KEY_HEADER_NAME = "public-key";
	private static final String SIGNATURE_HEADER_NAME = "signature";
	private static final String TIME_STAMP_HEADER_NAME = "timestamp";
	private static final String HASH_PASSWORD_HEADER_NAME = "hash-password";
	private static final String DOMAIN_HEADER_NAME = "domain";
	private static final String USERNAME_HEADER_NAME = "username";

	// Internal message constants
	private static final String SUCCESS_MSG = "Success";
	private static final String INVALID_ARG_MSG = "Invalid argument";
	private static final String FORBIDEN_MSG = "Forbiden operation";
	private static final String ALREADY_EXISTS_MSG = "Entity already exists";
	private static final String DATA_NOT_FOUND_MSG = "Data Not Found";
	private static final String SERVER_ERROR_MSG = "Internal server error";
	private static final String ELSE_MSG = "Error";

	private static final String INVALID_TIME_STAMP_MSG = "Freshness compromised";
	private static final String INVALID_SIGNATURE_MSG = "Signature compromised";
	private static final String INVALID_PASSWORD_MSG = "Passwords don't match";

	// Attributes
	private KeyStore ks;
	private String aliasForPubPrivKeys;
	private String keyStorePw;

	private Client client = ClientBuilder.newClient().register(LoggingFilter.class);
	private WebTarget baseTarget = client.target(BASE_TARGET_URI);
	private WebTarget vaultTarget = baseTarget.path(VAULT_URI);
	private WebTarget userTarget = baseTarget.path(USERS_URI);

	public void init(KeyStore ks, String aliasForPubPrivKey, String keyStorePw) {
		if (ks == null || aliasForPubPrivKey == null || keyStorePw == null) {
			throw new InvalidArgumentException("One of the arguments of the init method was null");
		}
		this.ks = ks;
		this.aliasForPubPrivKeys = aliasForPubPrivKey;
		this.keyStorePw = keyStorePw;
		checkArguments();
	}

	public void register_user() {

		// Get PubKey from key store
		Certificate cert;
		try {
			cert = ks.getCertificate(aliasForPubPrivKeys);
			PublicKey pubKey = Crypto.getPublicKeyFromCertificate(cert);
			PrivateKey clientprivKey = Crypto.getPrivateKeyFromKeystore(ks, aliasForPubPrivKeys, keyStorePw);

			// Generate timestamp
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String stringTS = timestamp.toString();

			String stringPubKey = Crypto.encode(pubKey.getEncoded());

			// Generate signature
			String tosign = stringTS + stringPubKey;
			String sig = Crypto.encode(Crypto.makeDigitalSignature(tosign.getBytes(), clientprivKey));

			Response postResponse = userTarget.request().header(SIGNATURE_HEADER_NAME, sig)
					.header(PUBLIC_KEY_HEADER_NAME, stringPubKey).header(TIME_STAMP_HEADER_NAME, stringTS)
					.post(Entity.json(null));

			if (postResponse.getStatus() == 201) {
				System.out.println(SUCCESS_MSG);
			} else if (postResponse.getStatus() == 409) {
				System.out.println(ALREADY_EXISTS_MSG);
				throw new AlreadyExistsException("This public key already exists in the server");
			} else if (postResponse.getStatus() == 500) {
				System.out.println(SERVER_ERROR_MSG);
			} else {
				System.out.println(ELSE_MSG);
			}
		} catch (KeyStoreException | InvalidKeyException      | 
				NoSuchAlgorithmException | SignatureException | 
				UnrecoverableKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}

	}

	public void save_password(String domain, String username, String password) {
		if (domain == null || username == null || password == null) {
			throw new InvalidArgumentException("One of the arguments of the init method was null");
		}
		if (password.length() >= 246) {
			throw new InvalidArgumentException("Password to big to the system 245 bytes maximum");
		}
		try {
			Certificate cert = ks.getCertificate(aliasForPubPrivKeys);
			PublicKey clientPubKey = Crypto.getPublicKeyFromCertificate(cert);
			PrivateKey clientprivKey = Crypto.getPrivateKeyFromKeystore(ks, aliasForPubPrivKeys, keyStorePw);

			Certificate serverCert = ks.getCertificate(ALIAS_FOR_SERVER_PUB_KEY);
			PublicKey serverPubKey = Crypto.getPublicKeyFromCertificate(serverCert);

			// --------Initial hashs and timestamp
			byte[] hashDomain = Crypto.hashString(domain);
			byte[] hashUsername = Crypto.hashString(username);
			byte[] hashPassword = Crypto.hashString(password);
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String stringTs = timestamp.toString();
			// --------

			// ---------Creation of the string used to make the signature to use
			// in the header
			String stringHashDomain = new String(hashDomain);
			String stringHashUsername = new String(hashUsername);

			// --------Ciphered hashs and string conversion for them
			byte[] cipherDomain = Crypto.cipherString(stringHashDomain, serverPubKey);
			byte[] cipherUsername = Crypto.cipherString(stringHashUsername, serverPubKey);
			byte[] cipherPassword = Crypto.cipherString(password, clientPubKey);
			byte[] cipherHashPassword = Crypto.cipherString(new String(hashPassword), clientprivKey);

			String StringCipheredDomain = Crypto.encode(cipherDomain);
			String StringCipheredUsername = Crypto.encode(cipherUsername);
			String StringCipheredPassword = Crypto.encode(cipherPassword);
			String headerHashPassword = Crypto.encode(cipherHashPassword);
			// ---------

			String dataToSign = stringHashUsername + stringHashDomain + stringTs + headerHashPassword
					+ StringCipheredPassword;

			String sig = Crypto.encode(Crypto.makeDigitalSignature(dataToSign.getBytes(), clientprivKey));
			// ---------

			/*
			 * The triplet { domain: BASE64 encode({{Hash(Domain)}Public key
			 * Server) username: BASE64 encode({Hash(User)}Public key Server)
			 * password: BASE64 encode({Password}Public key Client) }
			 */
			CommonTriplet commonTriplet = new CommonTriplet(StringCipheredPassword, StringCipheredUsername,
					StringCipheredDomain);
			// -------
			String stringPubKey = Crypto.encode(clientPubKey.getEncoded());

			Response postResponse = vaultTarget.request().header(PUBLIC_KEY_HEADER_NAME, stringPubKey)
					.header(SIGNATURE_HEADER_NAME, sig).header(TIME_STAMP_HEADER_NAME, stringTs)
					.header(HASH_PASSWORD_HEADER_NAME, headerHashPassword).post(Entity.json(commonTriplet));

			System.out.println("Status:  " + postResponse.getStatus());

			if (postResponse.getStatus() == 201) {
				System.out.println(SUCCESS_MSG);
			} else if (postResponse.getStatus() == 400) {
				System.out.println(INVALID_ARG_MSG);
			} else if (postResponse.getStatus() == 403) {
				System.out.println(FORBIDEN_MSG);
				throw new UsernameAndDomainAlreadyExistException(
						"This combination of username and domain already exists");
			} else if (postResponse.getStatus() == 404) {
				System.out.println(DATA_NOT_FOUND_MSG);
				throw new DataNotFoundException("This public key is not registered in the server");
			} else if (postResponse.getStatus() == 500) {
				System.out.println(SERVER_ERROR_MSG);
			} else {
				System.out.println(ELSE_MSG);
			}
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | KeyStoreException
				| UnrecoverableKeyException e) {
			e.printStackTrace();
		}
	}

	// Tiago
	public String retrive_password(String domain, String username) {
		if (domain == null || username == null) {
			throw new InvalidArgumentException("One of the arguments of the init method was null");
		}
		try {
			// Get keys and certificates
			Certificate cert1 = ks.getCertificate(aliasForPubPrivKeys);
			PublicKey pubKeyClient = Crypto.getPublicKeyFromCertificate(cert1);
			Certificate cert2 = ks.getCertificate(ALIAS_FOR_SERVER_PUB_KEY);
			PublicKey pubKeyServer = Crypto.getPublicKeyFromCertificate(cert2);
			PrivateKey privateKey = Crypto.getPrivateKeyFromKeystore(ks, aliasForPubPrivKeys, keyStorePw);

			// Hash domain and username
			byte[] hashDomain = Crypto.hashString(domain);
			byte[] hashUsername = Crypto.hashString(username);
			String stringHashDomain = new String(hashDomain);
			String stringHashUsername = new String(hashUsername);

			// Generate timestamp
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String stringTS = timestamp.toString();

			// Cipher domain and username hash with server public key
			byte[] cipheredDomain = Crypto.cipherString(stringHashDomain, pubKeyServer);
			byte[] cipheredUsername = Crypto.cipherString(stringHashUsername, pubKeyServer);
			String encodedDomain = Crypto.encode(cipheredDomain);
			String encodedUsername = Crypto.encode(cipheredUsername);

			// Generate signature
			String tosign = stringHashUsername + stringHashDomain + stringTS;
			String sig = Crypto.encode(Crypto.makeDigitalSignature(tosign.getBytes(), privateKey));

			String stringPubKey = Crypto.encode(pubKeyClient.getEncoded());
			Response getResponse = vaultTarget.request().header(PUBLIC_KEY_HEADER_NAME, stringPubKey)
					.header(SIGNATURE_HEADER_NAME, sig).header(TIME_STAMP_HEADER_NAME, stringTS)
					.header(DOMAIN_HEADER_NAME, encodedDomain).header(USERNAME_HEADER_NAME, encodedUsername).get();

			// Decipher password
			String passwordReceived = getResponse.readEntity(CommonTriplet.class).getPassword();
			String password = Crypto.decipherString(Crypto.decode(passwordReceived), privateKey);

			// Get headers info
			String sigToVerify = getResponse.getHeaderString(SIGNATURE_HEADER_NAME);
			stringTS = getResponse.getHeaderString(TIME_STAMP_HEADER_NAME);
			String encodedHashReceived = getResponse.getHeaderString(HASH_PASSWORD_HEADER_NAME);

			// Check timestamp freshness
			if (!Crypto.validTS(stringTS)) {
				System.out.println(INVALID_TIME_STAMP_MSG);
				return "Champog";
			}

			// Verify signature
			sig = stringTS + encodedHashReceived + passwordReceived;
			byte[] sigBytes = Crypto.decode(sigToVerify);
			if (!Crypto.verifyDigitalSignature(sigBytes, sig.getBytes(), pubKeyServer)) {
				System.out.println(INVALID_SIGNATURE_MSG);
				return "Champog";
			}

			// Verify if password's hash is correct
			byte[] hashToVerify = Crypto.hashString(password);
			byte[] cipheredHashReceived = Crypto.decode(encodedHashReceived);
			String hashReceived = Crypto.decipherString(cipheredHashReceived, pubKeyClient);
			if (!hashReceived.equals(new String(hashToVerify))) {
				System.out.println(INVALID_PASSWORD_MSG);
				return "Champog";
			}

			if (getResponse.getStatus() == 200) {
				System.out.println(SUCCESS_MSG);
			} else if (getResponse.getStatus() == 400) {
				System.out.println(INVALID_ARG_MSG);
				return "Champog";
			} else if (getResponse.getStatus() == 403) {
				System.out.println(FORBIDEN_MSG);
				throw new UsernameAndDomainAlreadyExistException(
						"This combination of username and domain already exists");
			} else if (getResponse.getStatus() == 404) {
				System.out.println(DATA_NOT_FOUND_MSG);
				return "Champog";
			} else if (getResponse.getStatus() == 500) {
				System.out.println(SERVER_ERROR_MSG);
				return "Champog";
			} else {
				System.out.println(ELSE_MSG);
				return "Champog";
			}
			return password;
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | KeyStoreException
				| UnrecoverableKeyException | ParseException e) {
			e.printStackTrace();
			return "Champog";
		}
	}

	public void close() {
		ks = null;
	}

	private void checkArguments() {
		Certificate cer1 = null;
		Certificate cer2 = null;
		try {
			cer1 = ks.getCertificate(aliasForPubPrivKeys);
			cer2 = ks.getCertificate(ALIAS_FOR_SERVER_PUB_KEY);
			Crypto.getPrivateKeyFromKeystore(ks, aliasForPubPrivKeys, keyStorePw);
		} catch (UnrecoverableKeyException | KeyStoreException e) {
			throw new InvalidArgumentException("Invalid Arguments on the init method");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (cer1 == null || cer2 == null) {
			throw new InvalidArgumentException("Invalid Arguments on the init method");
		}
	}

	public KeyStore getKs() {
		return ks;
	}

	public void setKs(KeyStore ks) {
		this.ks = ks;
	}
}
