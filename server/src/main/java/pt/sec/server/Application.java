package pt.sec.server;

import java.io.Console;

import pt.sec.server.service.Server;

public class Application {

	public static void main(String args[]){
		try {
			Server server = getServerDatabase();
		} 
		
		catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	private static Server getServerDatabase() throws Exception{
		Console console = System.console();
		if (console == null) {
			System.out.println("Couldn't get Console instance");
			System.exit(0);
		}
		console.printf("Insert your mysql id: ");
		String id = console.readLine();
		console.printf("Insert your mysql password: ");
    	char[] passwordChars = console.readPassword();
    	String passwordString = new String(passwordChars);
    	
    	return new Server(id, passwordString);
	}
    
}