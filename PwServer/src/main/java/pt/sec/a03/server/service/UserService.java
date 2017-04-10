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
	private static final String SERVER_KEY_STORE_PATH = "ks/Server1.jks";
	private static final String SERVER_KEY_STORE_PASS = "insecure";
	private PrivateKey privKey;
	private KeyStore ksServ;


	public UserService(){
		try {
			this.ksServ = Crypto.readKeystoreFile(SERVER_KEY_STORE_PATH, SERVER_KEY_STORE_PASS.toCharArray());
			this.privKey = Crypto.getPrivateKeyFromKeystore(ksServ, ALIAS_FOR_SERVER, SERVER_KEY_STORE_PASS);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException
				| IOException | UnrecoverableKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public User getUserByPK(String publicKey) {
		PasswordManager pwm = new PasswordManager();
		return pwm.getUserByPK(publicKey);
	}

	public String[] addUser(String publicKey, String signature, String nonce) {

		String stringNonce = decipherAndDecode(nonce);

		//verify nonce
		verifyNonce(publicKey, Long.parseLong(stringNonce));

		//verify Signature
		String toVerify = stringNonce + publicKey;
		verifySignature(publicKey, signature, toVerify);

		PasswordManager pwm = new PasswordManager();
		pwm.addUser(publicKey);

		//--prepare answer--//
		//Get new nonce
		String[] nonceNormCiph = getNewNonceForUser(publicKey);

		//Make signature
		String toSign = nonceNormCiph[0];
		String sign = signString(toSign);

		return new String[]{sign, nonceNormCiph[1]};
	}

	public String[] getUserMetaInfo(String publicKey) {
		try {
			PublicKey cliPublicKey = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));

			PasswordManager pwm = new PasswordManager();
			String stringNonce = pwm.getUserMetaInfo(publicKey);

			//Cipher nonce
			byte[] cipherNonce = Crypto.cipherString(stringNonce, cliPublicKey);
			String encodedNonce = Crypto.encode(cipherNonce);

			//Make signature
			String sign = signString(stringNonce);

			return new String[] { encodedNonce, sign };

		} catch (NoSuchAlgorithmException e) {
			throw new BadRequestException(e.getMessage());
		}  catch (InvalidKeyException e) {
			throw new pt.sec.a03.server.exception.InvalidKeyException(e.getMessage());
		} catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeySpecException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public String[] getNewNonceForUser(String publicKey) {
		try {
			PublicKey cliPublicKey = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));

			//Get new nonce
			PasswordManager pwm = new PasswordManager();
			String nonceToSend = pwm.getNewNonceForUser(publicKey);

			//Cipher nonce
			byte[] cipherNonce = Crypto.cipherString(nonceToSend, cliPublicKey);
			String encodedNonce = Crypto.encode(cipherNonce);

			return new String[]{nonceToSend, encodedNonce};
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			throw new BadRequestException(e.getMessage());
		} catch (InvalidKeySpecException | InvalidKeyException e) {
			throw new pt.sec.a03.server.exception.InvalidKeyException(e.getMessage());
		}
	}

	private String decipherAndDecode(String toDecipher) {
		try {
			return Crypto.decipherString(Crypto.decode(toDecipher), this.privKey);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
				| IllegalBlockSizeException |  BadPaddingException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	private void verifyNonce(String publicKey, long nonce) {
		PasswordManager pwm = new PasswordManager();
		if(!pwm.validateNonceForUer(publicKey, nonce)){
			throw new InvalidNonceException("Invalid Nonce");
		}
	}

	private String signString(String toSign) {
		try {
			return Crypto.encode(Crypto.makeDigitalSignature(toSign.getBytes(), this.privKey));
		} catch (NoSuchAlgorithmException e) {
			throw new BadRequestException(e.getMessage());
		} catch (SignatureException e) {
			throw new InvalidSignatureException(e.getMessage());
		} catch (InvalidKeyException e) {
			throw new pt.sec.a03.server.exception.InvalidKeyException(e.getMessage());
		}
	}

	private void verifySignature(String publicKey, String signature, String serverSideTosign) {
		try {
			byte[] serverSideSig = Crypto.decode(signature);

			PublicKey pk = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));

			if (!Crypto.verifyDigitalSignature(serverSideSig, serverSideTosign.getBytes(), pk)) {
				throw new InvalidSignatureException("Invalid Signature");
			}
		} catch (InvalidKeySpecException | InvalidKeyException e) {
			throw new pt.sec.a03.server.exception.InvalidKeyException(e.getMessage());
		} catch (SignatureException e) {
			throw new InvalidSignatureException(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			throw new BadRequestException(e.getMessage());
		}
	}
}
