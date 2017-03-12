package pt.sec.a03.client_lib;

import java.security.KeyStore;
import java.security.KeyStoreException;
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
	private Client client = ClientBuilder.newClient().register(LoggingFilter.class);
	private WebTarget baseTarget = client.target("http://localhost:8080/PwServer/webapi/");
	private WebTarget vaultTarget = baseTarget.path("vault");
	private WebTarget userTarget = baseTarget.path("users");

	public void init(KeyStore ks, String aliasForPubKey) {
		this.ks = ks;
		this.aliasForPubKey = aliasForPubKey;
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
		CommonTriplet commonTriplet = new CommonTriplet(password, username, domain);
		Certificate cert = ks.getCertificate(aliasForPubKey);
		PublicKey pubKey = Crypto.getPublicKeyFromCertificate(cert);

		String stringPubKey = Base64.encodeBase64String(pubKey.getEncoded());
		Response postResponse = vaultTarget.request().header("public-key", stringPubKey)
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

	// Tiago
	public String retrive_password(String domain, String username) {
		try{
			//Get keys and certificates
			Certificate cert1 = ks.getCertificate(aliasForPubKey);
			PublicKey pubKeyClient = Crypto.getPublicKeyFromCertificate(cert1);
		    Certificate cert2 = ks.getCertificate("server");
		    PublicKey pubKeyServer = Crypto.getPublicKeyFromCertificate(cert2);
			PrivateKey privateKey = Crypto.getPrivateKeyFromKeystore(ks, "client", "insecure");
			
			//Hash domain and username
		    byte[] hashDomain = Crypto.hashString(domain);
		    byte[] hashUsername = Crypto.hashString(username);
		    String stringHashDomain = new String(hashDomain);
		    String stringHashUsername = new String(hashUsername);
		    
		    //Generate timestamp
		    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		    String stringTS = timestamp.toString();
		    
		    //Cipher domain and username hash with server public key
		    byte[] cipheredDomain = Crypto.cipherString(stringHashDomain, pubKeyServer);
		    byte[] cipheredUsername = Crypto.cipherString(stringHashUsername, pubKeyServer);
		    String encodedDomain = Crypto.encode(cipheredDomain);
		    String encodedUsername = Crypto.encode(cipheredUsername);		    
		    
		    //Generate signature
		    String tosign = stringHashDomain + stringHashUsername + stringTS;
		    String sig = Crypto.encode(Crypto.makeDigitalSignature(tosign.getBytes(), privateKey));
		    
			String stringPubKey = Crypto.encode(pubKeyClient.getEncoded());
			Response getResponse = vaultTarget.request()
					.header("public-key", stringPubKey)
					.header("signature", sig)
					.header("timestamp", stringTS)
					.header("domain",  encodedDomain)
					.header("username",  encodedUsername).get();
			
			//Decipher password
			String passwordReceived = getResponse.readEntity(CommonTriplet.class).getPassword();
			String password = Crypto.decipherString(Crypto.decode(passwordReceived),
					privateKey);

			//Get headers info
			String sigToVerify = getResponse.getHeaderString("signature");
			stringTS = getResponse.getHeaderString("timestamp");
			String encodedHashReceived = getResponse.getHeaderString("hash");			
			
			//Check timestamp freshness
			if(!Crypto.validTS(stringTS)){
				System.out.println("Freshness compromised");
				return "Champog";
			}
			
			//Verify signature
			sig = stringTS + encodedHashReceived + passwordReceived;
			byte[] sigBytes = Crypto.decode(sigToVerify);
		    if(Crypto.verifyDigitalSignature(sigBytes, sig.getBytes(), pubKeyServer)){
				System.out.println("Signature compromised");
				return "Champog";
		    }
		    
		    //Verify if password's hash is correct
		    byte[] hashToVerify = Crypto.hashString(password);
		    byte[] cipheredHashReceived = Crypto.decode(encodedHashReceived);
		    String hashReceived = new String(Crypto.decipherString(cipheredHashReceived, pubKeyClient));
		    if(!hashReceived.equals(new String(hashToVerify))){
		    	System.out.println("Passwords don't match");
		    	return "Champog";
		    }
			
			if (getResponse.getStatus() == 200) {
				System.out.println("Success");
			} else if (getResponse.getStatus() == 400) {
				System.out.println("Invalid argument");
				return "Champog";
			} else if (getResponse.getStatus() == 404) {
				System.out.println("Data Not Found");
				return "Champog";
			} else if (getResponse.getStatus() == 500) {
				System.out.println("Internal server error");
				return "Champog";
			} else {
				System.out.println("Error");
				return "Champog";
			}
			return password;
		}
		catch(Exception e){
			e.printStackTrace();
			return "Champog";
		}
	}

	public void close() {
		ks = null;
	}
}
