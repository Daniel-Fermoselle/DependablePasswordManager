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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.BadRequestException;

import pt.sec.a03.crypto.Crypto;
import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.User;
import pt.sec.a03.server.exception.InvalidSignatureException;
import pt.sec.a03.server.exception.InvalidNonceException;

public class UserService {

	private static final String ALIAS_FOR_SERVER = "server";
	private static final String SERVER_KEY_STORE_PATH = "/Users/sigma/Desktop/Server1.jks";
	private static final String SERVER_KEY_STORE_PASS = "insecure";
	private PasswordManager pwm;
	PrivateKey privKey;
	KeyStore ksServ;

	public UserService() {
		try {
			this.ksServ = Crypto.readKeystoreFile(SERVER_KEY_STORE_PATH, SERVER_KEY_STORE_PASS.toCharArray());
			this.privKey = Crypto.getPrivateKeyFromKeystore(ksServ, ALIAS_FOR_SERVER, SERVER_KEY_STORE_PASS);
			this.pwm = new PasswordManager();

		} catch (KeyStoreException | NoSuchAlgorithmException
				| CertificateException | IOException
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

	public void addUser(String publicKey, String signature, String nonce) {
		try {

			//verify nonce
			verifyNonce(publicKey, Long.parseLong(nonce));

			//verify Signature
			String toSign = nonce + publicKey;
			byte[] serverSideSig = Crypto.decode(signature);
			PublicKey pk = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));
			if (!Crypto.verifyDigitalSignature(serverSideSig, toSign.getBytes(), pk)) {
				throw new InvalidSignatureException("Invalid Signature");
			}

			pwm.addUser(publicKey);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} catch (InvalidKeySpecException | InvalidKeyException e) {
			throw new pt.sec.a03.server.exception.InvalidKeyException(e.getMessage());
		} catch (SignatureException e) {
			throw new InvalidSignatureException("Invalid Signature");
		}
	}

	public void updateUserWithID(String id, String publicKey) {

		pwm.updateUserWithID(id, publicKey);
	}

	public String[] getUserMetaInfo(String publicKey) {
		try {
			PublicKey cliPublicKey = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));

			String stringNonce = pwm.getUserMetaInfo(publicKey);

			//Cipher nonce
			byte[] cipherNonce = Crypto.cipherString(stringNonce, cliPublicKey);
			String encodedNonce = Crypto.encode(cipherNonce);

			//Make signature
			byte[] sig = Crypto.makeDigitalSignature(stringNonce.getBytes(), this.privKey);
			String encodedSig = Crypto.encode(sig);

			return new String[] { encodedNonce, encodedSig };

		} catch (NoSuchAlgorithmException e) {
			throw new BadRequestException(e.getMessage());
		} catch (SignatureException e) {
			throw new InvalidSignatureException(e.getMessage());
		} catch (InvalidKeyException e) {
			throw new pt.sec.a03.server.exception.InvalidKeyException(e.getMessage());
		} catch (BadPaddingException | IllegalBlockSizeException
				| NoSuchPaddingException | InvalidKeySpecException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	private void verifyNonce(String publicKey, long nonce) {
		if(!pwm.validateNonceForUer(publicKey, nonce)){
			throw new InvalidNonceException("Invalid Nonce");
		}
	}
}
