package pt.sec.a03.server.domain;

import java.sql.SQLException;

import pt.sec.a03.server.database.Database;

public class PasswordManager {

	
	//TODO I think that the database should be put in a constructor of the vault service
	public void saveTriplet(Triplet t, String publicKey) throws SQLException{
		Database db = new Database();
		db.saveTriplet(t, publicKey);
	}
	
	public Triplet getTriplet(String username, String domain) throws SQLException{
		Database db = new Database();
		Triplet t = db.getTriplet(username, domain);
		return t;
	}
	
}
