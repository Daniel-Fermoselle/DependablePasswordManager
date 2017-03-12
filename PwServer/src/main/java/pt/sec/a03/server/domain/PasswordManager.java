package pt.sec.a03.server.domain;

import java.sql.SQLException;

import pt.sec.a03.server.database.Database;
import pt.sec.a03.server.exception.AlreadyExistsException;
import pt.sec.a03.server.exception.DataNotFoundException;
import pt.sec.a03.server.exception.ForbiddenAccessException;
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
	

	public Triplet getTriplet(String username, String domain) {
		try{
			Database db = new Database();
			Triplet t = db.getTriplet(username, domain);
			if(username==null || domain==null){
				throw new InvalidArgumentException("Username or domain invalid");
			}
			if(t==null){
				throw new DataNotFoundException("Username: " + username + " or Domain: " + domain + " not found");
			}
			else{
				return t;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public Triplet saveTriplet(Triplet t, String publicKey){
		if(publicKey == null || t == null || t.getPassword() == null || t.getUsername() == null || t.getDomain() == null) {
			throw new InvalidArgumentException("The arguments provided are not suitable to create a new password");
		}
		try {
			Database db = new Database();
			User u = db.getUserByPK(publicKey);
			Triplet newTriplet = db.getTriplet(t.getUsername(), t.getDomain());
			if(u == null) {
				throw new DataNotFoundException("The user with the public key " + publicKey + " doesn't exist in the server");
			}
			if(newTriplet != null){
				if(newTriplet.getUserID() == u.getUserID()){
					db.updateTriplet(t);
				}
				else{
					throw new ForbiddenAccessException("The user with the public key " + publicKey + " has no permissions to access this information");		
				}
			}			
			else{
				db.saveTriplet(t, u.getUserID());
			}
						
			return db.getTriplet(t.getUsername(), t.getDomain());
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public String getHash(String string, String string2) {
		// TODO Auto-generated method stub
		return null;
	}
}
