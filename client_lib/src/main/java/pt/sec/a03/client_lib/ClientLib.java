package pt.sec.a03.client_lib;

import java.security.KeyStore;


public class ClientLib 
{
	private KeyStore ks;
	
    public void init(KeyStore ks) {
    	this.ks = ks;
    }
    
    //Daniel
    public void register_user() {
    	
    }
    
    //Mar
    public void save_password(String domain, String username, String password) {
    	
    }
    
    //Tiago
    public String retrive_password(String domain, String username) {
    	return "String";
    }
    
    public void close() {
    	ks = null;
    }
}
