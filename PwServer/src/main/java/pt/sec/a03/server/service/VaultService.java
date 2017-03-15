package pt.sec.a03.server.service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import java.sql.Timestamp;
import java.text.ParseException;

import pt.sec.a03.crypto.Crypto;
import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.Triplet;
import pt.sec.a03.server.exception.InvalidSignatureException;
import pt.sec.a03.server.exception.InvalidTimestampException;

public class VaultService {

	private static final String ALIAS_FOR_SERVER = "server";
	private static final String SERVER_KEY_STORE_PATH = "/Users/sigma/Desktop/Server1.jks";
	private static final String SERVER_KEY_STORE_PASS = "insecure";
	PrivateKey privKey;
	KeyStore ksServ;
	PasswordManager pwm;

	public VaultService() {
		try {
			this.ksServ = Crypto.readKeystoreFile(SERVER_KEY_STORE_PATH, SERVER_KEY_STORE_PASS.toCharArray());
			this.privKey = Crypto.getPrivateKeyFromKeystore(ksServ, ALIAS_FOR_SERVER, SERVER_KEY_STORE_PASS);
			this.pwm = new PasswordManager();
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
				| UnrecoverableKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} 
	}

	public void put(String publicKey, String signature, String timestamp, String hashPw, String cipherPassword,
			String cipheredHashUsername, String cipheredHashDomain) {

		String[] userAndDom = null;
		try {
			// Verify freshness
			verifyTS(timestamp);

			// Decipher
			userAndDom = decipherUsernameAndDomain(cipheredHashDomain, cipheredHashUsername);

			// Verify signature
			String serverSideTosign = userAndDom[0] + userAndDom[1] + timestamp + hashPw + cipherPassword;

			byte[] serverSideSig = Crypto.decode(signature);

			PublicKey pk = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));

			if (!Crypto.verifyDigitalSignature(serverSideSig, serverSideTosign.getBytes(), pk)) {
				throw new InvalidSignatureException("Invalid Signature");
			}

			Triplet t = pwm.saveTriplet(new Triplet(cipherPassword, userAndDom[0], userAndDom[1]), publicKey);
			pwm.saveHash(t.getTripletID(), hashPw);

		} catch (NoSuchAlgorithmException |  ParseException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} catch (SignatureException e ) {
			throw new InvalidSignatureException(e.getMessage());
		} catch (InvalidKeySpecException | InvalidKeyException e) {
			throw new pt.sec.a03.server.exception.InvalidKeyException(e.getMessage());
		}
	}

	public String[] get(String publicKey, String username, String domain, String stringTS, String stringSig) {
		String[] userAndDom = null;

		try {
			// Verify TimeStamp
			verifyTS(stringTS);

			// Decipher domain and username
			userAndDom = decipherUsernameAndDomain(domain, username);

			// Verify Signature
			String verifySig = userAndDom[0] + userAndDom[1] + stringTS;

			PublicKey cliPublicKey = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));

			if (!Crypto.verifyDigitalSignature(Crypto.decode(stringSig), verifySig.getBytes(), cliPublicKey)) {
				throw new InvalidSignatureException("Invalid Signature");
			}

			Triplet t = pwm.getTriplet(userAndDom[0], userAndDom[1], publicKey);

			String pwHashFromDB = pwm.getHash(userAndDom[0], userAndDom[1], publicKey);

			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			stringTS = timestamp.toString();

			String toSign = stringTS + pwHashFromDB + t.getPassword();

			String sign = Crypto.encode(Crypto.makeDigitalSignature(toSign.getBytes(), this.privKey));

			return new String[] { stringTS, sign, pwHashFromDB, t.getPassword() };

		} catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException
				| ParseException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public void verifyTS(String stringTS) throws ParseException {
		if (!Crypto.validTS(stringTS)) {
			throw new InvalidTimestampException("Invalid Timestamp");
		}
	}

	public String[] decipherUsernameAndDomain(String domain, String username) {
		byte[] byteDomain = Crypto.decode(domain);
		byte[] byteUsername = Crypto.decode(username);

		String hashedDomain = null;
		String hashedUsername = null;

		hashedDomain = Crypto.decipherString(byteDomain, this.privKey);

		hashedUsername = Crypto.decipherString(byteUsername, this.privKey);

		return new String[] { hashedUsername, hashedDomain };
	}

}
