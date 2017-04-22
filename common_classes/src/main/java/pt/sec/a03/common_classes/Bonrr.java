package pt.sec.a03.common_classes;

import pt.sec.a03.crypto.Crypto;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Bonrr {

    private static final String HASH_DOMAIN_IN_MAP = "domain";
    private static final String HASH_USERNAME_IN_MAP = "username";
    private static final String PASSWORD_IN_MAP = "password";
    private static final String HASH_PASSWORD_IN_MAP = "hash-password";

    private PublicKey cliPubKey;
    private PrivateKey cliPrivKey;
    private Map<String, String> servers;
    private Map<String, PublicKey> serversPubKey;
    private int counter;
    private String bonrr;
    private int wts;
    private ArrayList<String> acklist;
    private int rid;
    private ArrayList<String> readlist;
    private AuthLink authLink;



    public Bonrr(PublicKey cliPubKey, PrivateKey cliPrivKey, Map<String, String> servers, Map<String, PublicKey> serversPubKey, int counter){
        this.cliPubKey = cliPubKey;
        this.cliPrivKey = cliPrivKey;
        this.servers = servers;
        this.serversPubKey = serversPubKey;
        this.counter = counter;
        this.wts = 0;
        acklist = new ArrayList<String>();
        this.rid = 0;
        readlist = new ArrayList<String>();
        authLink = new AuthLink(this);

        try {
            this.bonrr = Crypto.encode(Crypto.hashString(Crypto.encode(cliPubKey.getEncoded()) + counter));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void write(HashMap<String, byte[]> infoToSend) {
        wts = wts + 1;
        acklist = new ArrayList<String>();
        for (String s : servers.keySet()) {

            //Cipher domain and username
            ArrayList<byte[]> dataCiphered = Crypto.cipher(new String[] { new String(infoToSend.get(HASH_DOMAIN_IN_MAP)),
                    new String(infoToSend.get(HASH_USERNAME_IN_MAP))},
                    serversPubKey.get(s));

            //Make signature
            byte[] sig = makeSignature(infoToSend);

            //Update values to send
            infoToSend.put(HASH_DOMAIN_IN_MAP, dataCiphered.get(0));
            infoToSend.put(HASH_USERNAME_IN_MAP, dataCiphered.get(1));

            //Send
            authLink.send(cliPrivKey, cliPubKey, servers.get(s),serversPubKey.get(s), sig, wts, infoToSend);
        }
    }

    public String read(HashMap<String, byte[]> infoToSend) {
        return null;
    }

    public synchronized void addToAckList(String ack, int wts){
        if(wts == this.wts)
            acklist.add(ack);
    }

    private byte[] makeSignature(HashMap<String, byte[]> infoToSend) {
        String toSign = bonrr + wts;
        toSign = toSign + new String(infoToSend.get(HASH_DOMAIN_IN_MAP));
        toSign = toSign + new String(infoToSend.get(HASH_USERNAME_IN_MAP));
        toSign = toSign + Crypto.encode(infoToSend.get(PASSWORD_IN_MAP));
        toSign = toSign + Crypto.encode(infoToSend.get(HASH_PASSWORD_IN_MAP));
        try {
            return Crypto.makeDigitalSignature(toSign.getBytes(), cliPrivKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
