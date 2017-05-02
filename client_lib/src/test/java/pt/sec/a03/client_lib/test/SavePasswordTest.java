package pt.sec.a03.client_lib.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import pt.sec.a03.client_lib.ClientLib;
import pt.sec.a03.common_classes.exception.DataNotFoundException;
import pt.sec.a03.common_classes.exception.IllegalAccessExistException;
import pt.sec.a03.common_classes.exception.InvalidArgumentException;
import pt.sec.a03.crypto.Crypto;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SavePasswordTest extends AbstractClientLibTest {

	private static final long SLEEP_TIME = 1000;

	private static final String KEY_STORE_PASSWORD_1 = "insecure";
	private static final String KEY_STORE_ALIAS_FOR_PUB_PRIV_1 = "client";

	private static final String KEY_STORE_PASSWORD_2 = "insecure";
	private static final String KEY_STORE_ALIAS_FOR_PUB_PRIV_2 = "client";

	private static final String DOMAIN_1 = "YouTube";
	private static final String USERNAME_1 = "lol_gosu";
	private static final String PASSWORD_1 = "bestAdc";
	private static final String PASSWORD_2 = "buffLucian";

	private KeyStore ks1;
	private KeyStore ks2;
	private ClientLib c1;
	private ClientLib c2;
	private ClientLib c3;

	public SavePasswordTest() {
		super();
		Map<String, String> m = new HashMap<String, String>();
		m.put("server1", "localhost:5555");
		m.put("server2", "localhost:6666");
		m.put("server3", "localhost:7777");
		m.put("server4", "localhost:5444");
		c1 = new ClientLib(m, 100);
		c2 = new ClientLib(m, 90);
		c3 = new ClientLib(m, 90);
		try {
			ks1 = Crypto.readKeystoreFile("../Client/ks/Client1.jks", "insecure".toCharArray());
			ks2 = Crypto.readKeystoreFile("../Client/ks/client2.jks", "insecure".toCharArray());

			c1.init(ks1, KEY_STORE_ALIAS_FOR_PUB_PRIV_1, KEY_STORE_PASSWORD_1);
			c1.register_user();
			Thread.sleep(SLEEP_TIME);
			c3.init(ks1, KEY_STORE_ALIAS_FOR_PUB_PRIV_1, KEY_STORE_PASSWORD_1);
			Thread.sleep(SLEEP_TIME);
			c2.init(ks2, KEY_STORE_ALIAS_FOR_PUB_PRIV_2, KEY_STORE_PASSWORD_2);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	protected void populate() {
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
	public void test01_savePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		c1.save_password(DOMAIN_1, USERNAME_1, PASSWORD_1);
		assertEquals(PASSWORD_1, c1.retrieve_password(DOMAIN_1, USERNAME_1));
	}

	/**
	 * Null domain argument
	 */
	@Test
	public void test02_savePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
			c1.save_password(null, USERNAME_1, PASSWORD_1);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {

		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException, instead of: " + e.getMessage());
		}
	}

	/**
	 * Null username argument
	 */
	@Test
	public void test03_savePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
			c1.save_password(DOMAIN_1, null, PASSWORD_1);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {

		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException, instead of: " + e.getMessage());
		}
	}

	/**
	 * Null pass argument
	 */
	@Test
	public void test04_savePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
			c1.save_password(DOMAIN_1, USERNAME_1, null);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {

		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException, instead of: " + e.getMessage());
		}
	}

	/**
	 * Update pass
	 */
	@Test
	public void test05_savePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
			c1.save_password(DOMAIN_1, USERNAME_1, PASSWORD_1);
			Thread.sleep(SLEEP_TIME);
			c1.save_password(DOMAIN_1, USERNAME_1, PASSWORD_2);
			assertEquals(PASSWORD_2, c1.retrieve_password(DOMAIN_1, USERNAME_1));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * PubKey not registered
	 */
	@Test
	public void test06_savePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
			c2.save_password(DOMAIN_1, USERNAME_1, PASSWORD_1);
			fail("This test should fail with exception DataNotFoundException");
		} catch (DataNotFoundException e) {

		} catch (Exception e) {
			fail("This test should fail with exception DataNotFoundException, instead of: " + e.getMessage());
		}
	}

	/**
	 * Username and Domain already exist for other user
	 */
	@Test
	public void test07_savePassword() {
		try {
			c1.save_password(DOMAIN_1, USERNAME_1, PASSWORD_1);
			Thread.sleep(SLEEP_TIME);
			c2.register_user();
			Thread.sleep(SLEEP_TIME);
			c2.save_password(DOMAIN_1, USERNAME_1, PASSWORD_1);
			fail("This test should fail with exception IllegalAccessExistException");
		} catch (IllegalAccessExistException e) {

		} catch (Exception e) {
			fail("This test should fail with exception IllegalAccessExistException, instead of: " + e.getMessage());
		}
	}

	/**
	 * Username and Domain already exist for other user
	 */
	@Test
	public void test08_savePassword() {
		try {
			class TestThread extends Thread {
				String password;
				ClientLib cl;

				TestThread(ClientLib cl, String password) {
					this.password = password;
					this.cl = cl;
				}

				public void run() {
					cl.save_password(DOMAIN_1, USERNAME_1, password);
				}
			}
			TestThread client2 = new TestThread(c3, PASSWORD_2);
			TestThread client1 = new TestThread(c1, PASSWORD_1);
			client2.start();
			client1.start();
			Thread.sleep(8000);
			assertEquals(PASSWORD_1, c1.retrieve_password(DOMAIN_1, USERNAME_1));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
