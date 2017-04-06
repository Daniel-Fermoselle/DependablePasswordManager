package pt.sec.a03.server.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import pt.sec.a03.crypto.Crypto;
import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.User;
import pt.sec.a03.server.exception.InvalidSignatureException;

public class UserService {

	private PasswordManager pwm = new PasswordManager();
	public User getUserByID(String id) {
		return pwm.getUserByID(id);
	}

	public User getUserByPK(String publicKey) {
		return pwm.getUserByPK(publicKey);
	}

	public void addUser(String publicKey, String signature, String timestamp) {
		try {
			String toSign = timestamp + publicKey;
			byte[] serverSideSig = Crypto.decode(signature);
			PublicKey pk;
				pk = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));
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
}
