package pt.sec.a03.client_lib.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import pt.sec.a03.client_lib.ClientLib;
import pt.sec.a03.client_lib.exception.DataNotFoundException;
import pt.sec.a03.client_lib.exception.IllegalAccessExistException;
import pt.sec.a03.client_lib.exception.InvalidArgumentException;
import pt.sec.a03.client_lib.exception.InvalidSignatureException;
import pt.sec.a03.client_lib.exception.InvalidTimestampException;
import pt.sec.a03.crypto.Crypto;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RetrivePasswordTest extends AbstractClientLibTest {

	private static final String KEY_STORE_1 = "ks/Client1.jks";
	private static final String KEY_STORE_PASSWORD_1 = "insecure";
	private static final String KEY_STORE_ALIAS_FOR_PUB_PRIV_1 = "client";

	private static final String KEY_STORE_2 = "ks/Client3.jks";
	private static final String KEY_STORE_PASSWORD_2 = "insecure";
	private static final String KEY_STORE_ALIAS_FOR_PUB_PRIV_2 = "client";

	private static final String DOMAIN_1 = "YouTube";
	private static final String USERNAME_1 = "lol_gosu";
	private static final String DOMAIN_2 = "YouTubee";
	private static final String USERNAME_2 = "lol_gosuu";
	private static final String PASSWORD_1 = "bestAdc";
	private static final String PASSWORD_2 = "buffLucian";

	private static final String FAKE_SIGNATURE = "fewvrwrwrgkwrgkwewkge";
	
	private static final String SIGNATURE_HEADER_NAME = "signature";
	private static final String NONCE_HEADER_NAME = "nonce-value";

	private static final String FAKE_HASH = "WFEFERGGREGegerge";
	
	private KeyStore ks1;
	private KeyStore ks2;
	private ClientLib c1;
	private ClientLib c2;
	private String alias;


	public RetrivePasswordTest() {
		super();
		c1 = new ClientLib();
		c2 = new ClientLib();
		alias = "server";
		try {
			ks1 = Crypto.readKeystoreFile(KEY_STORE_1, KEY_STORE_PASSWORD_1.toCharArray());
			ks2 = Crypto.readKeystoreFile(KEY_STORE_2, KEY_STORE_PASSWORD_2.toCharArray());
			
			c1.init(ks1, KEY_STORE_ALIAS_FOR_PUB_PRIV_1, KEY_STORE_PASSWORD_1);
			c1.register_user();

			c2.init(ks2, KEY_STORE_ALIAS_FOR_PUB_PRIV_2, KEY_STORE_PASSWORD_2);
			
			c1.save_password(DOMAIN_1, USERNAME_1, PASSWORD_1);

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
	public void test01_retrivePassword() {
		assertEquals(PASSWORD_1, c1.retrive_password(DOMAIN_1, USERNAME_1));
	}

	/**
	 * Null domain argument
	 */
	@Test
	public void test02_retrivePassword() {
		try {
			c1.retrive_password(null, USERNAME_1);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {

		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		}
	}

	/**
	 * Null username argument
	 */
	@Test
	public void test03_retrivePassword() {
		try {
			c1.retrive_password(DOMAIN_1, null);
			fail("This test should fail with exception InvalidArgumentException");
		} catch (InvalidArgumentException e) {

		} catch (Exception e) {
			fail("This test should fail with exception InvalidArgumentException");
		}
	}

	/**
	 * Update pass
	 */
	@Test
	public void test04_retrivePassword() {
		c1.save_password(DOMAIN_1, USERNAME_1, PASSWORD_2);
		assertEquals(PASSWORD_2, c1.retrive_password(DOMAIN_1, USERNAME_1));
	}

	/**
	 * PubKey not registered
	 */
	@Test
	public void test05_retrivePassword() {
		try {
			c2.retrive_password(DOMAIN_1, USERNAME_1);
			fail("This test should fail with exception DataNotFoundException");
		} catch (DataNotFoundException e) {

		} catch (Exception e) {
			fail("This test should fail with exception DataNotFoundException");
		}
	}

	/**
	 * Username and Domain exist for other user
	 */
	@Test
	public void test06_retrivePassword() {
		try {
			c2.register_user();
			c2.retrive_password(DOMAIN_1, USERNAME_1);
			fail("This test should fail with exception IllegalAccessExistException");
		} catch (IllegalAccessExistException e) {

		} catch (Exception e) {
			fail("This test should fail with exception IllegalAccessExistException");
		}
	}
	
	/**
	 * Username and Domain exist for other user
	 */
	@Test
	public void test07_retrivePassword() {
		try {
			c1.retrive_password(DOMAIN_1, USERNAME_2);
			fail("This test should fail with exception DataNotFoundException");
		} catch (DataNotFoundException e) {

		} catch (Exception e) {
			fail("This test should fail with exception DataNotFoundException");
		}
	}
	
	/**
	 * Username and Domain exist for other user
	 */
	@Test
	public void test08_retrivePassword() {
		try {
			c1.retrive_password(DOMAIN_2, USERNAME_1);
			fail("This test should fail with exception DataNotFoundException");
		} catch (DataNotFoundException e) {

		} catch (Exception e) {
			fail("This test should fail with exception DataNotFoundException");
		}
	}
	
	
	/**
	 * Invalid Signature
	 */
	@Test
	public void test09_retrivePassword() {
		try {
			String[] infoToSend = c1.prepareForRetrivePassword(DOMAIN_1, USERNAME_1, alias);
			infoToSend[1] = FAKE_SIGNATURE;
			Response response = c1.sendRetrivePassword(infoToSend, alias);
			c1.processRetrivePassword(response, alias);
			fail("This test should fail with exception BadRequestException");
		} catch (BadRequestException e) {

		} catch (Exception e) {
			fail("This test should fail with exception BadRequestException");
		}
	}
	
	/**
	 * Invalid Signature
	 */
	@Test
	public void test10_retrivePassword() {
		try {
			String[] infoToSend = c1.prepareForRetrivePassword(DOMAIN_1, USERNAME_1, alias);

			Certificate cert2 = ks1.getCertificate(alias);
			PublicKey serverPub = Crypto.getPublicKeyFromCertificate(cert2);
			String stringNonce = 31321 + "";
			byte[] cipheredNonce = Crypto.cipherString(stringNonce, serverPub);
			infoToSend[2] = Crypto.encode(cipheredNonce);

			Response response = c1.sendRetrivePassword(infoToSend, alias);
			c1.processRetrivePassword(response, alias);
			fail("This test should fail with exception BadRequestException");
		} catch (BadRequestException e) {

		} catch (Exception e) {
			fail("This test should fail with exception BadRequestException");
		}
	}
	
	/**
	 * Invalid Signature received
	 */
	@Test
	public void test11_retrivePassword() {
		try {
			String[] infoToSend = c1.prepareForRetrivePassword(DOMAIN_1, USERNAME_1, alias);
			Response response = c1.sendRetrivePassword(infoToSend, alias);
			response.getHeaders().putSingle(SIGNATURE_HEADER_NAME, FAKE_SIGNATURE);
			c1.processRetrivePassword(response, alias);
			fail("This test should fail with exception InvalidSignatureException");
		} catch (InvalidSignatureException e) {

		} catch (Exception e) {
			fail("This test should fail with exception InvalidSignatureException");
		}
	}
	
	/**
	 * Invalid TS received
	 */
	@Test
	public void test12_retrivePassword() {
		try {
			String[] infoToSend = c1.prepareForRetrivePassword(DOMAIN_1, USERNAME_1, alias);
			Response response = c1.sendRetrivePassword(infoToSend, alias);

			Certificate cert2 = ks1.getCertificate(KEY_STORE_ALIAS_FOR_PUB_PRIV_1);
			PublicKey clientPub = Crypto.getPublicKeyFromCertificate(cert2);
			
			String stringNonce = 0 + "";
			byte[] cipheredNonce = Crypto.cipherString(stringNonce, clientPub);
			String invalidNonce = Crypto.encode(cipheredNonce);

			response.getHeaders().putSingle(NONCE_HEADER_NAME, invalidNonce);
			c1.processRetrivePassword(response, alias);
			fail("This test should fail with exception InvalidTimestampException");
		} catch (InvalidTimestampException e) {

		} catch (Exception e) {
			fail("This test should fail with exception InvalidTimestampException: " + e.getClass().getName());
		}
	}
	
	/**
	 * Invalid TS received
	 */
	@Test
	public void test13_retrivePassword() {
		try {
			String[] infoToSend = c1.prepareForRetrivePassword(DOMAIN_1, USERNAME_1, alias);
			infoToSend[3] = FAKE_HASH;
			Response response = c1.sendRetrivePassword(infoToSend, alias);
			c1.processRetrivePassword(response, alias);
			fail("This test should fail with exception BadRequestException");
		} catch (BadRequestException e) {

		} catch (Exception e) {
			fail("This test should fail with exception BadRequestException");
		}
	}
}
