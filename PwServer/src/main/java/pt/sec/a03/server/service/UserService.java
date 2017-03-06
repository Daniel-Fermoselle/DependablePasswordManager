package pt.sec.a03.server.service;

import java.sql.SQLException;

import pt.sec.a03.server.database.Database;
import pt.sec.a03.server.domain.User;

public class UserService {

	Database db = new Database();
	
	public User getUser(String id) throws SQLException{
		return db.getUser(id);
	}
}
