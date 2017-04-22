package pt.sec.a03.common_classes;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class CommonTriplet {

	public CommonTriplet() {
	}

	private String password;
	private String username;
	private String domain;
	private String hashPassword;

	public CommonTriplet(String password, String username, String domain, String hashPassword) {
		super();
		this.password = password;
		this.username = username;
		this.domain = domain;
		this.hashPassword = hashPassword;
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

	public String getHashPassword() { return hashPassword; }

	public void setHashPassword(String hashPassword) { this.hashPassword = hashPassword; }
}
