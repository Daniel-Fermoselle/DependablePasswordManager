package pt.sec.a03.server.domain;

public class User {

	private String publicKey;
	private long nonce;

	public User() {
	}

	public User(long userID, String publicKey) {
		this.publicKey = publicKey;
	}

	public User(String publicKey, long nonce) {
		this.publicKey = publicKey;
		this.nonce = nonce;
	}

	public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

	public String getPublicKey() {
		return publicKey;
	}

	public void setNonce(long nonce) { this.nonce = nonce; }

	public long getNonce() { return this.nonce; }
}
