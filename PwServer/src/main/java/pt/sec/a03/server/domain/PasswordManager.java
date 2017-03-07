package pt.sec.a03.server.domain;

import java.sql.SQLException;

import pt.sec.a03.server.database.Database;

public class PasswordManager {
	
	public Triplet saveTriplet(Triplet t, String publicKey) throws Exception{
		if(publicKey == null || t.getPassword() == null || t.getUsername() == null || t.getDomain() == null) {
			throw new InvalidArgumentException("The arguments provided are not suitable to create a new password");
		}
		try {
			Database db = new Database();
			User u = db.getUserByPK(publicKey);
			Triplet newTriplet = db.getTriplet(t.getDomain(), t.getUsername());
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
			return db.getTriplet(t.getDomain(), t.getUsername());
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
	
}
