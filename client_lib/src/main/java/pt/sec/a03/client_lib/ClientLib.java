package pt.sec.a03.client_lib;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.sql.Timestamp;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;

import pt.sec.a03.client_lib.filters.LoggingFilter;
import pt.sec.a03.common_classes.CommonTriplet;
import pt.sec.a03.crypto.Crypto;

public class ClientLib {

	private KeyStore ks;
	private String aliasForPubKey;
	private String keyStorePw;
	private static final String aliasForServerPubKey = "server";
	private Client client = ClientBuilder.newClient().register(LoggingFilter.class);
	private WebTarget baseTarget = client.target("http://localhost:8080/PwServer/webapi/");
	private WebTarget vaultTarget = baseTarget.path("vault");
	private WebTarget userTarget = baseTarget.path("users");

	public void init(KeyStore ks, String aliasForPubKey, String keyStorePw) {
		this.ks = ks;
		this.aliasForPubKey = aliasForPubKey;//this should be called alias for client keystore
		this.keyStorePw = keyStorePw;
	}

	public void register_user() throws KeyStoreException {
		// Get PubKey from key store
		Certificate cert = ks.getCertificate(aliasForPubKey);
		PublicKey pubKey = Crypto.getPublicKeyFromCertificate(cert);

		String stringPubKey = Base64.encodeBase64String(pubKey.getEncoded());
		Response postResponse = userTarget.request().header("public-key", stringPubKey).post(Entity.json(null));

		if (postResponse.getStatus() == 201) {
			System.out.println("Success");
		} else if (postResponse.getStatus() == 400) {
			System.out.println("Invalid argument");
		} else if (postResponse.getStatus() == 404) {
			System.out.println("Data Not Found");
		} else if (postResponse.getStatus() == 500) {
			System.out.println("Internal server error");
		} else {
			System.out.println("Error");
		}
	}

	// Mar
	public void save_password(String domain, String username, String password) throws KeyStoreException {
		try{
			Certificate cert = ks.getCertificate(aliasForPubKey);
			PublicKey clientPubKey = Crypto.getPublicKeyFromCertificate(cert);
			PrivateKey clientprivKey = Crypto.getPrivateKeyFromKeystore(ks, aliasForPubKey, keyStorePw);
	
			Certificate serverCert = ks.getCertificate(aliasForServerPubKey);
			PublicKey serverPubKey = Crypto.getPublicKeyFromCertificate(serverCert);
			
			
	
			//--------Initial hashs and timestamp
			byte[] hashDomain = Crypto.hashString(domain);
			byte[] hashUsername = Crypto.hashString(username);
			byte[] hashPassword = Crypto.hashString(password);
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			//--------
			
			//--------Ciphered hashs and string conversion for them and the timestamp
		    byte[] cipherDomain = Crypto.cipherString(new String(hashDomain), serverPubKey);
		    byte[] cipherUsername = Crypto.cipherString(new String(hashUsername), serverPubKey);
		    //byte[] cipherPassword = Crypto.cipherString(new String(hashPassword), clientprivKey);
		    byte[] cipherPassword = Crypto.cipherString(password, clientprivKey);
		    String StringCipheredDomain = Crypto.encode(cipherDomain);
		    String StringCipheredUsername = Crypto.encode(cipherUsername);
		    String StringCipheredPassword = Crypto.encode(cipherPassword);
		    String stringTs = timestamp.toString();
		    byte[] cipherHashPassword = Crypto.cipherString(new String(hashPassword), clientprivKey);
		    String headerHashPassword = Crypto.encode(cipherHashPassword);
		    //---------
		    
		    //---------Creation of the string to use to make the signature to use in the header
		    String stringHashDomain = new String(hashDomain);
		    String stringHashUsername = new String(hashUsername);
		    //String stringHashPassword = new String(hashPassword);
		    String stringcipheredPassword = new String(cipherPassword);
		   
		    
		    String dataToSign = stringHashUsername + stringHashDomain + stringTs + headerHashPassword + 
		    		stringcipheredPassword;
		    
		    String sig = Crypto.encode(Crypto.makeDigitalSignature(dataToSign.getBytes(), clientprivKey));
		    //---------
		    
		    /* The triplet
		      	{
				domain: BASE64 encode({{Hash(Domain)}Public key Server)
				username: BASE64 encode({Hash(User)}Public key Server)
				password: BASE64 encode({Password}Public key Client)
				}
			*/
			CommonTriplet commonTriplet = new CommonTriplet(StringCipheredPassword,
					StringCipheredUsername, StringCipheredDomain);
			//-------
		    
			//String stringPubKey = Base64.encodeBase64String(pubKey.getEncoded());
		    String stringPubKey = Crypto.encode(clientPubKey.getEncoded());
			Response postResponse = vaultTarget.request()
					.header("signature", sig)
					.header("public-key", stringPubKey)
					.header("timestamp", stringTs)
					.header("hash-password", headerHashPassword)
					.post(Entity.json(commonTriplet));
	
			if (postResponse.getStatus() == 201) {
				System.out.println("Success");
			} else if (postResponse.getStatus() == 400) {
				System.out.println("Invalid argument");
			} else if (postResponse.getStatus() == 404) {
				System.out.println("Data Not Found");
			} else if (postResponse.getStatus() == 500) {
				System.out.println("Internal server error");
			} else {
				System.out.println("Error");
			}
		}
		catch (NoSuchAlgorithmException e) {
		    e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Tiago
	public String retrive_password(String domain, String username) throws KeyStoreException {
		Certificate cert = ks.getCertificate(aliasForPubKey);
		PublicKey pubKey = Crypto.getPublicKeyFromCertificate(cert);

		String stringPubKey = Base64.encodeBase64String(pubKey.getEncoded());
		Response getResponse = vaultTarget.request().header("public-key", stringPubKey).header("domain", domain)
				.header("username", username).get();

		if (getResponse.getStatus() == 200) {
			System.out.println("Success");
		} else if (getResponse.getStatus() == 400) {
			System.out.println("Invalid argument");
		} else if (getResponse.getStatus() == 404) {
			System.out.println("Data Not Found");
		} else if (getResponse.getStatus() == 500) {
			System.out.println("Internal server error");
		} else {
			System.out.println("Error");
		}
		return getResponse.readEntity(CommonTriplet.class).getPassword();
	}

	public void close() {
		ks = null;
	}
}
