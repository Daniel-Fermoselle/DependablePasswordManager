package pt.sec.a03.server.domain;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class Triplet {

	private long tripletID;
	private long userID;
	private String domain;
	private String username;
	private String password;
	private String hash;

	public Triplet() {}
	
	public Triplet(String domain, String username, String password, String pwHash) {
		super();
		this.tripletID = 0;
		this.userID = 0;
		this.domain = domain;
		this.username = username;
		this.password = password;
		this.hash = pwHash;
	}	

	public Triplet(long tripletID, long userID, String domain, String username, String password, String pwHash) {
		super();
		this.tripletID = tripletID;
		this.userID = userID;
		this.domain = domain;
		this.username = username;
		this.password = password;
		this.hash = pwHash;
	}	
	
	@XmlTransient
	public long getTripletID() {
		return tripletID;
	}
	
	public void setTripletID(long tripletID) {
		this.tripletID = tripletID;
	}

	@XmlTransient
	public long getUserID() {
		return userID;
	}
	
	public void setUserID(long userID) {
		this.userID = userID;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHash() {
		return hash;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	
}
