package pt.sec.server;

public class Server{
	
	private String mysqlId;
	private String mysqlPassword;
	
    public Server(String mysqlId, String mysqlPassword) throws Exception {
    	this.mysqlId = mysqlId;
    	this.mysqlPassword = mysqlPassword;
    }        
	
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