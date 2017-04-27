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
    public static final int FAULT_NUMBER = 1;

    private PublicKey cliPubKey;
    private PrivateKey cliPrivKey;
    private Map<String, String> servers;
    private Map<String, PublicKey> serversPubKey;
    private String bonrr;
    private long wts;
    private ArrayList<String> acklist;
    private long rid;
    private ArrayList<String> readlist;
    private AuthLink authLink;


    public Bonrr(PublicKey cliPubKey, PrivateKey cliPrivKey, Map<String, String> servers, Map<String, PublicKey> serversPubKey, String bonrr) {
        this.cliPubKey = cliPubKey;
        this.cliPrivKey = cliPrivKey;
        this.servers = servers;
        this.serversPubKey = serversPubKey;
        this.wts = 0;
        acklist = new ArrayList<String>();
        this.rid = 0;
        readlist = new ArrayList<String>();
        authLink = new AuthLink(this);
        this.bonrr = bonrr;
    }

    public Bonrr(String bonrr, long wts) {
        this.bonrr = bonrr;
        this.wts = wts;
    }

    public String write(HashMap<String, byte[]> infoToSend) {
        wts = wts + 1;
        acklist = new ArrayList<String>();
        HashMap<String, byte[]> infoToSendTemp = new HashMap<>();

        for (String s : infoToSend.keySet()) {
            infoToSendTemp.put(s, infoToSend.get(s));
        }

        for (String s : servers.keySet()) {

            //Cipher domain and username
            ArrayList<byte[]> dataCiphered = Crypto.cipher(new String[]{new String(infoToSend.get(HASH_DOMAIN_IN_MAP)),
                            new String(infoToSend.get(HASH_USERNAME_IN_MAP))}, serversPubKey.get(s));

            //Make signature
            byte[] sig = makeSignature(infoToSend);

            //Update values to send
            infoToSendTemp.put(HASH_DOMAIN_IN_MAP, dataCiphered.get(0));
            infoToSendTemp.put(HASH_USERNAME_IN_MAP, dataCiphered.get(1));

            //Send
            authLink.send(cliPrivKey, cliPubKey, servers.get(s), sig, wts, infoToSendTemp, bonrr);
        }

        while (acklist.size() <= ((servers.keySet().size() + FAULT_NUMBER) / 2)){

        }
        acklist = new ArrayList<String>();
        return "Value Wrote\n";
    }

    public String read(HashMap<String, byte[]> infoToSend) {
        return null;
    }

    public boolean deliver(String wts) {
        if (Long.parseLong(wts) > this.wts) {
            return true;
        }
        return false;
    }

    public synchronized void addToAckList(String ack, long wts) {
        if (wts == this.wts) {
            acklist.add(ack);
        }
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

    public String getBonrrID() {
        return bonrr;
    }
}
