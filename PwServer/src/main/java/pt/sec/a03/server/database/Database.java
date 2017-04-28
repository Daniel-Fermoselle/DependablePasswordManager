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

import pt.sec.a03.common_classes.Bonrr;
import pt.sec.a03.server.MyApplication;
import pt.sec.a03.server.domain.User;
import pt.sec.a03.server.domain.Triplet;

public class Database {

	public static final String MYSQL_ID = "root";
	public static final String MYSQL_PASSWORD = "rootroot";
	public static final String SQL_SCRIPT_PATH = "database.sql";
	public static final String MY_SQL_DB_DEFAULT = "jdbc:mysql://localhost:3306/experiments?useSSL=false";

	private static final String DATABASE_URI = "jdbc:mysql://localhost:3306/%s?useSSL=false";

	private Connection conn;

	public Database(){
		
	}
	
	private void getConnection(){
		String mysqlDB = MY_SQL_DB_DEFAULT;
		String mysqlID = MYSQL_ID;
		String mysqlPW = MYSQL_PASSWORD;
		try {
			if(MyApplication.PORT == null){
				throw new IOException("Port variable is null");
			}
			String[] args = getDBParams(MyApplication.PORT);
			mysqlDB = String.format(DATABASE_URI, args[1]);
			mysqlID = args[2];
			mysqlPW = args[3].replaceAll("\n", "");
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
		this.conn.close();
		if (rowCount == 0) {
			return null;
		} else {
			return new User(userID, publicKeyDB, nonce);
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

	public void updateUser(String id, String publicKey) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();
		
		String strUpdate = "update Users set publicKey='" + publicKey + "' where userID='" + id + "';";
		stmt.executeUpdate(strUpdate);
		
		this.conn.close();
	}

	public void updateUserNonce(String id, long nonce) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();
		
		String strUpdate = "update Users set nonce='" + nonce + "' where userID='" + id + "';";
		stmt.executeUpdate(strUpdate);

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
			return new Triplet(tripletID, userID, domainDB, usernameDB, password, pwHash);
		}
	}

	public void saveTriplet(Triplet t, long userID) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();
		
		String sqlInsert = "insert into Vault(userID, pw, username, domain, pwHash) values (" + userID + ", '" + t.getPassword()
				+ "', '" + t.getUsername() + "', '" + t.getDomain() + "', '" + t.getHash() + "');";
		stmt.execute(sqlInsert);
		
		this.conn.close();
	}

	public void updateTriplet(Triplet t) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();
		
		String strUpdate = "update Vault set pw='" + t.getPassword() + "' , pwHash='" + t.getHash() + "' where domain='"
				+ t.getDomain() + "' and username='" + t.getUsername() + "';";
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

		this.conn.close();
	}

	public String[] getDBParams(String port) throws IOException {
		String fileString = new String(Files.readAllBytes(Paths.get("metadata/metadata.in")), StandardCharsets.UTF_8);
		String[] args = fileString.split("\n");
		for (String arg : args) {
			if(arg.startsWith(port)){
				String[] split = arg.split(",");
				return split;
			}
		}
		throw new RuntimeException("No matching database to port");
	}

	public Bonrr getBonrr(String bonrr) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();

		String DBbonrr = "";
		long DBwts = 0;

		// Step 1: Execute a SQL SELECT query, the query result
		String strSelect = "select bonrr, MAX(wts) as wts from Bonrrs where bonrr = '" + bonrr + "';";

		// Step 2: Process the ResultSet by scrolling the cursor forward via
		ResultSet rset = stmt.executeQuery(strSelect);
		int rowCount = 0;
		while (rset.next()) { // Move the cursor to the next row
			DBbonrr = rset.getString("bonrr");
			DBwts = rset.getLong("wts");
			++rowCount;
		}
		this.conn.close();
		if (rowCount == 0) {
			return null;
		} else {
			return new Bonrr(DBbonrr, DBwts);
		}
	}
	
	public String[] getSpecificBonrr(long bonrr) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();
		String DBpw = "";
		String DBpwHash = "";

		// Step 1: Execute a SQL SELECT query, the query result
		String strSelect = "select bonrr, MAX(wts) as wts, pw, pwHash from Bonrrs where bonrr = '" + bonrr + "';";

		// Step 2: Process the ResultSet by scrolling the cursor forward via
		ResultSet rset = stmt.executeQuery(strSelect);
		int rowCount = 0;
		while (rset.next()) { // Move the cursor to the next row
			DBpw = rset.getString("pw");
			DBpwHash = rset.getString("pwHash");
			++rowCount;
		}
		String [] returnStringArray = {DBpw, DBpwHash};
		this.conn.close();
		if (rowCount == 0) {
			return null;
		} else {
			return returnStringArray;
		}
	}

	public void saveBonrr(String bonrr, String wts, String signature, Triplet t) throws SQLException{
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();

		String sqlInsert = "insert into Bonrrs(bonrr, wts, signature, username, domain, pw, pwHash) " +
				" values ('" + bonrr + "', " + Long.parseLong(wts) + ", '" + signature + "', '" + t.getUsername() + "', '"
				+ t.getDomain() + "', '" + t.getPassword() + "', '" + t.getHash() + "');";

		stmt.execute(sqlInsert);

		this.conn.close();
	}
}
