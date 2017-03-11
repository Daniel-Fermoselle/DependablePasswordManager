package pt.sec.a03.server.service;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import pt.sec.a03.crypto.Crypto;
import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.Triplet;
import pt.sec.a03.server.exception.InvalidSignatureException;
import pt.sec.a03.server.exception.InvalidTimestampException;

public class VaultService {

	private static final String aliasForServer = "server";
	private static final String serverKeyStorePath = "/Users/daniel/Desktop/test/cenas/Server1.jks";
	private static final String serverKeyStorePass = "insecure";
	
	//TODO I think that the password manager should be put in a constructor of the vault service
	public Triplet put(String publicKey, String signature, String timestamp, String hashPw,
			String cipherPassword, String cipheredHashUsername, String cipheredHashDomain){
		Triplet t = null;
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		try{

	    KeyStore ksServ = Crypto.readKeystoreFile(serverKeyStorePath, serverKeyStorePass.toCharArray());
	    PrivateKey privateServer = Crypto.getPrivateKeyFromKeystore(ksServ, aliasForServer, serverKeyStorePass);

	    // Verify freshness 
	    if(!Crypto.validTS(timestamp)){
	    	throw new InvalidTimestampException("Invalid Timestamp");
	    }

	    // Decipher
	    String stringHashDomain = Crypto.decipherString(Crypto.decode(cipheredHashDomain),
	            privateServer);
	    String stringHashUsername = Crypto.decipherString(Crypto.decode(cipheredHashUsername),
	            privateServer);

	    String password = new String(Crypto.decode(cipherPassword));
	    
	    // Verify signature
	    //byte[] hashPassword = Crypto.decode(hashPw);
	    //String stringHashPassword = new String(hashPassword);
	    //String stringCipheredPassword = new String(Crypto.decode(cipherPassword));

	    String serverSideTosign = stringHashUsername + stringHashDomain + timestamp + hashPw +
	    		cipherPassword;

	    byte[] serverSideSig = Crypto.decode(serverSideTosign);
	    
	    byte[] pk = Crypto.decode(publicKey);
	    X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(pk);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pubKey = kf.generatePublic(X509publicKey);
	    
	    if(!Crypto.verifyDigitalSignature(serverSideSig, serverSideTosign.getBytes(), pubKey)){
	    	throw new InvalidSignatureException("Invalid Signature");
	    }
	    
	    PasswordManager pwm =  new PasswordManager();
		t=pwm.saveTriplet(new Triplet(password, stringHashUsername, stringHashDomain), publicKey);
		
		}
		catch(NoSuchAlgorithmException e){
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}
	
	public Triplet get(String publicKey, String username, String domain) {
		PasswordManager pwm =  new PasswordManager();
		Triplet t = pwm.getTriplet(username,domain);
		Triplet send = new Triplet();
		send.setPassword(t.getPassword());
		return send;
	}
	
}
