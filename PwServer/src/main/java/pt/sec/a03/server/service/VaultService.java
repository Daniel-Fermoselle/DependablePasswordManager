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
import pt.sec.a03.server.domain.Triplet;
import pt.sec.a03.server.exception.InvalidSignatureException;
import pt.sec.a03.server.exception.InvalidNonceException;

public class VaultService {

    private static final String ALIAS_FOR_SERVER = "server";
    private static final String SERVER_KEY_STORE_PATH = "/Users/sigma/Desktop/Server1.jks";
    private static final String SERVER_KEY_STORE_PASS = "insecure";

    PrivateKey privKey;
    KeyStore ksServ;
    PasswordManager pwm;

    public VaultService() {
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

    public void put(String publicKey, String signature, String nonce, String hashPw, String cipherPassword,
                    String cipheredHashUsername, String cipheredHashDomain) {
        // Verify nonce
        verifyNonce(publicKey, Long.parseLong(nonce));

        // Decipher
        String[] userAndDom = decipherUsernameAndDomain(cipheredHashDomain, cipheredHashUsername);

        // Verify signature
        String serverSideTosign = userAndDom[0] + userAndDom[1] + nonce + hashPw + cipherPassword;
        verifySignature(publicKey, signature, serverSideTosign);

        //Save Pass
        Triplet t = pwm.saveTriplet(new Triplet(cipherPassword, userAndDom[0], userAndDom[1]), publicKey);
        pwm.saveHash(t.getTripletID(), hashPw);
    }


    public String[] get(String publicKey, String username, String domain, String nonce, String signature) {

        // Verify nonce
        verifyNonce(publicKey, Long.parseLong(nonce));

        // Decipher domain and username
        String[] userAndDom = decipherUsernameAndDomain(domain, username);

        // Verify Signature
        String serverSideTosign = userAndDom[0] + userAndDom[1] + nonce;
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


}
