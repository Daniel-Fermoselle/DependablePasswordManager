package pt.sec.a03.server.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import java.io.FileNotFoundException;
import java.sql.SQLException;


import org.junit.Test;

import pt.sec.a03.server.database.Database;
import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.Triplet;
import pt.sec.a03.server.exception.DataNotFoundException;
import pt.sec.a03.server.exception.InvalidArgumentException;

public class GetPasswordTest extends AbstractPasswordManagerTest {

	private PasswordManager pwm;
	private Database db;
	private Triplet t1;
	
	public GetPasswordTest() {
		super();
	}

	@Override
	protected void populate() {
		pwm = new PasswordManager();
		db = new Database();
		
		t1 = new Triplet("sou_rico", "belly", "sonae");
		try {
			db.saveUser("123456789");
			db.saveTriplet(t1, "123456789");
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

	@Test
	public void test01_getPassword() throws Exception {
		Triplet t = pwm.getTriplet("belly", "sonae");
		
		assertTrue(equalTriplets(t, t1));
	}
	
	@Test
	public void test02_getPassword() throws Exception {
		try {
			pwm.getTriplet(null, "tecnico");
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e){
			
		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		}
	}
	
	@Test
	public void test03_getPassword() throws Exception {
		try {
			pwm.getTriplet("belly", null);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e){
			
		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		}
	}
	
	@Test
	public void test04_getPassword() throws Exception {
		try {
			pwm.getTriplet("belly", "pingo");
			fail("This test should fail with exception DataNotFoundException");
		} catch (DataNotFoundException e){
			
		} catch (Exception e) {
			fail("This test should fail with exception DataNotFoundException");
		}
	}
	
	@Test
	public void test05_getPassword() throws Exception {
		try {
			pwm.getTriplet("rambo", "sonae");
			fail("This test should fail with exception DataNotFoundException");
		} catch (DataNotFoundException e){
			
		} catch (Exception e) {
			fail("This test should fail with exception DataNotFoundException");
		}
	}
	
	public boolean equalTriplets(Triplet t1, Triplet t2) {
		if(t1.getDomain().equals(t2.getDomain())     &&
		   t1.getUsername().equals(t2.getUsername()) &&
		   t1.getPassword().equals(t2.getPassword()))
			return true;
		return false;
	}


}
