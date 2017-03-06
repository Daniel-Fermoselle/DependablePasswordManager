package pt.sec.a03.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import pt.sec.a03.server.domain.User;
import pt.sec.a03.server.domain.Triplet;

public class Database {

	public  static final String MYSQL_ID = "root";
	public  static final String MYSQL_PASSWORD = "rootroot";
	
    public Database() {
    }        
    
    //TODO PublicKey instead of String
    public User getUser(String publicKey) throws SQLException{
        String publicKeyDB = "";
        long userID = 0;
		// Step 1: Allocate a database "Connection" object
        Connection conn = DriverManager.getConnection(
              "jdbc:mysql://localhost:3306/experiments?useSSL=false", MYSQL_ID, MYSQL_PASSWORD); // MySQL

        // Step 2: Allocate a "Statement" object in the Connection
        Statement stmt = conn.createStatement();
        
        // Step 3: Execute a SQL SELECT query, the query result
        String strSelect = "select userID, publicKey from Users where publicKey = '" + publicKey + "'";

        // Step 4: Process the ResultSet by scrolling the cursor forward via next().
        ResultSet rset = stmt.executeQuery(strSelect);
        int rowCount = 0;
        while(rset.next()) {   // Move the cursor to the next row
            userID = rset.getLong("userID");
            publicKeyDB = rset.getString("publicKey");
            ++rowCount;
        }
        if(rowCount == 0){
        	return null;
        }
        else{
        	return new User(userID, publicKeyDB);
        }
    }
    
    //TODO PublicKey instead of String
    public void saveUser(String publicKey) throws SQLException{
		// Step 1: Allocate a database "Connection" object
		Connection conn = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/experiments?useSSL=false", MYSQL_ID, MYSQL_PASSWORD); // MySQL

		// Step 2: Allocate a "Statement" object in the Connection
		Statement stmt = conn.createStatement();

		// Step 3: Execute a SQL DELETE query, the query result
		String sqlInsert = "insert into Users(publicKey) values ('" + publicKey + "');";
		stmt.execute(sqlInsert);
    }
    
    public Triplet getTriplet(String domain, String username) throws SQLException{
        String password = "", usernameDB = "", domainDB = "";
        long tripletID = 0, userID = 0;
		// Step 1: Allocate a database "Connection" object
        Connection conn = DriverManager.getConnection(
              "jdbc:mysql://localhost:3306/experiments?useSSL=false", MYSQL_ID, MYSQL_PASSWORD); // MySQL

        // Step 2: Allocate a "Statement" object in the Connection
        Statement stmt = conn.createStatement();
        
        // Step 3: Execute a SQL SELECT query, the query result
        String strSelect = "select tripletID, userID, password, username, domain from Vault where domain = '" + domain + "' and username = '" + username + "';";

        // Step 4: Process the ResultSet by scrolling the cursor forward via next().
        ResultSet rset = stmt.executeQuery(strSelect);
        int rowCount = 0;
        while(rset.next()) {   // Move the cursor to the next row
        	tripletID = rset.getLong("tripletID");
            userID = rset.getLong("userID");
            password = rset.getString("password");
            usernameDB = rset.getString("username");
            domainDB = rset.getString("domain");
            ++rowCount;
        }
        if(rowCount == 0){
        	return null;
        }
        else{
        	return new Triplet(tripletID, userID, password, usernameDB, domainDB);
        }
    }
    
    //TODO PublicKey instead of String
    public void saveTriplet(Triplet t, String publicKey) throws SQLException{
    	long userID = 0;
		// Step 1: Allocate a database "Connection" object
		Connection conn = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/experiments?useSSL=false", MYSQL_ID, MYSQL_PASSWORD); // MySQL

        // Step 2: Allocate a "Statement" object in the Connection
        Statement select = conn.createStatement();
        
        // Step 3: Execute a SQL SELECT query, the query result
        String strSelect = "select Vault.userID from Vault, Users where Vault.userID=Users.userID and publicKey='" 
        		+ publicKey + "';";

        // Step 4: Process the ResultSet by scrolling the cursor forward via next().
        ResultSet rset = select.executeQuery(strSelect);
        while(rset.next()) {   // Move the cursor to the next row
            userID = rset.getLong("userID");
        }
		
		// Step 5: Allocate a "Statement" object in the Connection
		Statement insert = conn.createStatement();

		// Step 6: Execute a SQL INSERT query, the query result
		String sqlInsert = "insert into Vault(userID, pw, username, domain) values (" 
				+ userID + ", '" + t.getPassword() + "', '" + t.getUsername() + "', '" + t.getDomain() +"');";
		insert.execute(sqlInsert);
    }
	
}
