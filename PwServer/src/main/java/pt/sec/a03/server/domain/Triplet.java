package pt.sec.a03.server.domain;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class Triplet {

	private String domain;
	private String username;
	private String password;
	private String hash;
	private String signature;
	private long wts;
	private long rid;
	private long rank;


	public Triplet() {}
	
	public Triplet(String domain, String username, String password, String pwHash, String signature, long wts,
				   long rid, long rank) {
		this.domain = domain;
		this.username = username;
		this.password = password;
		this.hash = pwHash;
		this.signature = signature;
		this.wts = wts;
		this.rid = rid;
		this.rank = rank;
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

	public String getSignature() { return signature; }

	public void setSignature(String signature) { this.signature = signature; }

    public long getWts() { return wts; }

    public void setWts(long wts) { this.wts = wts; }
    
    public long getRid() { return rid; }
    
    public void setRid(long rid) { this.rid = rid; }
    
    public long getRank() { return rank; }
    
    public void setRank(long rank) { this.rank = rank; }
}
