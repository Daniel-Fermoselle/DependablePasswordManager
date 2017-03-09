package pt.sec.a03.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.User;
import pt.sec.a03.server.exception.AlreadyExistsException;
import pt.sec.a03.server.exception.InvalidArgumentException;

public class RegisterUserTest extends AbstractPasswordManagerTest {
	
	PasswordManager pwm;
	private String existingUserPK;
	private String newUserPK;
	
	@Override
	protected void populate() {
		pwm = new PasswordManager();
		existingUserPK = "123";
		newUserPK = "Bob";

	}
	
	@Test
	public void successRegister(){
		pwm.addUser(newUserPK);
		User u = pwm.getUserByPK(newUserPK);
		assertEquals("User registered incorrectly: ", newUserPK, u.getPublicKey());
	}
	
	@Test
	public void nullKeyRegister(){
		try{
			pwm.addUser(null);
			fail("This test should fail with an InvalidArgumentException, null pointers were passed");
		}
		catch(InvalidArgumentException e){			
		}
	}
	
	@Test
	public void existingUserRegister(){
		try{
			pwm.addUser(existingUserPK);
			fail("This test should fail with an AlreadyExistsException, public key already associated with an user");
		}
		catch(AlreadyExistsException e){			
		}
	}

}
