package pt.sec.a03.server.service;


import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.text.ParseException;

import pt.sec.a03.crypto.Crypto;
import pt.sec.a03.server.domain.PasswordManager;
import pt.sec.a03.server.domain.Triplet;

public class VaultService {

	PrivateKey privKey;
	
	//TODO I think that the password manager should be put in a constructor of the vault service
	public Triplet put(String publicKey, String password, String username, String domain){
		PasswordManager pwm =  new PasswordManager();
		return pwm.saveTriplet(new Triplet(password, username, domain), publicKey);
	}
	
	public String[] get(String publicKey, String username, String domain, String stringTS, String stringSig) {
		PasswordManager pwm =  new PasswordManager();
		String[] domAndUser = null;

		try {
			//Verify TimeStamp
			verifyTS(stringTS);
		
			//Decipher domain and username
			domAndUser = decipherUsernameAndDomain(domain, username);
			
			//Verify Signature
			String verifySig = domAndUser[1] + domAndUser[0] + stringTS;
			
			PublicKey cliPublicKey = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));
					
			if(!Crypto.verifyDigitalSignature(Crypto.decode(stringSig), verifySig.getBytes(), cliPublicKey)){
				//TODO Throw proper exception
			}

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		Triplet t = pwm.getTriplet(domAndUser[0],domAndUser[1]);
		
		String pwHashFromDB = pwm.getHash(domAndUser[0],domAndUser[1]);
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	    stringTS = timestamp.toString();
	    
	    String toSign = stringTS + pwHashFromDB + t.getPassword();
		
	    String sign = null;
		try {
			sign = Crypto.encode(Crypto.makeDigitalSignature(toSign.getBytes(), this.privKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new String[] {stringTS, sign, pwHashFromDB, t.getPassword()};
	}
	
	
	
	public void verifyTS(String stringTS) throws ParseException{
		if (!Crypto.validTS(stringTS)) {
			//TODO Throw TS Exception
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
			//TODO Throw proper exeception
			e.printStackTrace();
		}
		try {
			hashedUsername = Crypto.decipherString(byteUsername, this.privKey);
		} catch (Exception e) {
			//TODO Throw proper exeception
			e.printStackTrace();
		}
		
		return new String[] {hashedUsername, hashedDomain};
	}
	
}
