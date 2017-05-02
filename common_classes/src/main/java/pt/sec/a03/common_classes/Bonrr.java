package pt.sec.a03.common_classes;

import pt.sec.a03.common_classes.exception.DataNotFoundException;
import pt.sec.a03.common_classes.exception.IllegalAccessExistException;
import pt.sec.a03.common_classes.exception.InvalidSignatureException;
import pt.sec.a03.crypto.Crypto;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;

public class Bonrr {

	private static final String HASH_DOMAIN_IN_MAP = "domain";
	private static final String HASH_USERNAME_IN_MAP = "username";
	private static final String PASSWORD_IN_MAP = "password";
	private static final String HASH_PASSWORD_IN_MAP = "hash-password";
	private static final String WTS_IN_MAP = "wts";
	private static final String RID_IN_MAP = "map-rid";
	private static final String SIGNATURE_IN_MAP = "signature";
	private static final String RANK_IN_MAP = "map-rank";
	
	private static final String BAD_REQUEST_MSG = "Invalid Request";
	private static final String BAD_REQUEST_EXCEPTION_MSG = "There were an problem with the headers of the request";
	private static final String FORBIDEN_MSG = "Forbiden operation";
	private static final String DATA_NOT_FOUND_MSG = "Data Not Found";
	private static final String SERVER_ERROR_MSG = "Internal server error";
	private static final String INTERNAL_SERVER_FAILURE_EXCEPTION_MSG = "There were an problem with the server";

	public static final int FAULT_NUMBER = 1;
    private static final String SIG_TO_VERIFY = "sig-to-verify";

    private PublicKey cliPubKey;
	private PrivateKey cliPrivKey;
	private Map<String, String> servers;
	private Map<String, PublicKey> serversPubKey;
	private String bonrr;
    private long rank;
    private ArrayList<String> acklist;
    private ArrayList<HashMap<String, String>> readlist;

    private ArrayList<HashMap<String, String>> errorlist;

    //<Domain, <Username, wts>>
    private HashMap<String, HashMap<String, Long>> wts;
    private long rid;
    private AuthLink authLink;
    private boolean reading;
	private HashMap<String, byte[]> writeVal;

	public Bonrr(PublicKey cliPubKey, PrivateKey cliPrivKey, Map<String, String> servers,
			Map<String, PublicKey> serversPubKey, String bonrr, long rank) {
		this.cliPubKey = cliPubKey;
		this.cliPrivKey = cliPrivKey;
		this.servers = servers;
		this.serversPubKey = serversPubKey;
		this.wts = new HashMap<>();
		acklist = new ArrayList<String>();
		this.rid = 0;
		readlist = new ArrayList<HashMap<String, String>>();
		authLink = new AuthLink(this, this.cliPubKey, this.cliPrivKey);
		this.bonrr = bonrr;
		reading=false;
		writeVal = new HashMap<String, byte[]>();
		this.rank = rank;
		this.errorlist = new ArrayList<HashMap<String, String>>();
	}

	public Bonrr(String bonrr, long wts, long rank, String domain, String username) {
		this.bonrr = bonrr;
		this.wts = new HashMap<>();
        updateWts(domain, username, wts);
		this.rank = rank;
	}

    public String write(HashMap<String, byte[]> infoToSend) {
		//Set variables to write
		this.rid = rid + 1;
        this.writeVal = infoToSend;
        this.acklist = new ArrayList<String>();
        this.readlist = new ArrayList<HashMap<String, String>>();
        this.errorlist = new ArrayList<>();

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
        this.errorlist = new ArrayList<>();

        //Preform read
        return readBroadcast(infoToSend);
	}

    private String readBroadcast(HashMap<String, byte[]> infoToSend) {
        String domain = new String(infoToSend.get(HASH_DOMAIN_IN_MAP));
        String username = new String(infoToSend.get(HASH_USERNAME_IN_MAP));

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
            authLink.send(serversPubKey.get(s), servers.get(s), rid, infoToSendTemp, bonrr);
        }

        while (readlist.size() <= ((servers.keySet().size() + FAULT_NUMBER) / 2)) {
            if(errorlist.size() > ((servers.keySet().size() + FAULT_NUMBER) / 2)) {
                checkErrorOccurences(errorlist);
            }
        }

        //ERROR CASE FOR A WRITE WITHOUT PREVIOUS VALUE
        HashMap<String, byte[]> readBroadcastInfo = new HashMap<String, byte[]>();
        if(verifyNoValueRead()){
            readlist = new ArrayList<HashMap<String, String>>();
            updateWts(domain, username, getWts(domain, username) + 1);
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
            updateWts(domain, username, Long.parseLong(highestValue.get(WTS_IN_MAP)));
            readBroadcastInfo.put(RANK_IN_MAP, highestValue.get(RANK_IN_MAP).getBytes());
            readBroadcastInfo.put(HASH_DOMAIN_IN_MAP, highestValue.get(HASH_DOMAIN_IN_MAP).getBytes());
            readBroadcastInfo.put(HASH_USERNAME_IN_MAP, highestValue.get(HASH_USERNAME_IN_MAP).getBytes());
            readBroadcastInfo.put(PASSWORD_IN_MAP, Crypto.decode(highestValue.get(PASSWORD_IN_MAP)));
            readBroadcastInfo.put(HASH_PASSWORD_IN_MAP, Crypto.decode(highestValue.get(HASH_PASSWORD_IN_MAP)));

        }
        else{
            updateWts(domain, username, Long.parseLong(highestValue.get(WTS_IN_MAP)) + 1);
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
            String domain = new String(infoToSend.get(HASH_DOMAIN_IN_MAP));
            String username = new String(infoToSend.get(HASH_USERNAME_IN_MAP));

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
                    authLink.send(serversPubKey.get(s), servers.get(s), getWts(domain, username), rid,
                            Long.parseLong(new String (infoToSend.get(RANK_IN_MAP))), infoToSendTemp, bonrr);
                else
                    authLink.send(serversPubKey.get(s), servers.get(s), getWts(domain, username), rid, rank, infoToSendTemp, bonrr);

            }


