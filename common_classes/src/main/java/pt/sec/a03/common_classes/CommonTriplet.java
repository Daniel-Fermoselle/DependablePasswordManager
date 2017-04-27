package pt.sec.a03.common_classes;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class CommonTriplet {

	private String domain;
	private String username;
	private String password;
	private String hash;

	public CommonTriplet() {}

	public CommonTriplet(String domain, String username, String password, String pwHash) {
		this.domain = domain;
		this.username = username;
		this.password = password;
		this.hash = pwHash;
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
