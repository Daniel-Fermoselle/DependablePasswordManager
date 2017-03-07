package pt.sec.a03.server.service;

import java.sql.SQLException;

import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.Triplet;

public class VaultService {

	//TODO I think that the password manager should be put in a constructor of the vault service
	public Triplet put(String publicKey, String password, String username, String domain) throws Exception{
		PasswordManager pwm =  new PasswordManager();
		return pwm.saveTriplet(new Triplet(password, username, domain), publicKey);
	}
	
	public Triplet get(String publicKey, String username, String domain) throws SQLException{
		PasswordManager pwm =  new PasswordManager();
		Triplet t = pwm.getTriplet(username,domain);
		Triplet send = new Triplet();
		send.setPassword(t.getPassword());
		return send;
	}
	
}