            while (acklist.size() <= ((servers.keySet().size() + FAULT_NUMBER) / 2)) {
                if(errorlist.size() > ((servers.keySet().size() + FAULT_NUMBER) / 2)) {
                    checkErrorOccurences(errorlist);
                }
            }

            readlist = new ArrayList<HashMap<String, String>>();
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

	public boolean deliver(Long wts, Long rank, String domain, String username) {
		if (wts > getWts(domain, username) || (wts == getWts(domain, username) && rank > this.rank)) {
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

        if(nrOfErrors > (servers.keySet().size() + FAULT_NUMBER) / 2){
            return true;
        }
        return false;
    }

	public HashMap<String, String> highestVal(ArrayList<HashMap<String, String>> readlist) {
		long highestWts = 0;
		HashMap<String, String> highestValue = new HashMap<String, String>();

		for (HashMap<String, String> ocurrency : readlist) {
			if (Long.parseLong(ocurrency.get(WTS_IN_MAP)) > highestWts) {
			    try {
                    verifySignature(this.cliPubKey, ocurrency.get(SIGNATURE_IN_MAP), ocurrency.get(SIG_TO_VERIFY));
                    highestWts = Long.parseLong(ocurrency.get(WTS_IN_MAP));
                    highestValue = ocurrency;
                } catch (InvalidSignatureException e){
			        continue;
                }
			}
		}
		return highestValue;
	}
	
	public void checkErrorOccurences(ArrayList<HashMap<String, String>> readlist) {
		String status = null;
		int max = 0;
		HashMap<String, Integer> ocurrences = new HashMap<String, Integer>();
		ocurrences.put("400", 0);
		ocurrences.put("403", 0);
		ocurrences.put("404", 0);
		ocurrences.put("500", 0);
		try {
			for (HashMap<String, String> ocurrency : readlist) {
				for (String error : ocurrency.keySet()) {
					if (error.equals("400") || error.equals("403") || error.equals("404") || error.equals("500")) {
						ocurrences.put(error, ocurrences.get(error) + 1);
					}
				}
			}
		} catch (NullPointerException e) {
            return;
		}

		for (String ocurrency : ocurrences.keySet()) {
			if (ocurrences.get(ocurrency) > max) {
				status = ocurrency;
				max = ocurrences.get(ocurrency);
			}
		}

		if (status == null) {
			return;
		}
        if (status.equals("400")) {
			System.out.println(BAD_REQUEST_MSG);
			throw new BadRequestException(BAD_REQUEST_EXCEPTION_MSG);
		} else if (status.equals("403")) {
			System.out.println(FORBIDEN_MSG);
			throw new IllegalAccessExistException("This combination of username and domain already exists");
		} else if (status.equals("404")) {
			System.out.println(DATA_NOT_FOUND_MSG);
			throw new DataNotFoundException("This public key is not registered in the server");
		} else if (status.equals("500")) {
			System.out.println(SERVER_ERROR_MSG);
			throw new InternalServerErrorException(INTERNAL_SERVER_FAILURE_EXCEPTION_MSG);
		}

	}

    private byte[] makeSignature(HashMap<String, byte[]> infoToSend) {
        String domain = new String(infoToSend.get(HASH_DOMAIN_IN_MAP));
        String username = new String(infoToSend.get(HASH_USERNAME_IN_MAP));
        System.out.println("Bonrr: " + bonrr + " Wts: " + getWts(domain, username) + " Rid: " + rid + " Rank: " + rank);
        String toSign = bonrr + (getWts(domain, username) + "") + (rank + "");
        toSign = toSign + domain;
        toSign = toSign + username;
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

    public synchronized void addToAckList(String ack, long wts, long rid, String domain, String username) {
        if (wts == getWts(domain, username) && rid == this.rid) {
            acklist.add(ack);
        }
    }

    public synchronized void addToReadList(HashMap<String, String> value, long readid) {
        System.out.println("RID received: " + readid + " Local Rid: " + this.rid);
        if (readid == this.rid) {
            readlist.add(value);
        }
    }

    public boolean getReading() {
		return this.reading;
	}

    private void updateWts(String domain, String username, long wts) {
        HashMap<String, Long> toAdd = new HashMap<>();
        toAdd.put(username, wts);
        this.wts.put(domain, toAdd);
    }

    private long getWts(String domain, String username) {
        if(this.wts == null){
            this.wts = new HashMap<>();
            updateWts(domain, username, 0);
            return 0;

        }
        else if (this.wts.get(domain) == null){
            updateWts(domain, username, 0);
            return 0;
        }
        else {
            return this.wts.get(domain).get(username);
        }
    }

    public synchronized void addToErrorList(HashMap<String, String> value) {
        errorlist.add(value);
    }

    private void verifySignature(PublicKey publicKey, String signatureToVer, String signature) {
        try {
            if (!Crypto.verifyDigitalSignature(Crypto.decode(signatureToVer), signature.getBytes(), publicKey)) {
                throw new InvalidSignatureException("Invalid Signature on Bonrr");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

}
