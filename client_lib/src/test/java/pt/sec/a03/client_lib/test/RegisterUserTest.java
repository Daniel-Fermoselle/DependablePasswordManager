package pt.sec.a03.client_lib.test;

import static org.junit.Assert.fail;

import java.security.KeyStore;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import pt.sec.a03.client_lib.ClientLib;
import pt.sec.a03.client_lib.exception.AlreadyExistsException;
import pt.sec.a03.crypto.Crypto;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegisterUserTest extends AbstractClientLibTest {

	private static final String KEY_STORE_1 = "ks/Client1.jks";
	private static final String KEY_STORE_PASSWORD_1 = "insecure";
	private static final String KEY_STORE_ALIAS_FOR_PUB_PRIV_1 = "client";

	private ClientLib c1;
	private KeyStore ks1;

	public RegisterUserTest() {
		super();
	}

	@Override
	protected void populate() {
		c1 = new ClientLib();
		try {
			ks1 = Crypto.readKeystoreFile(KEY_STORE_1, KEY_STORE_PASSWORD_1.toCharArray());
			c1.init(ks1, KEY_STORE_ALIAS_FOR_PUB_PRIV_1, KEY_STORE_PASSWORD_1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void after() {
	}

	/**
	 * Normal arguments
	 */
	@Test
	public void test01_registerUser() {
		c1.register_user();
	}

	/**
	 * User already registered
	 */
	@Test
	public void test02_registerUser() {
		try {
			c1.register_user();
			fail("This test should fail with exception AlreadyExistsException");
		} catch (AlreadyExistsException e) {

		} catch (Exception e) {
			fail("This test should fail with exception AlreadyExistsException");
		}
	}
	
	/**
	 * Invalid Signature
	 */
	@Test
	public void test03_registerUser() {
		try {
			String[] infoToSend = c1.prepareForRegisterUser();
			infoToSend[0] ="pohsdadwqdwdwdqwdqwdqws";
			Response postReponse = c1.sendRegisterUser(infoToSend);
			c1.processRegisterUser(postReponse);
			fail("This test should fail with exception BadRequestException");
		} catch (BadRequestException e) {

		} catch (Exception e) {
			fail("This test should fail with exception BadRequestException");
		}
	}
	
	/**
	 * Invalid Public Key
	 */
	@Test
	public void test04_registerUser() {
		try {
			String[] infoToSend = c1.prepareForRegisterUser();
			infoToSend[1] ="pohsdadwqdwdwdqwdqwdqws";
			Response postReponse = c1.sendRegisterUser(infoToSend);
			c1.processRegisterUser(postReponse);
			fail("This test should fail with exception BadRequestException");
		} catch (BadRequestException e) {

		} catch (Exception e) {
			fail("This test should fail with exception BadRequestException");
		}
	}
	
	/**
	 * Invalid TimeStamp
	 */
	@Test
	public void test05_registerUser() {
		try {
			String[] infoToSend = c1.prepareForRegisterUser();
			infoToSend[2] = genInvalidTS();
			Response postReponse = c1.sendRegisterUser(infoToSend);
			c1.processRegisterUser(postReponse);
			fail("This test should fail with exception BadRequestException");
		} catch (BadRequestException e) {

		} catch (Exception e) {
			fail("This test should fail with exception BadRequestException");
		}
	}
}
