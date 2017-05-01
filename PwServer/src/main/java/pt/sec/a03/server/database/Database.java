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
		long nonce = 0;

		// Step 1: Execute a SQL SELECT query, the query result
		String strSelect = "select publicKey, nonce from Users where publicKey = '" + publicKey + "'";

		// Step 2: Process the ResultSet by scrolling the cursor forward via
		ResultSet rset = stmt.executeQuery(strSelect);
		int rowCount = 0;
		while (rset.next()) { // Move the cursor to the next row
			publicKeyDB = rset.getString("publicKey");
			nonce = rset.getLong("nonce");
			++rowCount;
		}
		this.conn.close();
		if (rowCount == 0) {
			return null;
		} else {
			return new User(publicKeyDB, nonce);
		}
	}

	public void saveUser(String publicKey) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();
		
		String sqlInsert = "insert into Users(publicKey, nonce) values ('" + publicKey + "'," + 0 + ");";
		stmt.execute(sqlInsert);
		
		this.conn.close();
	}

	public void updateNonce(String publicKey, long nonce) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();
		
		String strUpdate = "update Users set nonce='" + nonce + "' where publicKey='" + publicKey + "';";
		stmt.executeUpdate(strUpdate);

		this.conn.close();
	}

	public Bonrr getBonrrInstance(String bonrr, String domain, String username) throws SQLException {
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();

		String DBbonrr = "";
		long DBrank= 0;
		long DBwts = 0;

		// Step 1: Execute a SQL SELECT query, the query result
		String strSelect = "select bonrr, MAX(wts) as wts, rank from Bonrrs where bonrr = '" + bonrr + "' AND " +
				"domain = '" + domain + "' AND  username = '" + username + "' GROUP BY wts ORDER BY wts DESC;";

		// Step 2: Process the ResultSet by scrolling the cursor forward via
		ResultSet rset = stmt.executeQuery(strSelect);
		int rowCount = 0;
		while (rset.next()) { // Move the cursor to the next row
			DBbonrr = rset.getString("bonrr");
			DBwts = rset.getLong("wts");
			DBrank = rset.getLong("rank");
			++rowCount;
			break;
		}
		this.conn.close();
		if (rowCount == 0) {
			return null;
		} else {
			return new Bonrr(DBbonrr, DBwts, DBrank, domain, username);
		}
	}
	

	public boolean checkUserAndDomain(String bonrr, Triplet t) throws SQLException{
        getConnection();
        Statement stmt = this.conn.createStatement();
        String queryBonrr="";
        // Step 1: Execute a SQL SELECT query, the query result
        String strSelect = "select bonrr from Bonrrs where domain = '" + t.getDomain() + "' and username = '" + t.getUsername() + "';";

        // Step 2: Process the ResultSet by scrolling the cursor forward via
        ResultSet rset = stmt.executeQuery(strSelect);
        int rowCount = 0;
        while (rset.next()) { // Move the cursor to the next row
            queryBonrr=rset.getString("bonrr");
            ++rowCount;
            break;
        }
        this.conn.close();
        if(queryBonrr.equals("") || queryBonrr.equals(bonrr)){
        	return true;
        }
		return false;
	}

	public void saveBonrr(String bonrr, Triplet t) throws SQLException{
		//Get mysql conneciton
		getConnection();
		Statement stmt = this.conn.createStatement();

		String sqlInsert = "insert into Bonrrs(bonrr, wts, username, domain, rank, signature, pw, pwHash) " +
                " values ('" + bonrr + "', " + t.getWts() + ", '" + t.getUsername() + "', '"
                + t.getDomain() + "', " + t.getRank() + ", '" + t.getSignature() + "', '" + t.getPassword() + "', '" + t.getHash() + "');";

		stmt.execute(sqlInsert);

		this.conn.close();
	}

    public Triplet getBonrr(String bonrr, String username, String domain) throws SQLException {
        //Get mysql conneciton
        getConnection();
        Statement stmt = this.conn.createStatement();
        Triplet t = new Triplet();

        // Step 1: Execute a SQL SELECT query, the query result
        String strSelect = "select bonrr, MAX(wts) as wts, pw, pwHash, signature, rank from Bonrrs where bonrr = '" + bonrr + "' and domain = '" + domain
                + "' and username = '" + username + "'GROUP BY wts ORDER BY wts DESC;";

        // Step 2: Process the ResultSet by scrolling the cursor forward via
        ResultSet rset = stmt.executeQuery(strSelect);
        int rowCount = 0;
        while (rset.next()) { // Move the cursor to the next row
            t.setDomain(domain);
            t.setUsername(username);
            t.setPassword(rset.getString("pw"));
            t.setHash(rset.getString("pwHash"));
            t.setSignature(rset.getString("signature"));
            t.setWts(rset.getLong("wts"));
            t.setRank(rset.getLong("rank"));
            ++rowCount;
            break;
        }
        this.conn.close();
        if (rowCount == 0) {
            return null;
        } else {
            return t;
        }
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


}