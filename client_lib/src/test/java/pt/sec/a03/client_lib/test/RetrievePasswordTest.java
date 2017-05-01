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
import pt.sec.a03.common_classes.exceptions.DataNotFoundException;
import pt.sec.a03.common_classes.exceptions.IllegalAccessExistException;
import pt.sec.a03.common_classes.exceptions.InvalidArgumentException;
import pt.sec.a03.crypto.Crypto;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RetrievePasswordTest extends AbstractClientLibTest {

	private static final String KEY_STORE_1 = "ks/client1.jks";
	private static final String KEY_STORE_PASSWORD_1 = "insecure";
	private static final String KEY_STORE_ALIAS_FOR_PUB_PRIV_1 = "client";

	private static final String KEY_STORE_2 = "ks/client2.jks";
	private static final String KEY_STORE_PASSWORD_2 = "insecure";
	private static final String KEY_STORE_ALIAS_FOR_PUB_PRIV_2 = "client";

	private static final String DOMAIN_1 = "YouTube";
	private static final String USERNAME_1 = "lol_gosu";
	private static final String DOMAIN_2 = "YouTubee";
	private static final String USERNAME_2 = "lol_gosuu";
	private static final String PASSWORD_1 = "bestAdc";
	private static final String PASSWORD_2 = "buffLucian";
	
	private static final long SLEEP_TIME = 1000;

	private KeyStore ks1;
	private KeyStore ks2;
	private ClientLib c1;
	private ClientLib c2;
	private Map<String, String> m;

	public RetrievePasswordTest() {
		super();
		m = new HashMap<String, String>();
		m.put("server1", "localhost:5555");
		m.put("server2", "localhost:6666");
		m.put("server3", "localhost:7777");
		m.put("server4", "localhost:5444");
		c1 = new ClientLib(m);
		c2 = new ClientLib(m);

		try {
			ks1 = Crypto.readKeystoreFile(KEY_STORE_1, KEY_STORE_PASSWORD_1.toCharArray());
			ks2 = Crypto.readKeystoreFile(KEY_STORE_2, KEY_STORE_PASSWORD_2.toCharArray());

			c1.init(ks1, KEY_STORE_ALIAS_FOR_PUB_PRIV_1, KEY_STORE_PASSWORD_1);
			Thread.sleep(SLEEP_TIME);
			c1.register_user();
			Thread.sleep(SLEEP_TIME);
			c2.init(ks2, KEY_STORE_ALIAS_FOR_PUB_PRIV_2, KEY_STORE_PASSWORD_2);
			Thread.sleep(SLEEP_TIME);
			c1.save_password(DOMAIN_1, USERNAME_1, PASSWORD_1);
			Thread.sleep(SLEEP_TIME);
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
		}
	}

	/**
	 * Normal arguments
	 */
	@Test
	public void test01_retrievePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
			assertEquals(PASSWORD_1, c1.retrieve_password(DOMAIN_1, USERNAME_1));
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Null domain argument
	 */
	@Test
	public void test02_retrievePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
			c1.retrieve_password(null, USERNAME_1);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		}
	}

	/**
	 * Null username argument
	 */
	@Test
	public void test03_retrievePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
			c1.retrieve_password(DOMAIN_1, null);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		}
	}

	/**
	 * Update pass
	 */
	@Test
	public void test04_retrievePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
			c1.save_password(DOMAIN_1, USERNAME_1, PASSWORD_2);
			Thread.sleep(SLEEP_TIME);
			assertEquals(PASSWORD_2, c1.retrieve_password(DOMAIN_1, USERNAME_1));
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * PubKey not registered
	 */
	@Test
	public void test05_retrievePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
			c2.retrieve_password(DOMAIN_1, USERNAME_1);
			fail("This test should fail with exception DataNotFoundException");
		} catch (DataNotFoundException e) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			System.out.println(e.getClass().toString());
			fail("This test should fail with exception DataNotFoundException");
		}
	}

	/**
	 * Username and Domain exist for other user
	 */
	@Test
	public void test06_retrievePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
			c2.register_user();
			Thread.sleep(SLEEP_TIME);
			c2.retrieve_password(DOMAIN_1, USERNAME_1);
			fail("This test should fail with exception DataNotFoundException");
		} catch (DataNotFoundException e) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			fail("This test should fail with exception DataNotFoundException");
		}
	}

	/**
	 * Username and Domain exist for other user
	 */
	@Test
	public void test07_retrievePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
			c1.retrieve_password(DOMAIN_1, USERNAME_2);
			fail("This test should fail with exception DataNotFoundException");
		} catch (DataNotFoundException e) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			fail("This test should fail with exception DataNotFoundException");
		}
	}

	/**
	 * Username and Domain exist for other user
	 */
	@Test
	public void test08_retrievePassword() {
		try {
			Thread.sleep(SLEEP_TIME);
			c1.retrieve_password(DOMAIN_2, USERNAME_1);
			fail("This test should fail with exception DataNotFoundException");
		} catch (DataNotFoundException e) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			fail("This test should fail with exception DataNotFoundException");
		}
	}

}
