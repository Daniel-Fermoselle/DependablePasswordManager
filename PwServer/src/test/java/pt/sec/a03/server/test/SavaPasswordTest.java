package pt.sec.a03.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.Triplet;
import pt.sec.a03.server.exception.DataNotFoundException;
import pt.sec.a03.server.exception.InvalidArgumentException;

public class SavaPasswordTest extends AbstractPasswordManagerTest {
	private PasswordManager pwm;
	private Triplet t1;
	private Triplet t12;
	private Triplet t2;
	private Triplet nullTriplet;
	private static String pubKey = "123";
	private static String pubKeyNoob = "1234";
	private static String nullPubKey = null;
	
	@Override
	protected void populate() {
		pwm = new PasswordManager();
		t1 = new Triplet("poguito", "username", "youtube");
		t12 = new Triplet("poguito2", "username", "youtube");
		t2 = new Triplet("noobito", "noobUser", "noobDomain");
		nullTriplet = null;
	}
	
	
	
	//tests: 
	  @Test
	  public void successSavePassword() throws Exception{
		Triplet t3 = pwm.saveTriplet(t1,pubKey);
	    assertEquals("Username saved incorrectly.", t1.getUsername(), t3.getUsername());
	    assertEquals("Domain saved incorrectly.", t1.getDomain(), t3.getDomain());
	    assertEquals("Password saved incorrectly.", t1.getPassword(), t3.getPassword());
	  }
	  
	  @Test
	  public void successUpdatePassword() throws Exception{
		Triplet t3 = pwm.saveTriplet(t1,pubKey);
		Triplet t4 = pwm.saveTriplet(t12,pubKey);
	    assertEquals("Username saved incorrectly.", t12.getUsername(), t3.getUsername());
	    assertEquals("Domain saved incorrectly.", t12.getDomain(), t3.getDomain());
	    assertEquals("Password saved incorrectly.", t12.getPassword(), t4.getPassword());
	  }
	  
	  @Test
	  public void wrongPublicKeyUpdatePassword(){
		try{
			Triplet t3 = pwm.saveTriplet(t1,pubKey);
			Triplet t4 = pwm.saveTriplet(t1,pubKeyNoob);
			fail("This test should fail with exception DataNotFoundException");
	  	} catch (DataNotFoundException e) {
			System.out.println("This is fine this test should fail with this exception: DataNotFoundException");
		} catch (Exception e) {
			e.printStackTrace();
		};
	  }
	  
	  @Test
	  public void wrongPublicKeySavePassword(){
		try {
			Triplet t3 = pwm.saveTriplet(t1,pubKeyNoob);
			fail("This test should fail with exception DataNotFoundException");
		} catch (DataNotFoundException e) {
			System.out.println("This is fine this test should fail with this exception: DataNotFoundException");
		} catch (Exception e) {
			e.printStackTrace();
		}
	  }
	  
	  @Test
	  public void wrongUsernameSavePassword(){
		try{
		Triplet t3 = pwm.saveTriplet(t2,pubKey);
		fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {
			System.out.println("This is fine this test should fail with this exception: InvalidArgumentException");
		} catch (Exception e) {
			e.printStackTrace();
		}
	  }
	  
	  @Test
	  public void nullArgumentsSavePassword(){
		try{
		Triplet t3 = pwm.saveTriplet(nullTriplet,nullPubKey);
		fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {
			System.out.println("This is fine this test should fail with this exception: InvalidArgumentException");
		} catch (Exception e) {
			e.printStackTrace();
		}
	  }
	  
	  @Test
	  public void nullArgumentsUpdatePassword(){
		try{
			Triplet t3 = pwm.saveTriplet(t1,pubKey);
			Triplet t4 = pwm.saveTriplet(t1,nullPubKey);
			fail("This test should fail with exception DataNotFoundException");
	  	} catch (DataNotFoundException e) {
			System.out.println("This is fine this test should fail with this exception: DataNotFoundException");
		} catch (Exception e) {
			e.printStackTrace();
		};
	  }

}
