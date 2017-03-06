package pt.sec.a03.server.domain;

import java.sql.SQLException;

import pt.sec.a03.server.database.Database;

public class PasswordManager {

	public void saveTriplet(Triplet t) throws SQLException{
		Database db = new Database();
		db.saveTriplet(t);
	}
	
}
