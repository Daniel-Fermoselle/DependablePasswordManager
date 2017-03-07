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
	
	public void saveTriplet(Triplet t, String publicKey) throws SQLException{
		Database db = new Database();
		db.saveTriplet(t, publicKey);
	}

	

	
	
}
