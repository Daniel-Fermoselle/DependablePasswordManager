package pt.sec.a03.client_lib;

import java.security.KeyStore;
import java.security.PublicKey;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;

import pt.sec.a03.common_classes.CommonTriplet;


public class ClientLib 
{
	private KeyStore ks;
	private Client client = ClientBuilder.newClient();
	private WebTarget baseTarget = client.target("http://localhost:8080/PwServer/webapi/");
	private WebTarget vaultTarget = baseTarget.path("vault");
		
    public void init(KeyStore ks) {
    	this.ks = ks;
    }
    
    //Daniel
    public void register_user() {
    	
    }
    
    //Mar
    public void save_password(String domain, String username, String password) {
    	CommonTriplet commonTriplet = new CommonTriplet(password, username, domain);
    	PublicKey pubKey = null; // ks.getKey("PublicKey", "Pog123");
    	String stringPubKey = Base64.encodeBase64String(pubKey.getEncoded());
    	Response postResponse = vaultTarget
    			.request()
    			.header("public-key", stringPubKey)
    			.post(Entity.json(commonTriplet));
    	
		if (postResponse.getStatus() == 201) {
			System.out.println("Sucess");
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
    	return "String";
    }
    
    public void close() {
    	ks = null;
    }
}
