package pt.sec.a03.client_lib.test;

import java.security.KeyStore;

import org.junit.Test;

import pt.sec.a03.client_lib.ClientLib;
import pt.sec.a03.crypto.Crypto;

public class SavePasswordTest extends AbstractClientLibTest {

	private static final String KEY_STORE_1 = "ks/Client1.jks";
	private static final String KEY_STORE_PASSWORD_1 = "insecure";
	private static final String KEY_STORE_ALIAS_FOR_PUB_PRIV_1 = "client";

	private ClientLib c1;
	private KeyStore ks1;

	public SavePasswordTest() {
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
	public void test01_init() {
		c1.register_user();
	}


}
