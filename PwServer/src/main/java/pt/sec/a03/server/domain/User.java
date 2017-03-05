package pt.sec.a03.server.domain;

public class User {

	private long userID;
	//TODO PublicKey instead of String
	private String publicKey;
	
	public User(){}
	
	public User(long userID, String publicKey) {
		this.userID = userID;
		this.publicKey = publicKey;
	}

	public long getUserID() {
		return userID;
	}
	
	public void setUserID(long userID) {
		this.userID = userID;
	}
	
	public String getPublicKey() {
		return publicKey;
	}
	
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
}
