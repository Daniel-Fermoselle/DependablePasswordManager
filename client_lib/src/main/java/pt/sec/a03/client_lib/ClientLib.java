package pt.sec.a03.client_lib;

import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.sql.Timestamp;
import java.text.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;

import pt.sec.a03.client_lib.exception.AlreadyExistsException;
import pt.sec.a03.client_lib.exception.DataNotFoundException;
import pt.sec.a03.client_lib.exception.IllegalAccessExistException;
import pt.sec.a03.client_lib.exception.InvalidArgumentException;
import pt.sec.a03.client_lib.exception.InvalidReceivedPasswordException;
import pt.sec.a03.client_lib.exception.InvalidSignatureException;
import pt.sec.a03.client_lib.exception.InvalidTimestampException;
import pt.sec.a03.client_lib.exception.UnexpectedErrorExeception;
import pt.sec.a03.client_lib.exception.UsernameAndDomainAlreadyExistException;
import pt.sec.a03.common_classes.CommonTriplet;
import pt.sec.a03.crypto.Crypto;

public class ClientLib {

    // Keys related constants
    private static final String ALIAS_FOR_SERVER_PUB_KEY = "server";

    // Connection related constants
    private static final String BASE_TARGET_URI = "http://localhost:8080/PwServer/webapi/";
    private static final String VAULT_URI = "vault";
    private static final String USERS_URI = "users";

    private static final String PUBLIC_KEY_HEADER_NAME = "public-key";
    private static final String SIGNATURE_HEADER_NAME = "signature";
    private static final String NONCE_HEADER_NAME = "nonce-value";
    private static final String HASH_PASSWORD_HEADER_NAME = "hash-password";
    private static final String DOMAIN_HEADER_NAME = "domain";
    private static final String USERNAME_HEADER_NAME = "username";

    // Internal message constants
    private static final String SUCCESS_MSG = "Success";
    private static final String FORBIDEN_MSG = "Forbiden operation";
    private static final String ALREADY_EXISTS_MSG = "Entity already exists";
    private static final String DATA_NOT_FOUND_MSG = "Data Not Found";
    private static final String BAD_REQUEST_MSG = "Invalid Request";
    private static final String SERVER_ERROR_MSG = "Internal server error";
    private static final String ELSE_MSG = "Error";

    private static final String NULL_ARGUMENSTS_MSG = "One of the arguments was null";
    private static final String OVERSIZE_PASSWORD_MSG = "Password to big to the system 245 bytes maximum";
    private static final String INVALID_TIMESTAMP_EXCEPTION_MSG = "The timestamp received is invalid";
    private static final String BAD_REQUEST_EXCEPTION_MSG = "There were an problem with the headers of the request";
    private static final String INTERNAL_SERVER_FAILURE_EXCEPTION_MSG = "There were an problem with the server";
    private static final String UNEXPECTED_ERROR_EXCEPTION_MSG = "There was an unexpected error";

    // Attributes
    private KeyStore ks;
    private String aliasForPubPrivKeys;
    private String keyStorePw;
    private long nonce;

    private Client client = ClientBuilder.newClient();
    private WebTarget baseTarget = client.target(BASE_TARGET_URI);
    private WebTarget vaultTarget = baseTarget.path(VAULT_URI);
    private WebTarget userTarget = baseTarget.path(USERS_URI);

    public void init(KeyStore ks, String aliasForPubPrivKey, String keyStorePw) {
        if (ks == null || aliasForPubPrivKey == null || keyStorePw == null) {
            throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
        }
        this.ks = ks;
        this.aliasForPubPrivKeys = aliasForPubPrivKey;
        this.keyStorePw = keyStorePw;
        checkArguments();
        getMetaInfo();
    }

    public void register_user() {
        String[] infoToSend = prepareForRegisterUser();
        Response response = sendRegisterUser(infoToSend);
        processRegisterUser(response);
    }

