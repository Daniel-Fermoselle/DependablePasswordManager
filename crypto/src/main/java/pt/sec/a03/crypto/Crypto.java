package pt.sec.a03.crypto;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;

public class Crypto {

	private static final long MINUTE_IN_MILLIS = 60000;// one minute in mls

	private static final String CIPHER_ALGORITHM = "RSA";

	//plain -> cipher -> encode  ---- > decode -> decipher -> plain
	/*___________________

	Function name: Register user
	
	encode(Sig(TS || encode(PK)) - signature in header
	PK in header - public-key in header
	TS in header - timestamp in header
	{
	}
		 

	Daniel
	Function name: Client Put request
	
	encode( Sig(Hash(U) || Hash(D) || TS || encode({Hash(PS)}PrivCli) || encode({PS}PubCli) )) - signature in header
	PK in header - public-key in header
	TS in header - timestamp in header
	encode({Hash(PS)}PrivCli) - hash-password in header
	{
		domain:   encode({{Hash(D)}PubServer)
		username: encode({Hash(U)}PubServer)
		password: encode({PS}PubCli)
	}
	
	BD:
	encode({Hash(PS)}PubCli)
	encode({PS}PubCli)
	
	Tiago
	Function name: Client Get request
	
	encode( Sig(Hash(U) || Hash(D) || TS)) - signature in header
	PK in header - public-key in header
	TS in header - time-stamp in header
	domain: encode({{Hash(D)}PubServer)
	username:encode({Hash(U)}PubServer)

	
	Marcal
	Function name: Server Get response
	
	encode(Sig(TS || encode({Hash(PS)}PrivCli) || encode({PS}PubCli))) - signature in header
	TS in header - time-stamp in header
	encode({Hash(PS)}PrivCli)   - hash in header
	{
		password: encode({PS}PubCli)
	}
	___________________*/
	
	public static PublicKey getPublicKeyFromCertificate(Certificate certificate) {
		return certificate.getPublicKey();
	}
	
	public static PublicKey getPubKeyFromByte(byte[] bytePubKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytePubKey));
	}

	public static Certificate readCertificateFile(String certificateFilePath) throws CertificateException, IOException {
		FileInputStream fis;

		try {
			fis = new FileInputStream(certificateFilePath);
		} catch (FileNotFoundException e) {
			System.err.println("Certificate file <" + certificateFilePath + "> not fount.");
			return null;
		}
		BufferedInputStream bis = new BufferedInputStream(fis);

		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		if (bis.available() > 0) {
			Certificate cert = cf.generateCertificate(bis);
			return cert;
		}
		bis.close();
		fis.close();
		return null;
	}
	
	public static PrivateKey getPrivateKeyFromKeystore(KeyStore keystore,
			String keyAlias, String keyPassword) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {

		PrivateKey key = (PrivateKey) keystore.getKey(keyAlias, keyPassword.toCharArray());

		return key;
	}

	public static KeyStore readKeystoreFile(String keyStoreFilePath, char[] keyStorePassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		FileInputStream fis;
		try {
			fis = new FileInputStream(keyStoreFilePath);
		} catch (FileNotFoundException e) {
			System.err.println("Keystore file <" + keyStoreFilePath + "> not fount.");
			return null;
		}
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(fis, keyStorePassword);
		return keystore;
	}

	public static byte[] cipherString(String toCipher, Key key) 
			throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		byte[] cipherText = null;

			// get an RSA cipher object and print the provider
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			// encrypt the plain text using the public key
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(toCipher.getBytes());
	
		return cipherText;
	}
		
	public static String decipherString(byte[] toDecipher, Key key) 
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] decipheredText = null;
	
	      // get an RSA cipher object and print the provider
	      Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

	      // decrypt the text using the private key
	      cipher.init(Cipher.DECRYPT_MODE, key);
	      decipheredText = cipher.doFinal(toDecipher);
	    return new String(decipheredText);
	}
	
	public static byte[] makeDigitalSignature(byte[] bytes, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

		// get a signature object using the SHA-256 and RSA combo
		// and sign the plain-text with the private key
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initSign(privateKey);
		sig.update(bytes);
		byte[] signature = sig.sign();

		return signature;
	}

	public static boolean verifyDigitalSignature(byte[] cipherDigest, byte[] bytes, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

		// verify the signature with the public key
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(publicKey);
		sig.update(bytes);
		try {
			return sig.verify(cipherDigest);
		} catch (SignatureException se) {
			System.err.println("Caught exception while verifying signature " + se);
			return false;
		}
	}

	public static String encode(byte[] msg) {
		String encodedMsg = new String(Base64.encodeBase64(msg));

		return encodedMsg;
	}

	public static byte[] decode(String msg) {
		byte[] decodedCipheredSms = Base64.decodeBase64(msg.getBytes());

		return decodedCipheredSms;
	}

	public static byte[] hashString(String toHash) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(toHash.getBytes());

        return md.digest();
	}

    public static ArrayList<byte[]> cipher(String[] strings, Key key) {
		ArrayList<byte[]> dataCiphered = new ArrayList<byte[]>();
		for (String string : strings) {
			try {
				dataCiphered.add(cipherString(string, key));
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException | InvalidKeyException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}
		return dataCiphered;
	}
}