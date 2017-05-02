package pt.sec.a03.client_lib.test;

import static org.junit.Assert.fail;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import pt.sec.a03.client_lib.ClientLib;
import pt.sec.a03.common_classes.exception.InvalidArgumentException;
import pt.sec.a03.crypto.Crypto;

public class InitTest extends AbstractClientLibTest {

	private static final String KEY_STORE_1 = "ks/client1.jks";
	private static final String KEY_STORE_PASSWORD_1 = "insecure";
	private static final String WRONG_KEY_STORE_PASSWORD_1 = "secure";
	private static final String KEY_STORE_ALIAS_FOR_PUB_PRIV_1 = "client";
	private static final String WRONG_KEY_STORE_ALIAS_FOR_PUB_PRIV_1 = "pog";

	private static final String KEY_STORE_2 = "ks/clientMissingCer.jks";
	private static final String KEY_STORE_PASSWORD_2 = "insecure";
	private static final String KEY_STORE_ALIAS_FOR_PUB_PRIV_2 = "client";

	private ClientLib c1;
	private KeyStore ks1;
	private KeyStore ks2;

	public InitTest() {
		super();
	}
	
	@Override
	protected void populate() {
		Map<String, String> m = new HashMap<String, String>();
		m.put("server1", "localhost:5555");
		m.put("server2", "localhost:6666");
		m.put("server3", "localhost:7777");
		m.put("server4", "localhost:5444");
		c1 = new ClientLib(m,100);
		try {
			ks1 = Crypto.readKeystoreFile(KEY_STORE_1, KEY_STORE_PASSWORD_1.toCharArray());
			ks2 = Crypto.readKeystoreFile(KEY_STORE_2, KEY_STORE_PASSWORD_2.toCharArray());

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
	public void test01_init() {
		c1.init(ks1, KEY_STORE_ALIAS_FOR_PUB_PRIV_1, KEY_STORE_PASSWORD_1);
	}

	/**
	 * Null key store argument
	 */
	@Test
	public void test02_init() {
		try {
			c1.init(null, KEY_STORE_ALIAS_FOR_PUB_PRIV_1, KEY_STORE_PASSWORD_1);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {

		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		}
	}
	
	/**
	 * Null alias argument
	 */
	@Test
	public void test03_init() {
		try {
			c1.init(ks1, null, KEY_STORE_PASSWORD_1);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {

		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		}

	}

	/**
	 * Wrong alias argument
	 */
	@Test
	public void test04_init() {
		try {
			c1.init(ks1, WRONG_KEY_STORE_ALIAS_FOR_PUB_PRIV_1, KEY_STORE_PASSWORD_1);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {

		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		}

	}

	/**
	 * Null password argument
	 */
	@Test
	public void test05_init() {
		try {
			c1.init(ks1, KEY_STORE_ALIAS_FOR_PUB_PRIV_1, null);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {

		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		}

	}

	/**
	 * Wrong password argument
	 */
	@Test
	public void test06_init() {
		try {
			c1.init(ks1, KEY_STORE_ALIAS_FOR_PUB_PRIV_1, WRONG_KEY_STORE_PASSWORD_1);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {

		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		}

	}

	/**
	 * No server certificate in key store
	 */
	@Test
	public void test07_init() {
		try {
			c1.init(ks2, KEY_STORE_ALIAS_FOR_PUB_PRIV_2, KEY_STORE_PASSWORD_2);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {

		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		}

	}
}
