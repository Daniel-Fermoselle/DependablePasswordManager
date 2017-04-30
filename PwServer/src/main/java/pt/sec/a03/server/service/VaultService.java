package pt.sec.a03.server.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.BadRequestException;

import pt.sec.a03.common_classes.Bonrr;
import pt.sec.a03.crypto.Crypto;
import pt.sec.a03.server.MyApplication;
import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.Triplet;
import pt.sec.a03.server.exception.InvalidSignatureException;
import pt.sec.a03.server.exception.InvalidNonceException;

public class VaultService {

	private static final String SERVER_KEY_STORE_PASS = "insecure";

	PrivateKey privKey;
	PublicKey pubKey;
	KeyStore ksServ;
	PasswordManager pwm;

	// Ver se bonrr existe se nao cria-lo

	// Instacia-lo com args

	// Passar msg ao bonrr

	public VaultService() {
		try {
			String paramForKeys = getAlias(MyApplication.PORT);
			this.ksServ = Crypto.readKeystoreFile("ks/" + paramForKeys + ".jks", SERVER_KEY_STORE_PASS.toCharArray());
			Certificate cert = ksServ.getCertificate(paramForKeys);
			if (cert == null) {
				throw new RuntimeException("No certificate for server");
			}
			this.pubKey = Crypto.getPublicKeyFromCertificate(cert);
			this.privKey = Crypto.getPrivateKeyFromKeystore(ksServ, paramForKeys, SERVER_KEY_STORE_PASS);
			this.pwm = new PasswordManager();

		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
				| UnrecoverableKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public String[] put(String publicKey, Triplet t, String bonrr) {
		// Decipher
		String[] userAndDom = decipherUsernameAndDomain(t.getDomain(), t.getUsername());

		// Get Bonrr instance
		Bonrr bonrrInstance = pwm.getBonrrInstance(bonrr, userAndDom[1], userAndDom[0]);

		System.out.println("Got BonrrInstance");

		// Verify deliver
		if (bonrrInstance.deliver(t.getWts(), t.getRank(), userAndDom[1], userAndDom[0])) {
			System.out.println("Going to update/put Bonrr");

            System.out.println("Going to verify sig");
            System.out.println("Bonrr: " + bonrr + " \nWts: " + t.getWts() + " Rid: " + t.getRid() + " Rank: " + t.getRank());
            // Verify signature
			String serverSideTosign = bonrr + (t.getWts() + "") + (t.getRank() + "") + userAndDom[1] + userAndDom[0]
					+ t.getPassword() + t.getHash();
			verifySignature(publicKey, t.getSignature(), serverSideTosign);

            System.out.println("Verified update/put Bonrr");

            Triplet triplet = new Triplet(userAndDom[1], userAndDom[0], t.getPassword(), t.getHash(), t.getSignature(),
					t.getWts(),t.getRid(), t.getRank());

			// Save to bonrr
            System.out.println("Going to save update/put Bonrr");
            pwm.saveBonrr(bonrr, triplet);
		}

		// Response
		String[] cipherUserDom = cipherUsernameAndDomain(userAndDom[1], userAndDom[0], publicKey);

		String ackMsg = "ACK" + t.getWts() + userAndDom[1] + userAndDom[0];

		// Make signature
		String sign = makeSignature(ackMsg);

		return new String[] {Crypto.encode(this.pubKey.getEncoded()), sign, "ACK", t.getWts() + "", cipherUserDom[1],
				cipherUserDom[0] };
	}

	public String[] get(String publicKey, String username, String domain, String rid, String bonrr) {

		// Decipher domain and username
		String[] userAndDom = decipherUsernameAndDomain(domain, username);

		// Get Bonrr instance
		Triplet bonrrInfo = pwm.getBonrr(bonrr, userAndDom[0], userAndDom[1]);

		String[] cipherUserDom = cipherUsernameAndDomain(bonrrInfo.getDomain(), bonrrInfo.getUsername(), publicKey);
		bonrrInfo.setDomain(cipherUserDom[1]);
		bonrrInfo.setUsername(cipherUserDom[0]);

		/*
		 * if(!bonrrInstance.deliver(rid, READ_MODE)){ throw new
		 * InvalidNonceException("rid with wrong value"); }
		 */

		// Make signature
		String toSign = rid + (bonrrInfo.getWts() + "") + (bonrrInfo.getRank() + "") + bonrrInfo.getDomain()
                + bonrrInfo.getUsername() + bonrrInfo.getPassword() + bonrrInfo.getHash() + bonrrInfo.getSignature();
		String sign = makeSignature(toSign);

		return new String[] { Crypto.encode(this.pubKey.getEncoded()), sign, rid, bonrrInfo.getWts() + "",
				bonrrInfo.getRank() + "", bonrrInfo.getDomain(), bonrrInfo.getUsername(), bonrrInfo.getPassword(),
				bonrrInfo.getHash(), bonrrInfo.getSignature() };
	}

	private String[] cipherUsernameAndDomain(String domain, String username, String publicKey) {
		try {
			PublicKey pubKey = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));
			String cipheredDomain = Crypto.encode(Crypto.cipherString(domain, pubKey));
			String cipheredUsername = Crypto.encode(Crypto.cipherString(username, pubKey));
			return new String[] { cipheredUsername, cipheredDomain };
		} catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | InvalidKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public String[] decipherUsernameAndDomain(String domain, String username) {
		try {
			byte[] byteDomain = Crypto.decode(domain);
			byte[] byteUsername = Crypto.decode(username);

			String hashedDomain = null;
			String hashedUsername = null;

			hashedDomain = Crypto.decipherString(byteDomain, this.privKey);

			hashedUsername = Crypto.decipherString(byteUsername, this.privKey);

			return new String[] { hashedUsername, hashedDomain };
		} catch (InvalidKeyException e) {
			throw new pt.sec.a03.server.exception.InvalidKeyException(e.getMessage());
		} catch (BadPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException
				| NoSuchPaddingException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	private String makeSignature(String toSign) {
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

	public void verifySignature(String publicKey, String signature, String serverSideTosign) {
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

	private String getAlias(String port) {
		try {
			String fileString = new String(Files.readAllBytes(Paths.get("metadata/metadata.in")),
					StandardCharsets.UTF_8);
			String[] args = fileString.split("\n");
			for (String arg : args) {
				if (arg.startsWith(port)) {
					String[] split = arg.split(",");
					return split[4];
				}
			}
			throw new RuntimeException("No matching alias to port");
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
