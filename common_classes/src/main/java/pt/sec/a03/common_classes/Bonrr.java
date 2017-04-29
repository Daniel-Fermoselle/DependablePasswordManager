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
	private static final String WTS_IN_MAP = "wts";
	private static final String SIGNATURE_IN_MAP = "signature";

	private static final int FAULT_NUMBER = 1;

	private PublicKey cliPubKey;
	private PrivateKey cliPrivKey;
	private Map<String, String> servers;
	private Map<String, PublicKey> serversPubKey;
	private String bonrr;
	private long wts;
	private ArrayList<String> acklist;
	private long rid;
	private ArrayList<HashMap<String, String>> readlist;
	private AuthLink authLink;

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

			// Cipher domain and username
			ArrayList<byte[]> dataCiphered = Crypto.cipher(new String[] {
					new String(infoToSend.get(HASH_DOMAIN_IN_MAP)), new String(infoToSend.get(HASH_USERNAME_IN_MAP)) },
					serversPubKey.get(s));

			// Make signature
			byte[] sig = makeSignature(infoToSend);

			// Update values to send
			infoToSendTemp.put(HASH_DOMAIN_IN_MAP, dataCiphered.get(0));
			infoToSendTemp.put(HASH_USERNAME_IN_MAP, dataCiphered.get(1));

			// Send
			authLink.send(cliPrivKey, cliPubKey, servers.get(s), sig, wts, infoToSendTemp, bonrr);
		}

		while (acklist.size() <= ((servers.keySet().size() + FAULT_NUMBER) / 2)) {

		}
		acklist = new ArrayList<String>();
		return "Value Wrote\n";
	}

	public String read(HashMap<String, byte[]> infoToSend) {
		rid = rid++;
		readlist = new ArrayList<HashMap<String, String>>();
		String password = "Champog";
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
			authLink.send(cliPrivKey, cliPubKey, servers.get(s), wts, infoToSendTemp, bonrr);
		}

		while (readlist.size() <= ((servers.keySet().size() + FAULT_NUMBER) / 2)) {}

		HashMap<String, String> highestValue = highestVal(readlist);
		try {
			// Decipher password
			String passwordReceived = highestValue.get(PASSWORD_IN_MAP);
			password = Crypto.decipherString(Crypto.decode(passwordReceived), cliPrivKey);

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

		readlist = new ArrayList<HashMap<String, String>>();

		return password;
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

	public synchronized void addToReadList(HashMap<String, String> value, long readid) {
		if (readid == this.rid) {
			readlist.add(value);
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
}
