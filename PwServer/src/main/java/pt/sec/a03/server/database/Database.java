package pt.sec.a03.server.database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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
	public static final String MY_SQL_DB_DEFAULT = "jdbc:mysql://localhost:3306/experiments?useSSL=false";
	
	private Connection conn;
	private String id;
	
	public Database(){
		
	}

	public Database(String id) {
		this.id = id;
	}
	
	private void getConnection(){
		String mysqlDB = MY_SQL_DB_DEFAULT;
		String mysqlID = MYSQL_ID;
		String mysqlPW = MYSQL_PASSWORD;
		try {			
			String fileString = new String(Files.readAllBytes(Paths.get("metadata.in")), StandardCharsets.UTF_8);
			String[] args = fileString.split(",");
			mysqlDB = args[0].replace("\n", "");		
			mysqlID = args[1].replace("\n", "");	
			mysqlPW = args[2].replaceAll("\n", "");
		} catch (IOException e) {
		} finally{
			try {
				this.conn = DriverManager.getConnection(mysqlDB, mysqlID, mysqlPW);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}
	}

	public User getUserByPK(String publicKey) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();
		
		String publicKeyDB = "";
		long userID = 0;

		// Step 1: Execute a SQL SELECT query, the query result
		String strSelect = "select userID, publicKey from Users where publicKey = '" + publicKey + "'";

		// Step 2: Process the ResultSet by scrolling the cursor forward via
		ResultSet rset = stmt.executeQuery(strSelect);
		int rowCount = 0;
		while (rset.next()) { // Move the cursor to the next row
			userID = rset.getLong("userID");
			publicKeyDB = rset.getString("publicKey");
			++rowCount;
		}	
		
		this.conn.close();
		
		if (rowCount == 0) {
			return null;
		} else {
			return new User(userID, publicKeyDB);
		}
	}

	public void saveUser(String publicKey) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();
		
		String sqlInsert = "insert into Users(publicKey) values ('" + publicKey + "');";
		stmt.execute(sqlInsert);
		
		this.conn.close();
	}

	public Triplet getTriplet(String username, String domain) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();
		
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
		
		this.conn.close();
		
		if (rowCount == 0) {
			return null;
		} else {
			return new Triplet(tripletID, userID, password, usernameDB, domainDB, pwHash);
		}
	}

	public void saveTriplet(Triplet t, long userID) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();
		
		String sqlInsert = "insert into Vault(userID, pw, username, domain) values (" + userID + ", '" + t.getPassword()
				+ "', '" + t.getUsername() + "', '" + t.getDomain() + "');";
		stmt.execute(sqlInsert);
		
		this.conn.close();
	}

	public void updateTriplet(Triplet t) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();
		
		String strUpdate = "update Vault set pw='" + t.getPassword() + "' where domain='" + t.getDomain()
				+ "' and username='" + t.getUsername() + "';";
		stmt.executeUpdate(strUpdate);
		
		this.conn.close();
	}

	public void updateHash(long tripletID, String hashPw) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();
		
		String strUpdate = "update Vault set pwHash='" + hashPw + "' where tripletID='" + tripletID + "';";
		stmt.executeUpdate(strUpdate);
		
		this.conn.close();
	}

	public void runScript() throws SQLException, FileNotFoundException {
		//Get mysql conneciton
		getConnection();
		
		PrintStream originalStream = System.out;

		PrintStream dummyStream = new PrintStream(new OutputStream() {
			public void write(int b) {
			}
		});

		System.setOut(dummyStream);
		
		// Initialize object for ScripRunner
		ScriptRunner sr = new ScriptRunner(conn);

		// Give the input file to Reader
		Reader reader = new BufferedReader(new FileReader(SQL_SCRIPT_PATH));

		// Exctute script
		sr.runScript(reader);
		
		System.setOut(originalStream);
		
		conn.close();
	}

}
