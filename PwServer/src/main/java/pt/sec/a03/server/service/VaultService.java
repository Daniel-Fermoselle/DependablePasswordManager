package pt.sec.a03.server.service;

import java.sql.SQLException;

import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.Triplet;

public class VaultService {

	public void put(String publicKey, String password, String username, String domain) throws SQLException{
		PasswordManager pwm =  new PasswordManager();
		pwm.saveTriplet(new Triplet(password, username, domain), publicKey);
	}
	
}
