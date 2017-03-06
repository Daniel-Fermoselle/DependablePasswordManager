package pt.sec.a03.server.service;

import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.User;

public class UserService {

	private PasswordManager pwm =  new PasswordManager();
	
	public User getUserByID(String id) {
		return pwm.getUserByID(id);
	}
	
	public User getUserByPK(String publicKey) {
		return pwm.getUserByPK(publicKey);
	}
	
	public void addUser(String publicKey) {
		pwm.addUser(publicKey);
	}
}
