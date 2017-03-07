package pt.sec.a03.server.service;

import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.Triplet;

public class VaultService {

	public Triplet put(String publicKey, String password, String username, String domain) throws Exception{
		PasswordManager pwm =  new PasswordManager();
		return pwm.saveTriplet(new Triplet(password, username, domain), publicKey);
	}
	
}
