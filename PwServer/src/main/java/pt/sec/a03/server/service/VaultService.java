package pt.sec.a03.server.service;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import java.sql.Timestamp;
import java.text.ParseException;

import pt.sec.a03.crypto.Crypto;
import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.Triplet;
import pt.sec.a03.server.exception.InvalidSignatureException;
import pt.sec.a03.server.exception.InvalidTimestampException;

public class VaultService {

	private static final String aliasForServer = "server";
	private static final String serverKeyStorePath = "/Users/sigma/Desktop/Server1.jks";
	private static final String serverKeyStorePass = "insecure";
	PrivateKey privKey;

	// TODO I think that the password manager should be put in a constructor of
	// the vault service
	public void put(String publicKey, String signature, String timestamp, String hashPw, String cipherPassword,
			String cipheredHashUsername, String cipheredHashDomain) {

		System.out.println("Working Directory = " + System.getProperty("user.dir"));

		try {

			KeyStore ksServ = Crypto.readKeystoreFile(serverKeyStorePath, serverKeyStorePass.toCharArray());
			PrivateKey privateServer = Crypto.getPrivateKeyFromKeystore(ksServ, aliasForServer, serverKeyStorePass);

			// Verify freshness
			if (!Crypto.validTS(timestamp)) {
				throw new InvalidTimestampException("Invalid Timestamp");
			}

			// Decipher
			String stringHashDomain = Crypto.decipherString(Crypto.decode(cipheredHashDomain), privateServer);
			String stringHashUsername = Crypto.decipherString(Crypto.decode(cipheredHashUsername), privateServer);

			// Verify signature
			String serverSideTosign = stringHashUsername + stringHashDomain + timestamp + hashPw + cipherPassword;

			byte[] serverSideSig = Crypto.decode(signature);

			PublicKey pk = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));

			if (!Crypto.verifyDigitalSignature(serverSideSig, serverSideTosign.getBytes(), pk)) {
				throw new InvalidSignatureException("Invalid Signature");
			}

			PasswordManager pwm = new PasswordManager();
			Triplet t = pwm.saveTriplet(new Triplet(cipherPassword, stringHashUsername, stringHashDomain), publicKey);
			pwm.saveHash(t.getTripletID(), hashPw);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String[] get(String publicKey, String username, String domain, String stringTS, String stringSig) {
		
		PasswordManager pwm = new PasswordManager();
		String[] userAndDom = null;

		try {
			KeyStore ksServ = Crypto.readKeystoreFile(serverKeyStorePath, serverKeyStorePass.toCharArray());
			this.privKey = Crypto.getPrivateKeyFromKeystore(ksServ, aliasForServer, serverKeyStorePass);
			
			// Verify TimeStamp
			verifyTS(stringTS);

			// Decipher domain and username
			userAndDom = decipherUsernameAndDomain(domain, username);

			// Verify Signature
			String verifySig = userAndDom[0] + userAndDom[1] + stringTS;

			PublicKey cliPublicKey = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));

			if (!Crypto.verifyDigitalSignature(Crypto.decode(stringSig), verifySig.getBytes(), cliPublicKey)) {
				// TODO Throw proper exception
			}

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Triplet t = pwm.getTriplet(userAndDom[0], userAndDom[1]);

		String pwHashFromDB = pwm.getHash(userAndDom[0], userAndDom[1]);
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		stringTS = timestamp.toString();

		String toSign = stringTS + pwHashFromDB + t.getPassword();

		String sign = null;
		try {
			sign = Crypto.encode(Crypto.makeDigitalSignature(toSign.getBytes(), this.privKey));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new String[] { stringTS, sign, pwHashFromDB, t.getPassword() };
	}

	public void verifyTS(String stringTS) throws ParseException {
		if (!Crypto.validTS(stringTS)) {
			// TODO Throw TS Exception
		}
	}

	public String[] decipherUsernameAndDomain(String domain, String username) {
		byte[] byteDomain = Crypto.decode(domain);
		byte[] byteUsername = Crypto.decode(username);

		String hashedDomain = null;
		String hashedUsername = null;

		try {
			hashedDomain = Crypto.decipherString(byteDomain, this.privKey);
		} catch (Exception e) {
			// TODO Throw proper exeception
			e.printStackTrace();
		}
		try {
			hashedUsername = Crypto.decipherString(byteUsername, this.privKey);
		} catch (Exception e) {
			// TODO Throw proper exeception
			e.printStackTrace();
		}

		return new String[] { hashedUsername, hashedDomain };
	}

}
