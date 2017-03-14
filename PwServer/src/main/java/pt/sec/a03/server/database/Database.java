package pt.sec.a03.server.database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

	public Database() {
	}

	// TODO PublicKey instead of String
	public User getUserByPK(String publicKey) throws SQLException {
		String publicKeyDB = "";
		long userID = 0;
		// Step 1: Allocate a database "Connection" object
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/experiments?useSSL=false", MYSQL_ID,
				MYSQL_PASSWORD); // MySQL

		// Step 2: Allocate a "Statement" object in the Connection
		Statement stmt = conn.createStatement();

		// Step 3: Execute a SQL SELECT query, the query result
		String strSelect = "select userID, publicKey from Users where publicKey = '" + publicKey + "'";

		// Step 4: Process the ResultSet by scrolling the cursor forward via
		// next().
		ResultSet rset = stmt.executeQuery(strSelect);
		int rowCount = 0;
		while (rset.next()) { // Move the cursor to the next row
			userID = rset.getLong("userID");
			publicKeyDB = rset.getString("publicKey");
			++rowCount;
		}
		if (rowCount == 0) {
			return null;
		} else {
			return new User(userID, publicKeyDB);
		}
	}

	public User getUserByID(String id) throws SQLException {
		String publicKeyDB = "";
		long userID = 0;
		// Step 1: Allocate a database "Connection" object
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/experiments?useSSL=false", MYSQL_ID,
				MYSQL_PASSWORD); // MySQL

		// Step 2: Allocate a "Statement" object in the Connection
		Statement stmt = conn.createStatement();

		// Step 3: Execute a SQL SELECT query, the query result
		String strSelect = "select userID, publicKey from Users where userID = '" + id + "'";

		// Step 4: Process the ResultSet by scrolling the cursor forward via
		// next().
		ResultSet rset = stmt.executeQuery(strSelect);
		int rowCount = 0;
		while (rset.next()) { // Move the cursor to the next row
			userID = rset.getLong("userID");
			publicKeyDB = rset.getString("publicKey");
			++rowCount;
		}
		if (rowCount == 0) {
			return null;
		} else {
			return new User(userID, publicKeyDB);
		}
	}

	// TODO PublicKey instead of String
	public void saveUser(String publicKey) throws SQLException {
		// Step 1: Allocate a database "Connection" object
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/experiments?useSSL=false", MYSQL_ID,
				MYSQL_PASSWORD); // MySQL

		// Step 2: Allocate a "Statement" object in the Connection
		Statement stmt = conn.createStatement();

		// Step 3: Execute a SQL DELETE query, the query result
		String sqlInsert = "insert into Users(publicKey) values ('" + publicKey + "');";
		stmt.execute(sqlInsert);
	}

	public void updateUser(String id, String publicKey) throws SQLException {

		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/experiments?useSSL=false", MYSQL_ID,
				MYSQL_PASSWORD); // MySQL

		// Step 2: Allocate a "Statement" object in the Connection
		Statement stmt = conn.createStatement();

		String strUpdate = "update Users set publicKey='" + publicKey + "' where userID='" + id + "';";
		stmt.executeUpdate(strUpdate);
	}

	public Triplet getTriplet(String username, String domain) throws SQLException {
		String password = "", usernameDB = "", domainDB = "", pwHash = "";
		long tripletID = 0, userID = 0;
		// Step 1: Allocate a database "Connection" object
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/experiments?useSSL=false", MYSQL_ID,
				MYSQL_PASSWORD); // MySQL

		// Step 2: Allocate a "Statement" object in the Connection
		Statement stmt = conn.createStatement();

		// Step 3: Execute a SQL SELECT query, the query result
		String strSelect = "select tripletID, userID, pw, username, domain, pwHash from Vault where domain = '" + domain
				+ "' and username = '" + username + "';";

		// Step 4: Process the ResultSet by scrolling the cursor forward via
		// next().
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

	// TODO PublicKey instead of String
	public void saveTriplet(Triplet t, long userID) throws SQLException {
		// Step 1: Allocate a database "Connection" object
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/experiments?useSSL=false", MYSQL_ID,
				MYSQL_PASSWORD); // MySQL

		// Step 2: Allocate a "Statement" object in the Connection
		Statement insert = conn.createStatement();

		// Step 3: Execute a SQL INSERT query, the query result
		String sqlInsert = "insert into Vault(userID, pw, username, domain) values (" + userID + ", '" + t.getPassword()
				+ "', '" + t.getUsername() + "', '" + t.getDomain() + "');";
		insert.execute(sqlInsert);
	}

	public void updateTriplet(Triplet t) throws SQLException {
		// Step 1: Allocate a database "Connection" object
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/experiments?useSSL=false", MYSQL_ID,
				MYSQL_PASSWORD); // MySQL

		// Step 2: Allocate a "Statement" object in the Connection
		Statement stmt = conn.createStatement();

		// Step 3 & 4: Execute a SQL UPDATE via executeUpdate()
		String strUpdate = "update Vault set pw='" + t.getPassword() + "' where domain='" + t.getDomain()
				+ "' and username='" + t.getUsername() + "';";
		stmt.executeUpdate(strUpdate);
	}

	public void updateHash(long tripletID, String hashPw) throws SQLException {
		// Step 1: Allocate a database "Connection" object
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/experiments?useSSL=false", MYSQL_ID,
				MYSQL_PASSWORD); // MySQL

		// Step 2: Allocate a "Statement" object in the Connection
		Statement stmt = conn.createStatement();

		// Step 3 & 4: Execute a SQL UPDATE via executeUpdate()
		String strUpdate = "update Vault set pwHash='" + hashPw + "' where tripletID='" + tripletID + "';";
		stmt.executeUpdate(strUpdate);
	}

	public void runScript() throws SQLException, FileNotFoundException {
		// database connection
		Connection jdbcConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/experiments?useSSL=false",
				MYSQL_ID, MYSQL_PASSWORD);

		// Initialize object for ScripRunner
		ScriptRunner sr = new ScriptRunner(jdbcConnection);

		// Give the input file to Reader
		Reader reader = new BufferedReader(new FileReader(SQL_SCRIPT_PATH));

		// Exctute script
		sr.runScript(reader);

	}

}
