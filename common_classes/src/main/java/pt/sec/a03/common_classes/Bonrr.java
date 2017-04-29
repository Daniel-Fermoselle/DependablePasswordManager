package pt.sec.a03.common_classes;

import pt.sec.a03.crypto.Crypto;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Bonrr {

	private static final String HASH_DOMAIN_IN_MAP = "domain";
	private static final String HASH_USERNAME_IN_MAP = "username";
	private static final String PASSWORD_IN_MAP = "password";
	private static final String HASH_PASSWORD_IN_MAP = "hash-password";
	private static final String WTS_IN_MAP = "wts";
	private static final String RID_IN_MAP = "map-rid";
	private static final String SIGNATURE_IN_MAP = "signature";
	private static final String RANK_IN_MAP = "map-rank";

	public static final int FAULT_NUMBER = 1;

	private PublicKey cliPubKey;
	private PrivateKey cliPrivKey;
	private Map<String, String> servers;
	private Map<String, PublicKey> serversPubKey;
	private String bonrr;
    private long rank;
    private ArrayList<String> acklist;
    private ArrayList<HashMap<String, String>> readlist;

    private long wts;
    private long rid;
    private AuthLink authLink;
    private boolean reading;
	private HashMap<String, byte[]> writeVal;

	public Bonrr(PublicKey cliPubKey, PrivateKey cliPrivKey, Map<String, String> servers,
			Map<String, PublicKey> serversPubKey, String bonrr) {
		this.cliPubKey = cliPubKey;
		this.cliPrivKey = cliPrivKey;
		this.servers = servers;
		this.serversPubKey = serversPubKey;
		this.wts = 0;
		acklist = new ArrayList<String>();
		this.rid = 0;
		readlist = new ArrayList<HashMap<String, String>>();
		authLink = new AuthLink(this, this.cliPubKey, this.cliPrivKey);
		this.bonrr = bonrr;
		reading=false;
		writeVal = new HashMap<String, byte[]>();
		this.rank = 100;
	}

	public Bonrr(String bonrr, long wts, long rank) {
		this.bonrr = bonrr;
		this.wts = wts;
		this.rank = rank;
	}

	public String write(HashMap<String, byte[]> infoToSend) {
		//Set variables to write
		rid = rid + 1;
        this.writeVal=infoToSend;
        acklist = new ArrayList<String>();
        readlist = new ArrayList<HashMap<String, String>>();

		//Preform read
		HashMap<String, byte[]> infoToRead = new HashMap<String, byte[]>();
		infoToRead.put(HASH_DOMAIN_IN_MAP, infoToSend.get(HASH_DOMAIN_IN_MAP));
		infoToRead.put(HASH_USERNAME_IN_MAP, infoToSend.get(HASH_USERNAME_IN_MAP));
		return readBroadcast(infoToRead);
	}

	public String read(HashMap<String, byte[]> infoToSend) {
        //Set variables to read
        rid = rid + 1;
        acklist =  new ArrayList<String>();
        readlist = new ArrayList<HashMap<String, String>>();
        reading=true;

        //Preform read
        return readBroadcast(infoToSend);
	}

    private String readBroadcast(HashMap<String, byte[]> infoToSend) {
        HashMap<String, byte[]> infoToSendTemp = new HashMap<>();

        for (String s : infoToSend.keySet()) {
            infoToSendTemp.put(s, infoToSend.get(s));
        }

        for (String s : servers.keySet()) {
            // Cipher domain and username
            ArrayList<byte[]> dataCiphered = Crypto.cipher(new String[] {
                            new String(infoToSend.get(HASH_DOMAIN_IN_MAP)), new String(infoToSend.get(HASH_USERNAME_IN_MAP)) },
                    serversPubKey.get(s));

            // Update values to send
            infoToSendTemp.put(HASH_DOMAIN_IN_MAP, dataCiphered.get(0));
            infoToSendTemp.put(HASH_USERNAME_IN_MAP, dataCiphered.get(1));

            // Send
            authLink.send(servers.get(s), rid, infoToSendTemp, bonrr);
        }

        while (readlist.size() <= ((servers.keySet().size() + FAULT_NUMBER) / 2)) {
        }

        HashMap<String, byte[]> readBroadcastInfo = new HashMap<String, byte[]>();
        if(verifyNoValueRead()){
            this.wts =  this.wts + 1;
            readBroadcastInfo.put(HASH_DOMAIN_IN_MAP, writeVal.get(HASH_DOMAIN_IN_MAP));
            readBroadcastInfo.put(HASH_USERNAME_IN_MAP, writeVal.get(HASH_USERNAME_IN_MAP));
            readBroadcastInfo.put(PASSWORD_IN_MAP, writeVal.get(PASSWORD_IN_MAP));
            readBroadcastInfo.put(HASH_PASSWORD_IN_MAP, writeVal.get(HASH_PASSWORD_IN_MAP));
            return writeBroadcast(readBroadcastInfo);
        }

        HashMap<String, String> highestValue = highestVal(readlist);
        verifyReadPassword(highestValue);

        readlist = new ArrayList<HashMap<String, String>>();

        if(reading){
            this.wts = Long.parseLong(highestValue.get(WTS_IN_MAP));
            readBroadcastInfo.put(RANK_IN_MAP, highestValue.get(RANK_IN_MAP).getBytes());
            readBroadcastInfo.put(HASH_DOMAIN_IN_MAP, highestValue.get(HASH_DOMAIN_IN_MAP).getBytes());
            readBroadcastInfo.put(HASH_USERNAME_IN_MAP, highestValue.get(HASH_USERNAME_IN_MAP).getBytes());
            readBroadcastInfo.put(PASSWORD_IN_MAP, Crypto.decode(highestValue.get(PASSWORD_IN_MAP)));
            readBroadcastInfo.put(HASH_PASSWORD_IN_MAP, Crypto.decode(highestValue.get(HASH_PASSWORD_IN_MAP)));

        }
        else{
            this.wts = Long.parseLong(highestValue.get(WTS_IN_MAP)) + 1;
            readBroadcastInfo.put(HASH_DOMAIN_IN_MAP, writeVal.get(HASH_DOMAIN_IN_MAP));
            readBroadcastInfo.put(HASH_USERNAME_IN_MAP, writeVal.get(HASH_USERNAME_IN_MAP));
            readBroadcastInfo.put(PASSWORD_IN_MAP, writeVal.get(PASSWORD_IN_MAP));
            readBroadcastInfo.put(HASH_PASSWORD_IN_MAP, writeVal.get(HASH_PASSWORD_IN_MAP));
        }
        this.rid = Long.parseLong(highestValue.get(RID_IN_MAP));
        return writeBroadcast(readBroadcastInfo);
    }

    public String writeBroadcast(HashMap<String, byte[]> infoToSend) {
        try {
            HashMap<String, byte[]> infoToSendTemp = new HashMap<>();

            for (String s : infoToSend.keySet()) {
                infoToSendTemp.put(s, infoToSend.get(s));
            }

            for (String s : servers.keySet()) {

                // Cipher domain and username
                ArrayList<byte[]> dataCiphered = Crypto
                        .cipher(new String[] { new String(infoToSend.get(HASH_DOMAIN_IN_MAP)),
                                new String(infoToSend.get(HASH_USERNAME_IN_MAP)) }, serversPubKey.get(s));

                // Make signature
                byte[] sig = makeSignature(infoToSend);
                infoToSendTemp.put(SIGNATURE_IN_MAP, sig);

                // Update values to send
                infoToSendTemp.put(HASH_DOMAIN_IN_MAP, dataCiphered.get(0));
                infoToSendTemp.put(HASH_USERNAME_IN_MAP, dataCiphered.get(1));

                // Send
                if(reading)
                    authLink.send(servers.get(s),wts, rid, Long.parseLong(new String (infoToSend.get(RANK_IN_MAP))),
                            infoToSendTemp, bonrr);
                else
                    authLink.send(servers.get(s), wts, rid, rank, infoToSendTemp, bonrr);

            }

            while (acklist.size() <= ((servers.keySet().size() + FAULT_NUMBER) / 2)) {}

            acklist = new ArrayList<String>();

            if (reading) {
                reading = false;
                return Crypto.decipherString(infoToSendTemp.get(PASSWORD_IN_MAP), cliPrivKey);
            }
            return "Value Wrote\n";

        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                | BadPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

	public boolean deliver(Long wts, Long rank) {
		if (wts > this.wts || (wts == this.wts && rank > this.rank)) {
			return true;
		}
		return false;
	}

    private boolean verifyNoValueRead() {
        int nrOfErrors = 0;
        for(HashMap<String,String> readlistElement: readlist){
            if(readlistElement==null){
                nrOfErrors++;
            }
        }
        //ERROR CASE FOR A WRITE WITHOUT PREVIOUS VALUE
        if(nrOfErrors>(servers.keySet().size() + FAULT_NUMBER) / 2){
            return true;
        }
        return false;
    }

	public HashMap<String, String> highestVal(ArrayList<HashMap<String, String>> readlist) {
		long highestWts = 0;
		HashMap<String, String> highestValue = new HashMap<String, String>();

		for (HashMap<String, String> ocurrency : readlist) {
			if (Long.parseLong(ocurrency.get(WTS_IN_MAP)) > highestWts) {
				highestWts = Long.parseLong(ocurrency.get(WTS_IN_MAP));
				highestValue = ocurrency;
			}
		}
		return highestValue;
	}

    private byte[] makeSignature(HashMap<String, byte[]> infoToSend) {
        System.out.println("Bonrr: " + bonrr + " Wts: " + wts + " Rid: " + rid + " Rank: " + rank);
        String toSign = bonrr + (wts + "") + (rank + "");
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

    private void verifyReadPassword(HashMap<String, String> highestValue) {
        try {
            // Decipher password
            String passwordReceived = highestValue.get(PASSWORD_IN_MAP);
            String password = Crypto.decipherString(Crypto.decode(passwordReceived), cliPrivKey);

            // Verify if password's hash is correct
            byte[] hashToVerify = Crypto.hashString(password);
            byte[] cipheredHashReceived = Crypto.decode(highestValue.get(HASH_PASSWORD_IN_MAP));
            String hashReceived = Crypto.decipherString(cipheredHashReceived, cliPubKey);
            if (!hashReceived.equals(new String(hashToVerify))) {
                throw new RuntimeException("Password doesn't match with what was written.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public synchronized void addToAckList(String ack, long wts) {
        if (wts == this.wts) {
            acklist.add(ack);
        }
    }

    public synchronized void addToReadList(HashMap<String, String> value, long readid) {
        if(readid==-1){
            System.out.println("addToReadList: Reading without anything written");
            readlist.add(value);
        }
        System.out.println("RID received: " + readid + " Local Rid: " + this.rid);
        if (readid == this.rid) {
            readlist.add(value);
        }
    }

    public boolean getReading() {
		return this.reading;
	}
}
