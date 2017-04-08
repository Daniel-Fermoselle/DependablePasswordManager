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

import pt.sec.a03.crypto.Crypto;
import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.User;
import pt.sec.a03.server.exception.InvalidSignatureException;

public class UserService {

	private static final String ALIAS_FOR_SERVER = "server";
	private static final String SERVER_KEY_STORE_PATH = "/Users/daniel/Desktop/Server1.jks";
	private static final String SERVER_KEY_STORE_PASS = "insecure";
	private PasswordManager pwm;
	private KeyStore ksServ;
	private PrivateKey privKey;
	
	public UserService(){
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
	
	public User getUserByID(String id) {
		return pwm.getUserByID(id);
	}

	public User getUserByPK(String publicKey) {
		return pwm.getUserByPK(publicKey);
	}

	public String[] addUser(String publicKey, String signature, String timestamp) {
		try {
			String toSign = timestamp + publicKey;
			byte[] serverSideSig = Crypto.decode(signature);
			PublicKey pk;
				pk = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));
			if (!Crypto.verifyDigitalSignature(serverSideSig, toSign.getBytes(), pk)) {
				throw new InvalidSignatureException("Invalid Signature");
			}
			pwm.addUser(publicKey);
			//--prepare answer--//
			Timestamp ts = new Timestamp(System.currentTimeMillis());
			String stringTs = ts.toString();
			String sig = Crypto.encode(Crypto.makeDigitalSignature(toSign.getBytes(), privKey));
			return new String[] {sig, stringTs};
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}  catch (InvalidKeySpecException | InvalidKeyException e) {
			throw new pt.sec.a03.server.exception.InvalidKeyException(e.getMessage());
		} catch (SignatureException e) {
			throw new InvalidSignatureException("Invalid Signature");
		}
	}

	public void updateUserWithID(String id, String publicKey) {
		pwm.updateUserWithID(id, publicKey);
	}
}
