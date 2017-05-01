package pt.sec.a03.client_lib.test;

import static org.junit.Assert.fail;

import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import pt.sec.a03.client_lib.ClientLib;
import pt.sec.a03.common_classes.exception.AlreadyExistsException;
import pt.sec.a03.crypto.Crypto;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegisterUserTest extends AbstractClientLibTest {

	private static final String KEY_STORE_1 = "ks/client1.jks";
	private static final String KEY_STORE_PASSWORD_1 = "insecure";
	private static final String KEY_STORE_ALIAS_FOR_PUB_PRIV_1 = "client";

	private ClientLib c1;
	private KeyStore ks1;
	private Map<String, String> m;

	public RegisterUserTest() {
		super();
		m = new HashMap<String, String>();
		m.put("server1", "localhost:5555");
		m.put("server2", "localhost:6666");
		m.put("server3", "localhost:7777");
		m.put("server4", "localhost:5444");
	}

	@Override
	protected void populate() {
		c1 = new ClientLib(m);
		try {
			ks1 = Crypto.readKeystoreFile(KEY_STORE_1, KEY_STORE_PASSWORD_1.toCharArray());
			c1.init(ks1, KEY_STORE_ALIAS_FOR_PUB_PRIV_1, KEY_STORE_PASSWORD_1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void after() {
		try {
			restore();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * Normal arguments
	 */
	@Test
	public void test01_registerUser() {
		try {
			Thread.sleep(5500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		c1.register_user();
	}

	/**
	 * User already registered
	 */
	@Test
	public void test02_registerUser() {
		try {
			Thread.sleep(4500);
			c1.register_user();
			Thread.sleep(1000);
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
			Thread.sleep(3500);
			c1.setResponses(new ArrayList());
			for (String s : m.keySet()) {
				String[] infoToSend = c1.prepareForRegisterUser(s);
				infoToSend[0] ="pohsdadwqdwdwdqwdqwdqws";
				c1.sendRegisterUser(infoToSend, s);
			}
			c1.processResponses();
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
			Thread.sleep(2500);
			c1.setResponses(new ArrayList());
			for (String s : m.keySet()) {
				String[] infoToSend = c1.prepareForRegisterUser(s);
				infoToSend[1] = "pohsdadwqdwdwdqwdqwdqws";
				c1.sendRegisterUser(infoToSend, s);
			}
			c1.processResponses();
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
			Thread.sleep(500);
			c1.setResponses(new ArrayList());
			for (String s : m.keySet()) {
				String[] infoToSend = c1.prepareForRegisterUser(s);
				Certificate cert2 = ks1.getCertificate(s);
				PublicKey serverPub = Crypto.getPublicKeyFromCertificate(cert2);
				String stringNonce = 31321 + "";
				byte[] cipheredNonce = Crypto.cipherString(stringNonce, serverPub);
				infoToSend[2] = Crypto.encode(cipheredNonce);
				c1.sendRegisterUser(infoToSend, s);
			}
			c1.processResponses();
			fail("This test should fail with exception BadRequestException");
		} catch (BadRequestException e) {

		} catch (Exception e) {
			fail("This test should fail with exception BadRequestException");
		}
	}
}