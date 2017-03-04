package pt.sec.server.domain;

public class PasswordManager {
	
	private String mysqlId;
	private String mysqlPassword;
	
    public PasswordManager(String mysqlId, String mysqlPassword) {
    	this.mysqlId = mysqlId;
    	this.mysqlPassword = mysqlPassword;
    }        
    
    //TODO Method to be used by register(Key publicKey) of the server API
    //Marcal
    public void addUser() {}
    
    //TODO Method to be used by put(Key publicKey,byte[] domain, byte[] password) of the server API
    //Tiago
    public void addTriple() {}
    
    //TODO Method to be used by get(Key publicKey,byte[] domain, byte[] password) of the server API
    //Daniel
    public byte[] getPassword() { return null; }
	
	public String getMysqlId() {
		return mysqlId;
	}
	public void setMysqlId(String mysqlId) {
		this.mysqlId = mysqlId;
	}
	public String getMysqlPassword() {
		return mysqlPassword;
	}
	public void setMysqlPassword(String mysqlPassword) {
		this.mysqlPassword = mysqlPassword;
	}

}
