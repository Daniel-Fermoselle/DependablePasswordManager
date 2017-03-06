package pt.sec.a03.server.domain;

import java.sql.SQLException;

import pt.sec.a03.server.database.Database;
import pt.sec.a03.server.exception.AlreadyExistsException;
import pt.sec.a03.server.exception.DataNotFoundException;

public class PasswordManager {
	
	public void addUser(String publicKey) {
		try {
			Database db = new Database();
			if(db.userInDB(publicKey)) {
				throw new AlreadyExistsException("Already exists user with the following Public Key: " 
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
		if(db.userInDB(publicKey)) {
			
		}
		else {
			throw new DataNotFoundException("The user with the ID " + id + " doesn't exist in the server");
		}
		} catch (SQLException e) {
			
		}
	}

	public User getUserByPK(String publicKey) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void saveTriplet(Triplet t, String publicKey) throws SQLException{
		Database db = new Database();
		db.saveTriplet(t, publicKey);
	}

	
	
}
