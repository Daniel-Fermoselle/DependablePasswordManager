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
	private Triplet t2;
	private static String pubKey = "123";
	private static String pubKeyNoob = "1234";
	
	@Override
	protected void populate() {
		pwm = new PasswordManager();
		t1 = new Triplet("poguito", "username", "youtube");
		t2 = new Triplet("noobito", "noobUser", "noobDomain");
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

}
