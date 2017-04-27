package pt.sec.a03.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.junit.Test;

import pt.sec.a03.crypto.Crypto;
import pt.sec.a03.server.database.Database;
import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.Triplet;
import pt.sec.a03.server.domain.User;
import pt.sec.a03.server.exception.DataNotFoundException;
import pt.sec.a03.server.exception.ForbiddenAccessException;
import pt.sec.a03.server.exception.InvalidArgumentException;

public class SavePasswordTest extends AbstractPasswordManagerTest {
	private PasswordManager pwm;
	private Triplet t1;
	private Triplet t12;
	private Triplet t2;
	private Triplet nullTriplet;
	//private static String pubKey = "123";
	private static String pubKeyNoob = "1234";
	private static String ForbidPublic = "12345";
	private static String nullPubKey = null;
	private Database db;
	
	@Override
	protected void populate() {
		db = new Database();
		pwm = new PasswordManager();
		try {
			t1 = new Triplet("youtube", "username", "poguito",
					Crypto.encode(Crypto.hashString("poguito")));
			t12 = new Triplet("youtube", "username", "poguito2",
					Crypto.encode(Crypto.hashString("poguito2")));
			t2 = new Triplet( "noobDomain", "noobUser","noobito",
					Crypto.encode(Crypto.hashString("noobito")));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		nullTriplet = null;
		
		pwm = new PasswordManager();
		db = new Database();
		try {
			db.saveUser(ForbidPublic);
			User user = db.getUserByPK(ForbidPublic);
			db.saveUser(pubKeyNoob);
			User user2 = db.getUserByPK(pubKeyNoob);
			db.saveTriplet(t1, user.getUserID());
			db.saveTriplet(t2, user2.getUserID());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void after() {
		try {
			db.runScript();
		} catch (FileNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	
	
	
	//tests: 
	  @Test
	  public void successSavePassword() throws Exception{
		try{
		Triplet t3 = pwm.saveTriplet(t2,pubKeyNoob);
	    assertEquals("Username saved incorrectly.", t2.getUsername(), t3.getUsername());
	    assertEquals("Domain saved incorrectly.", t2.getDomain(), t3.getDomain());
	    assertEquals("Password saved incorrectly.", t2.getPassword(), t3.getPassword());
		} catch (Exception e) {
			fail("This test should not fail");
		};
	  }
	  
	  @Test
	  public void successUpdatePassword() throws Exception{
		try{
		Triplet t4 = pwm.saveTriplet(t12,ForbidPublic);
	    assertEquals("Username saved incorrectly.", t12.getUsername(), t1.getUsername());
	    assertEquals("Domain saved incorrectly.", t12.getDomain(), t1.getDomain());
	    assertEquals("Password saved incorrectly.", t12.getPassword(), t4.getPassword());
		} catch (Exception e) {
			fail("This test should not fail");
		};
	  }
	  
	  @Test
	  public void wrongPublicKeyUpdatePassword(){
		try{
			Triplet t4 = pwm.saveTriplet(t1,pubKeyNoob);
			fail("This test should fail with exception ForbiddenAccessException");
	  	} catch (ForbiddenAccessException e) {
			System.out.println("This is fine this test should fail with this exception: ForbiddenAccessException");
		} catch (Exception e) {
			fail("This test should fail with exception ForbiddenAccessException");
		};
	  }
	  
	  @Test
	  public void repetitivePassword(){
		try{
			Triplet t4 = pwm.saveTriplet(t1,ForbidPublic);
			assertEquals("Username saved incorrectly.", t1.getUsername(), t4.getUsername());
		    assertEquals("Domain saved incorrectly.", t1.getDomain(), t4.getDomain());
		    assertEquals("Password saved incorrectly.", t1.getPassword(), t4.getPassword());	  	
		} catch (Exception e) {
			fail("This test should not fail");
		};
	  }
	  
	  @Test
	  public void nullArgumentsSavePassword(){
		try{
		Triplet t3 = pwm.saveTriplet(nullTriplet,nullPubKey);
		fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {
			System.out.println("This is fine this test should fail with this exception: InvalidArgumentException");
		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		}
	  }
	  
	  @Test
	  public void nullArgumentsUpdatePassword(){
		try{
			Triplet t4 = pwm.saveTriplet(t1,nullPubKey);
			fail("This test should fail with exception InvalidArgumentException");
	  	} catch (InvalidArgumentException e) {
			System.out.println("This is fine this test should fail with this exception: InvalidArgumentException");
		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		};
	  }

}
