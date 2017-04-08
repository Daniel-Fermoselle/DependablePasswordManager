package pt.sec.a03.server.database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.ibatis.jdbc.ScriptRunner;

import pt.sec.a03.server.domain.User;
import pt.sec.a03.server.domain.Triplet;

public class Database {

	public static final String MYSQL_ID = "root";
	public static final String MYSQL_PASSWORD = "rootroot";
	public static final String SQL_SCRIPT_PATH = "database.sql";
	public static final String MY_SQL_DB = "jdbc:mysql://localhost:3306/experiments?useSSL=false";

	private Connection conn;
	private Statement stmt;

	public Database() {
		try {
			conn = DriverManager.getConnection(MY_SQL_DB, MYSQL_ID, MYSQL_PASSWORD);
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public User getUserByPK(String publicKey) throws SQLException {
		String publicKeyDB = "";
		long userID = 0;
		long nonce = 0;

		// Step 1: Execute a SQL SELECT query, the query result
		String strSelect = "select userID, publicKey, nonce from Users where publicKey = '" + publicKey + "'";

		// Step 2: Process the ResultSet by scrolling the cursor forward via
		ResultSet rset = stmt.executeQuery(strSelect);
		int rowCount = 0;
		while (rset.next()) { // Move the cursor to the next row
			userID = rset.getLong("userID");
			publicKeyDB = rset.getString("publicKey");
			nonce = rset.getLong("nonce");
			++rowCount;
		}
		if (rowCount == 0) {
			return null;
		} else {
			return new User(userID, publicKeyDB, nonce);
		}
	}

	public User getUserByID(String id) throws SQLException {
		String publicKeyDB = "";
		long userID = 0;
		long nonce = 0;


		// Step 1: Execute a SQL SELECT query, the query result
		String strSelect = "select userID, publicKey, nonce from Users where userID = '" + id + "'";

		// Step 2: Process the ResultSet by scrolling the cursor forward via
		ResultSet rset = stmt.executeQuery(strSelect);
		int rowCount = 0;
		while (rset.next()) { // Move the cursor to the next row
			userID = rset.getLong("userID");
			publicKeyDB = rset.getString("publicKey");
			nonce = rset.getLong("nonce");
			++rowCount;
		}
		if (rowCount == 0) {
			return null;
		} else {
			return new User(userID, publicKeyDB, nonce);
		}
	}

	public void saveUser(String publicKey) throws SQLException {
		String sqlInsert = "insert into Users(publicKey) values ('" + publicKey + "');";
		stmt.execute(sqlInsert);
	}

	public void updateUser(String id, String publicKey) throws SQLException {
		String strUpdate = "update Users set publicKey='" + publicKey + "' where userID='" + id + "';";
		stmt.executeUpdate(strUpdate);
	}

	public void updateUserNonce(String id, long nonce) throws SQLException {
		String strUpdate = "update Users set nonce='" + nonce + "' where userID='" + id + "';";
		stmt.executeUpdate(strUpdate);
	}

	public Triplet getTriplet(String username, String domain) throws SQLException {
		String password = "", usernameDB = "", domainDB = "", pwHash = "";
		long tripletID = 0, userID = 0;

		// Step 1: Execute a SQL SELECT query, the query result
		String strSelect = "select tripletID, userID, pw, username, domain, pwHash from Vault where domain = '" + domain
				+ "' and username = '" + username + "';";

		// Step 2: Process the ResultSet by scrolling the cursor forward via
		ResultSet rset = stmt.executeQuery(strSelect);
		int rowCount = 0;
		while (rset.next()) { // Move the cursor to the next row
			tripletID = rset.getLong("tripletID");
			userID = rset.getLong("userID");
			password = rset.getString("pw");
			usernameDB = rset.getString("username");
			domainDB = rset.getString("domain");
			pwHash = rset.getString("pwHash");
			++rowCount;
		}
		if (rowCount == 0) {
			return null;
		} else {
			return new Triplet(tripletID, userID, password, usernameDB, domainDB, pwHash);
		}
	}

	public void saveTriplet(Triplet t, long userID) throws SQLException {
		String sqlInsert = "insert into Vault(userID, pw, username, domain) values (" + userID + ", '" + t.getPassword()
				+ "', '" + t.getUsername() + "', '" + t.getDomain() + "');";
		stmt.execute(sqlInsert);
	}

	public void updateTriplet(Triplet t) throws SQLException {
		String strUpdate = "update Vault set pw='" + t.getPassword() + "' where domain='" + t.getDomain()
				+ "' and username='" + t.getUsername() + "';";
		stmt.executeUpdate(strUpdate);
	}

	public void updateHash(long tripletID, String hashPw) throws SQLException {
		String strUpdate = "update Vault set pwHash='" + hashPw + "' where tripletID='" + tripletID + "';";
		stmt.executeUpdate(strUpdate);
	}

	public void runScript() throws SQLException, FileNotFoundException {
		// Initialize object for ScripRunner
		ScriptRunner sr = new ScriptRunner(conn);

		// Give the input file to Reader
		Reader reader = new BufferedReader(new FileReader(SQL_SCRIPT_PATH));

		// Exctute script
		sr.runScript(reader);
	}

}
