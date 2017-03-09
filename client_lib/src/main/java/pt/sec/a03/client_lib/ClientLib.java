package pt.sec.a03.client_lib;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;

import pt.sec.a03.common_classes.CommonTriplet;
import pt.sec.a03.crypto.Crypto;


public class ClientLib 
{
	
	private KeyStore ks;
	private String aliasForPubKey;
	private Client client = ClientBuilder.newClient();
	private WebTarget baseTarget = client.target("http://localhost:8080/PwServer/webapi/");
	private WebTarget vaultTarget = baseTarget.path("vault");
	private WebTarget userTarget = baseTarget.path("users");
		
    public void init(KeyStore ks, String aliasForPubKey) {
    	this.ks = ks;
    	this.aliasForPubKey = aliasForPubKey;
    }
    
    public void register_user() throws KeyStoreException {
    	//Get PubKey from key store
    	Certificate cert = ks.getCertificate(aliasForPubKey);
    	PublicKey pubKey = Crypto.getPublicKeyFromCertificate(cert);
    	
    	String stringPubKey = Base64.encodeBase64String(pubKey.getEncoded());
    	Response postResponse = userTarget
    			.request()
    			.header("public-key", stringPubKey)
    			.post(Entity.json(null));
    	
		if (postResponse.getStatus() == 201) {
			System.out.println("Success");
		}
		else if (postResponse.getStatus() == 400) {
			System.out.println("Invalid argument");
		}
		else if (postResponse.getStatus() == 404) {
			System.out.println("Data Not Found");
		}
		else if (postResponse.getStatus() == 500) {
			System.out.println("Internal server error");
		}
		else {
			System.out.println("Error");
		}
    }
    
    //Mar
    public void save_password(String domain, String username, String password) {
    	CommonTriplet commonTriplet = new CommonTriplet(password, username, domain);
    	PublicKey pubKey = null; // ks.getKey("PublicKey", "Pog123");
    	String stringPubKey = "123";//Base64.encodeBase64String(pubKey.getEncoded());
    	Response postResponse = vaultTarget
    			.request()
    			.header("public-key", stringPubKey)
    			.post(Entity.json(commonTriplet));
    	
		if (postResponse.getStatus() == 201) {
			System.out.println("Success");
		}
		else if (postResponse.getStatus() == 400) {
			System.out.println("Invalid argument");
		}
		else if (postResponse.getStatus() == 404) {
			System.out.println("Data Not Found");
		}
		else if (postResponse.getStatus() == 500) {
			System.out.println("Internal server error");
		}
		else {
			System.out.println("Error");
		}
    }
    
    //Tiago
    public String retrive_password(String domain, String username) {
    	PublicKey pubKey = null; // ks.getKey("PublicKey", "Pog123");
    	String stringPubKey = "123";//Base64.encodeBase64String(pubKey.getEncoded());
    	Response getResponse = vaultTarget
    			.request()
    			.header("public-key", stringPubKey).header("domain", domain).header("username", username)
    			.get();
    	
		if (getResponse.getStatus() == 200) {
			System.out.println("Success");
		}
		else if (getResponse.getStatus() == 400) {
			System.out.println("Invalid argument");
		}
		else if (getResponse.getStatus() == 404) {
			System.out.println("Data Not Found");
		}
		else if (getResponse.getStatus() == 500) {
			System.out.println("Internal server error");
		}
		else {
			System.out.println("Error");
		}
		return getResponse.readEntity(CommonTriplet.class).getPassword();
    }
    
    public void close() {
    	ks = null;
    }
}
