package pt.sec.a03.client_lib.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;



public abstract class AbstractClientLibTest {

	public static final String MYSQL_ID = "root";
	public static final String MYSQL_PASSWORD = "rootroot";
	public static final String SQL_SCRIPT_PATH = "../PwServer/database.sql";
	
	public AbstractClientLibTest() {

	}

	@BeforeClass
	public static void setUpBeforeAll() throws Exception {
	}

	@Before // run before each test
	public void setUp() throws Exception {
		try {
			populate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After // rollback after each test
	public void tearDown() {
		try {
			after();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void restore() throws Exception {
		PrintStream originalStream = System.out;
		
		PrintStream dummyStream    = new PrintStream(new OutputStream(){
		    public void write(int b) {
		        //NO-OP
		    }
		});
		
		System.setOut(dummyStream);
		
		// database connection
		Connection jdbcConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/experiments?useSSL=false",
				MYSQL_ID, MYSQL_PASSWORD);

		// Initialize object for ScripRunner
		ScriptRunner sr = new ScriptRunner(jdbcConnection);

		// Give the input file to Reader
		Reader reader = new BufferedReader(new FileReader(SQL_SCRIPT_PATH));

		// Exctute script
		sr.runScript(reader);
		System.setOut(originalStream);
	}

	protected abstract void populate(); // each test adds its own data

	protected abstract void after();
	
	public String genInvalidTS(){
		Calendar calendar = Calendar.getInstance();
	    calendar.setTime(new Date());
	    calendar.add(Calendar.HOUR, 3);
	    Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());		    
		return timestamp.toString();
	}


}