    public String[] prepareForRegisterUser() {
        // Get PubKey from key store
        Certificate cert;
        try {
            cert = ks.getCertificate(aliasForPubPrivKeys);
            PublicKey pubKey = Crypto.getPublicKeyFromCertificate(cert);
            PrivateKey clientprivKey = Crypto.getPrivateKeyFromKeystore(ks, aliasForPubPrivKeys, keyStorePw);

            // Generate timestamp
            String stringNonce = nonce + "";

            String stringPubKey = Crypto.encode(pubKey.getEncoded());

            // Generate signature
            String tosign = stringNonce + stringPubKey;
            String sig = Crypto.encode(Crypto.makeDigitalSignature(tosign.getBytes(), clientprivKey));

            return new String[]{sig, stringPubKey, stringNonce};

        } catch (KeyStoreException | InvalidKeyException | NoSuchAlgorithmException | SignatureException
                | UnrecoverableKeyException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public Response sendRegisterUser(String[] infoToSend) {

        return userTarget.request()
                .header(SIGNATURE_HEADER_NAME, infoToSend[0])
                .header(PUBLIC_KEY_HEADER_NAME, infoToSend[1])
                .header(NONCE_HEADER_NAME, infoToSend[2])
                .post(Entity.json(null));
    }

    public void processRegisterUser(Response postResponse) {
        if (postResponse.getStatus() == 201) {
            System.out.println(SUCCESS_MSG);
        } else if (postResponse.getStatus() == 400) {
            System.out.println(BAD_REQUEST_MSG);
            throw new BadRequestException(BAD_REQUEST_EXCEPTION_MSG);
        } else if (postResponse.getStatus() == 409) {
            System.out.println(ALREADY_EXISTS_MSG);
            throw new AlreadyExistsException("This public key already exists in the server");
        } else if (postResponse.getStatus() == 500) {
            System.out.println(SERVER_ERROR_MSG);
            throw new InternalServerErrorException(INTERNAL_SERVER_FAILURE_EXCEPTION_MSG);
        } else {
            System.out.println(ELSE_MSG);
            throw new UnexpectedErrorExeception(UNEXPECTED_ERROR_EXCEPTION_MSG);
        }
    }

    public void save_password(String domain, String username, String password) {
        if (domain == null || username == null || password == null) {
            throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
        }
        if (password.length() >= 246) {
            throw new InvalidArgumentException(OVERSIZE_PASSWORD_MSG);
        }

        String[] infoToSend = prepareForSave(domain, username, password);
        Response response = sendSavePassword(infoToSend);
        processSavePassword(response);
    }

    public String[] prepareForSave(String domain, String username, String password) {
        try {
            Certificate cert = ks.getCertificate(aliasForPubPrivKeys);
            PublicKey clientPubKey = Crypto.getPublicKeyFromCertificate(cert);
            PrivateKey clientprivKey = Crypto.getPrivateKeyFromKeystore(ks, aliasForPubPrivKeys, keyStorePw);

            Certificate serverCert = ks.getCertificate(ALIAS_FOR_SERVER_PUB_KEY);
            PublicKey serverPubKey = Crypto.getPublicKeyFromCertificate(serverCert);

            // --------Initial hashs and timestamp
            byte[] hashDomain = Crypto.hashString(domain);
            byte[] hashUsername = Crypto.hashString(username);
            byte[] hashPassword = Crypto.hashString(password);
            String stringNonce = nonce + "";
            // --------

            // ---------Creation of the string used to make the signature to use
            // in the header
            String stringHashDomain = new String(hashDomain);
            String stringHashUsername = new String(hashUsername);

            // --------Ciphered hashs and string conversion for them
            byte[] cipherDomain = Crypto.cipherString(stringHashDomain, serverPubKey);
            byte[] cipherUsername = Crypto.cipherString(stringHashUsername, serverPubKey);
            byte[] cipherPassword = Crypto.cipherString(password, clientPubKey);
            byte[] cipherHashPassword = Crypto.cipherString(new String(hashPassword), clientprivKey);

            String StringCipheredDomain = Crypto.encode(cipherDomain);
            String StringCipheredUsername = Crypto.encode(cipherUsername);
            String StringCipheredPassword = Crypto.encode(cipherPassword);
            String headerHashPassword = Crypto.encode(cipherHashPassword);
            // ---------

            String dataToSign = stringHashUsername + stringHashDomain + stringNonce + headerHashPassword
                    + StringCipheredPassword;

            String sig = Crypto.encode(Crypto.makeDigitalSignature(dataToSign.getBytes(), clientprivKey));
            // ---------

            // -------
            String stringPubKey = Crypto.encode(clientPubKey.getEncoded());

            return new String[]{stringPubKey, sig, stringNonce, headerHashPassword, StringCipheredPassword,
                    StringCipheredUsername, StringCipheredDomain};

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | KeyStoreException
                | UnrecoverableKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public Response sendSavePassword(String[] infoToSend) {
        CommonTriplet commonTriplet = new CommonTriplet(infoToSend[4], infoToSend[5], infoToSend[6]);

        return vaultTarget.request()
                .header(PUBLIC_KEY_HEADER_NAME, infoToSend[0])
                .header(SIGNATURE_HEADER_NAME, infoToSend[1])
                .header(NONCE_HEADER_NAME, infoToSend[2])
                .header(HASH_PASSWORD_HEADER_NAME, infoToSend[3])
                .post(Entity.json(commonTriplet));
    }

    public void processSavePassword(Response postResponse) {
        if (postResponse.getStatus() == 201) {
            System.out.println(SUCCESS_MSG);
        } else if (postResponse.getStatus() == 400) {
            System.out.println(BAD_REQUEST_MSG);
            throw new BadRequestException(BAD_REQUEST_EXCEPTION_MSG);
        } else if (postResponse.getStatus() == 403) {
            System.out.println(FORBIDEN_MSG);
            throw new UsernameAndDomainAlreadyExistException("This combination of username and domain already exists");
        } else if (postResponse.getStatus() == 404) {
            System.out.println(DATA_NOT_FOUND_MSG);
            throw new DataNotFoundException("This public key is not registered in the server");
        } else if (postResponse.getStatus() == 500) {
            System.out.println(SERVER_ERROR_MSG);
            throw new InternalServerErrorException(INTERNAL_SERVER_FAILURE_EXCEPTION_MSG);
        } else {
            System.out.println(ELSE_MSG);
            throw new UnexpectedErrorExeception(UNEXPECTED_ERROR_EXCEPTION_MSG);
        }
    }

    public String retrive_password(String domain, String username) {
        if (domain == null || username == null) {
            throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
        }
        String[] infoToSend = prepareForRetrivePassword(domain, username);
        Response response = sendRetrivePassword(infoToSend);

        return processRetrivePassword(response);
    }

    public String[] prepareForRetrivePassword(String domain, String username) {
        try {
            // Get keys and certificates
            Certificate cert1 = ks.getCertificate(aliasForPubPrivKeys);
            PublicKey pubKeyClient = Crypto.getPublicKeyFromCertificate(cert1);
            Certificate cert2 = ks.getCertificate(ALIAS_FOR_SERVER_PUB_KEY);
            PublicKey pubKeyServer = Crypto.getPublicKeyFromCertificate(cert2);
            PrivateKey privateKey = Crypto.getPrivateKeyFromKeystore(ks, aliasForPubPrivKeys, keyStorePw);

            // Hash domain and username
            byte[] hashDomain = Crypto.hashString(domain);
            byte[] hashUsername = Crypto.hashString(username);
            String stringHashDomain = new String(hashDomain);
            String stringHashUsername = new String(hashUsername);

            // Generate timestamp
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String stringTS = timestamp.toString();

            // Cipher domain and username hash with server public key
            byte[] cipheredDomain = Crypto.cipherString(stringHashDomain, pubKeyServer);
            byte[] cipheredUsername = Crypto.cipherString(stringHashUsername, pubKeyServer);
            String encodedDomain = Crypto.encode(cipheredDomain);
            String encodedUsername = Crypto.encode(cipheredUsername);

            // Generate signature
            String tosign = stringHashUsername + stringHashDomain + stringTS;
            String sig = Crypto.encode(Crypto.makeDigitalSignature(tosign.getBytes(), privateKey));

            String stringPubKey = Crypto.encode(pubKeyClient.getEncoded());

            return new String[]{stringPubKey, sig, stringTS, encodedDomain, encodedUsername};

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | KeyStoreException
                | UnrecoverableKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public Response sendRetrivePassword(String[] infoToSend) {

        return vaultTarget.request()
                .header(PUBLIC_KEY_HEADER_NAME, infoToSend[0])
                .header(SIGNATURE_HEADER_NAME, infoToSend[1])
                .header(NONCE_HEADER_NAME, infoToSend[2])
                .header(DOMAIN_HEADER_NAME, infoToSend[3])
                .header(USERNAME_HEADER_NAME, infoToSend[4])
                .get();
    }

    public String processRetrivePassword(Response getResponse) {
        try {
            // Get keys and certificates
            Certificate cert1 = ks.getCertificate(aliasForPubPrivKeys);
            PublicKey pubKeyClient = Crypto.getPublicKeyFromCertificate(cert1);
            Certificate cert2 = ks.getCertificate(ALIAS_FOR_SERVER_PUB_KEY);
            PublicKey pubKeyServer = Crypto.getPublicKeyFromCertificate(cert2);
            PrivateKey privateKey = Crypto.getPrivateKeyFromKeystore(ks, aliasForPubPrivKeys, keyStorePw);

            if (getResponse.getStatus() == 400) {
                System.out.println(BAD_REQUEST_MSG);
                throw new BadRequestException(BAD_REQUEST_EXCEPTION_MSG);
            } else if (getResponse.getStatus() == 403) {
                System.out.println(FORBIDEN_MSG);
                throw new IllegalAccessExistException("This combination of username and domain already exists");
            } else if (getResponse.getStatus() == 404) {
                System.out.println(DATA_NOT_FOUND_MSG);
                throw new DataNotFoundException("This public key is not registered in the server");
            } else if (getResponse.getStatus() == 500) {
                System.out.println(SERVER_ERROR_MSG);
                throw new InternalServerErrorException(INTERNAL_SERVER_FAILURE_EXCEPTION_MSG);
            }

            // Decipher password
            String passwordReceived = getResponse.readEntity(CommonTriplet.class).getPassword();
            String password = Crypto.decipherString(Crypto.decode(passwordReceived), privateKey);

            // Get headers info
            String sigToVerify = getResponse.getHeaderString(SIGNATURE_HEADER_NAME);
            String stringNonce = getResponse.getHeaderString(NONCE_HEADER_NAME);
            String encodedHashReceived = getResponse.getHeaderString(HASH_PASSWORD_HEADER_NAME);

            // Check timestamp freshness
            if (!validNonce(stringNonce)) {
                throw new InvalidTimestampException(INVALID_TIMESTAMP_EXCEPTION_MSG);
            }

            // Verify signature
            String sig = stringNonce + encodedHashReceived + passwordReceived;
            byte[] sigBytes = Crypto.decode(sigToVerify);
            if (!Crypto.verifyDigitalSignature(sigBytes, sig.getBytes(), pubKeyServer)) {
                throw new InvalidSignatureException("The signature is wrong");
            }

            // Verify if password's hash is correct
            byte[] hashToVerify = Crypto.hashString(password);
            byte[] cipheredHashReceived = Crypto.decode(encodedHashReceived);
            String hashReceived = Crypto.decipherString(cipheredHashReceived, pubKeyClient);
            if (!hashReceived.equals(new String(hashToVerify))) {
                throw new InvalidReceivedPasswordException("Password received was different than the one sent to the server");
            }

            if (getResponse.getStatus() == 200) {
                System.out.println(SUCCESS_MSG);

                return password;

            } else {
                System.out.println(ELSE_MSG);
                throw new UnexpectedErrorExeception(UNEXPECTED_ERROR_EXCEPTION_MSG);
            }

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | KeyStoreException
                | UnrecoverableKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private boolean validNonce(String stringNonce) {
        long receivedNonce = Long.parseLong(stringNonce);
        if (receivedNonce > nonce) {
            return true;
        } else {
            return false;
        }
    }

    public void close() {
        ks = null;
    }

    private void checkArguments() {
        Certificate cer1 = null;
        Certificate cer2 = null;
        try {
            cer1 = ks.getCertificate(aliasForPubPrivKeys);
            cer2 = ks.getCertificate(ALIAS_FOR_SERVER_PUB_KEY);
            Crypto.getPrivateKeyFromKeystore(ks, aliasForPubPrivKeys, keyStorePw);
        } catch (UnrecoverableKeyException | KeyStoreException e) {
            throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cer1 == null || cer2 == null) {
            throw new InvalidArgumentException(NULL_ARGUMENSTS_MSG);
        }
    }

    private void getMetaInfo() {
        try {
            Certificate cert = ks.getCertificate(aliasForPubPrivKeys);
            PublicKey pubKey = Crypto.getPublicKeyFromCertificate(cert);
            Certificate cert2 = ks.getCertificate(ALIAS_FOR_SERVER_PUB_KEY);
            PublicKey pubKeyServer = Crypto.getPublicKeyFromCertificate(cert2);
            PrivateKey privateKey = Crypto.getPrivateKeyFromKeystore(ks, aliasForPubPrivKeys, keyStorePw);

            String stringPubKey = Crypto.encode(pubKey.getEncoded());

            Response response = userTarget.request()
                    .header(PUBLIC_KEY_HEADER_NAME, stringPubKey)
                    .get();

            String stringNonceCiph = response.getHeaderString(NONCE_HEADER_NAME);
            String stringSig = response.getHeaderString(SIGNATURE_HEADER_NAME);

            String stringNonce = Crypto.decipherString(Crypto.decode(stringNonceCiph), privateKey);
            byte[] sigBytes = Crypto.decode(stringSig);

            String sig = stringNonce;

            if (!Crypto.verifyDigitalSignature(sigBytes, sig.getBytes(), pubKeyServer)) {
                throw new InvalidSignatureException("The signature is wrong");
            }

            this.nonce = Long.parseLong(stringNonce);

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

    }


}
