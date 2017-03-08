package pt.sec.a03.server.domain;

import java.sql.SQLException;

import pt.sec.a03.server.database.Database;
import pt.sec.a03.server.exception.AlreadyExistsException;
import pt.sec.a03.server.exception.DataNotFoundException;
import pt.sec.a03.server.exception.InvalidArgumentException;

public class PasswordManager {
	
	public void addUser(String publicKey) {
		if(publicKey == null) {
			throw new InvalidArgumentException("The argument " + publicKey + " is not suitable to create a new user");
		}
		try {
			Database db = new Database();
			User user = db.getUserByPK(publicKey);
			if(user != null) {
				throw new AlreadyExistsException("Already exists in the server user with the following Public Key: " 
						+ publicKey);		
			}
			else {		
				db.saveUser(publicKey);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public User getUserByID(String id) {
		try {
			Database db = new Database();
			User user = db.getUserByID(id);
			if(user != null) {
				return user;
			}
			else {
				throw new DataNotFoundException("The user with the ID " + id + " doesn't exist in the server");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public User getUserByPK(String publicKey) {
		try {
			Database db = new Database();
			User user = db.getUserByPK(publicKey);
			if(user != null) {
				return user;
			}
			else {
				throw new DataNotFoundException("The user with the Public Key " + publicKey + " doesn't exist in the server");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public void updateUserWithID(String id, String publicKey) {
		try {
			Database db = new Database();
			User user = db.getUserByID(id);
			if(user != null) {
				db.updateUser(id, publicKey);
			}
			else {
				throw new DataNotFoundException("The user with the ID " + id + " doesn't exist in the server");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public Triplet getTriplet(String username, String domain) throws SQLException{
		Database db = new Database();
		Triplet t = db.getTriplet(username, domain);
		if(t==null){
			throw new InvalidArgumentException("Username: " + username + " or Domain: " + " invalid");
		}
		else{
			return t;
		}
	}
	
	public Triplet saveTriplet(Triplet t, String publicKey) throws Exception{
		if(publicKey == null || t.getPassword() == null || t.getUsername() == null || t.getDomain() == null) {
			throw new InvalidArgumentException("The arguments provided are not suitable to create a new password");
		}
		try {
			Database db = new Database();
			User u = db.getUserByPK(publicKey);
			Triplet newTriplet = db.getTriplet(t.getUsername(), t.getDomain());
			//Update password in database
			if(newTriplet != null){
				db.updateTriplet(newTriplet);
			}
			if(u == null) {
				throw new DataNotFoundException("The user with the public key " + publicKey + " doesn't exist in the server");
			}
			else {		
				//TODO refactor this to use ID instead of public key so we can avoid doing one select
				db.saveTriplet(t, u.getPublicKey());
			}
			return db.getTriplet(t.getUsername(), t.getDomain());
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
}
