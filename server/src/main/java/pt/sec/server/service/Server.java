package pt.sec.server.service;

import pt.sec.server.domain.PasswordManager;

public class Server{
	
	private PasswordManager passwordManager;
	
	public Server(String mysqlId, String mysqlPassword) {
		this.setPasswordManager(new PasswordManager(mysqlId, mysqlPassword));
	}
    
    //TODO Method to implement from interface
	//Marcal
    public void register(){}
    
    //TODO Method to implement from interface
    //Tiago
    public void put(){}
    
    //TODO Method to implement from interface
    //Daniel
    public byte[] get(){
    	return null;
    }

	public PasswordManager getPasswordManager() {
		return passwordManager;
	}

	public void setPasswordManager(PasswordManager passwordManager) {
		this.passwordManager = passwordManager;
	}    	
	
}