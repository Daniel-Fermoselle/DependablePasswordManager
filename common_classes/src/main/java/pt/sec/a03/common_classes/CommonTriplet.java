package pt.sec.a03.common_classes;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class CommonTriplet {

	public CommonTriplet() {
	}

	private String password;
	private String username;
	private String domain;

	public CommonTriplet(String password, String username, String domain) {
		super();
		this.password = password;
		this.username = username;
		this.domain = domain;
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
