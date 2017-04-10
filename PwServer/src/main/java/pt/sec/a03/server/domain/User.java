package pt.sec.a03.server.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class User {

	private long userID;
	private String publicKey;
	private long nonce;
	
	public User(){}
	
	public User(long userID, String publicKey) {
		this.userID = userID;
		this.publicKey = publicKey;
	}

	public User(long userID, String publicKey, long nonce) {
		this.userID = userID;
		this.publicKey = publicKey;
		this.nonce = nonce;
	}
	
	public long getUserID() {
		return userID;
	}
	
	public void setUserID(long userID) {
		this.userID = userID;
	}
	
	public void setUserNonce(long nonce) {
		this.nonce = nonce;
	}
	
	public String getPublicKey() {
		return publicKey;
	}
	
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public long getNonce() {
		return this.nonce;
	}
	
}
