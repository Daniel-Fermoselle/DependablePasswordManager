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
	KeyStore ksServ;
	PasswordManager pwm;

	//Ver se bonrr existe se nao cria-lo

    //Instacia-lo com args

    //Passar msg ao bonrr

	public VaultService() {
		try {
            String paramForKeys = getAlias(MyApplication.PORT);
            this.ksServ = Crypto.readKeystoreFile("ks/" + paramForKeys + ".jks", SERVER_KEY_STORE_PASS.toCharArray());
			this.privKey = Crypto.getPrivateKeyFromKeystore(ksServ, paramForKeys, SERVER_KEY_STORE_PASS);
			this.pwm = new PasswordManager();

		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
				| UnrecoverableKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} 
	}

    public String[] put(String publicKey, String signature, String wts, String hashPw, String cipherPassword,
                        String cipheredHashUsername, String cipheredHashDomain, String bonrr) {

	    Bonrr bonrrInstance = pwm.verifyBonrr(bonrr);

	    if(!bonrrInstance.deliver(wts)){
            throw new InvalidNonceException("wts with wrong value");
        }

        // Decipher
        String[] userAndDom = decipherUsernameAndDomain(cipheredHashDomain, cipheredHashUsername);

        // Verify signature
        String serverSideTosign = userAndDom[0] + userAndDom[1] + stringNonce + hashPw + cipherPassword;
        verifySignature(publicKey, signature, serverSideTosign);

        pwm.saveBonrr(bonrr, wts, cipheredHashDomain, cipheredHashUsername, cipherPassword, hashPw);

        //Save Pass
        Triplet t = pwm.saveTriplet(new Triplet(cipherPassword, userAndDom[0], userAndDom[1]), publicKey);
        pwm.saveHash(t.getTripletID(), hashPw);

        //Get new nonce
        String[] nonceNormCiph = getNewNonceForUser(publicKey);

        //Make signature
        String toSign = nonceNormCiph[0];
        String sign = signString(toSign);

        return new String[] { sign, nonceNormCiph[1]};
    }


    public String[] get(String publicKey, String username, String domain, String nonce, String signature) {

        String stringNonce = decipherAndDecode(nonce);

        // Verify nonce
        verifyNonce(publicKey, Long.parseLong(stringNonce));

        // Decipher domain and username
        String[] userAndDom = decipherUsernameAndDomain(domain, username);

        // Verify Signature
        String serverSideTosign = userAndDom[0] + userAndDom[1] + stringNonce;
        verifySignature(publicKey, signature, serverSideTosign);

        //Get pass and hash
        Triplet t = pwm.getTriplet(userAndDom[0], userAndDom[1], publicKey);
        String pwHashFromDB = pwm.getHash(userAndDom[0], userAndDom[1], publicKey);

        //Get new nonce
        String[] nonceNormCiph = getNewNonceForUser(publicKey);

        //Make signature
        String toSign = nonceNormCiph[0] + pwHashFromDB + t.getPassword();
        String sign = signString(toSign);

        return new String[]{nonceNormCiph[1], sign, pwHashFromDB, t.getPassword()};
    }

    public String[] getNewNonceForUser(String publicKey) {
        try {
            PublicKey cliPublicKey = Crypto.getPubKeyFromByte(Crypto.decode(publicKey));

            //Get new nonce
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

    public void verifyNonce(String publicKey, long nonce) {
        if (!pwm.validateNonceForUer(publicKey, nonce)) {
            throw new InvalidNonceException("Invalid Nonce");
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

            return new String[]{hashedUsername, hashedDomain};
        } catch (InvalidKeyException e) {
            throw new pt.sec.a03.server.exception.InvalidKeyException(e.getMessage());
        } catch (BadPaddingException | NoSuchAlgorithmException
                | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new BadRequestException(e.getMessage());
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

    private String decipherAndDecode(String toDecipher) {
        try {
            return Crypto.decipherString(Crypto.decode(toDecipher), this.privKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | IllegalBlockSizeException |  BadPaddingException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private String getAlias(String port) {
        try {
            String fileString = new String(Files.readAllBytes(Paths.get("metadata/metadata.in")), StandardCharsets.UTF_8);
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
