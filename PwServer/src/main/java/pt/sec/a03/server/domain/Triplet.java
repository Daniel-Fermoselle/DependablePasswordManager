package pt.sec.a03.server.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Triplet {

	private long tripletID;
	private long userID;
	private String password;
	private String username;
	private String domain;

	public Triplet() {}
	
	public Triplet(String password, String username, String domain) {
		super();
		this.tripletID = 0;
		this.userID = 0;
		this.password = password;
		this.username = username;
		this.domain = domain;
	}	

	public Triplet(long tripletID, long userID, String password, String username, String domain) {
		super();
		this.tripletID = tripletID;
		this.userID = userID;
		this.password = password;
		this.username = username;
		this.domain = domain;
	}	
	
	public long getTripletID() {
		return tripletID;
	}
	
	public void setTripletID(long tripletID) {
		this.tripletID = tripletID;
	}

	public long getUserID() {
		return userID;
	}
	
	public void setUserID(long userID) {
		this.userID = userID;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDomain() {
		return domain;
	}
	
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
}
