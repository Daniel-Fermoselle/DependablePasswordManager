package pt.sec.client;

import java.io.Console;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.sec.a03.client_lib.ClientLib;

public class ClientApplication {

	public static void main(String args[]){
		
		ClientLib cl = new ClientLib();
			Console console = System.console();
			if (console == null) {
				System.out.println("Couldn't get Console instance");
				System.exit(0);
			}
			console.printf("Password Manager initiated:\n");

            while (true) {
            	
            	console.printf("Chose one of the following:\n\t-register_user\n\t-save_password\n\t-retrieve_password\n=>");
                String input = console.readLine();

                
                if ("Exit".equals(input)) {
                    console.printf("CY@!\n");
                    System.exit(0);
                }
                else if("register_user".equals(input)) {
                	console.printf("Registering....\n");
                	cl.register_user();
                	console.printf("User registered successfully....\n");
                }
                else if("save_password".equals(input)) {
                	String domain = readNormal(console, "Domain: ");
                	String username = readNormal(console, "Username: ");
                	String password = readNormal(console, "Password: ");
                    cl.save_password(domain, username, password);
                }
                else if("retrieve_password".equals(input)) {
                	String domain = readNormal(console, "Domain: ");
                	String username = readNormal(console, "Username: ");
                	console.printf("The password is: " + cl.retrive_password(domain, username) + "\n");
                }
                else {
                    console.printf("Try again! ^_^\n");
                }
            }

        
    }
	
	public static String readNormal(Console console, String msg){
		console.printf(msg);
		String string = console.readLine();
		Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
    	Matcher m = p.matcher(string);
    	boolean b = m.find();
		while(b){
			System.out.println("No letters or special char alowed. Try again!");
			console.printf(msg);
			string = console.readLine();
			m = p.matcher(string);
	    	b = m.find();
		}
		return string;
	}
	
	public static String readPassword(Console console, String msg){
		console.printf(msg);
    	char[] passwordChars = console.readPassword();
    	String passwordString = new String(passwordChars);
    	Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
    	Matcher m = p.matcher(passwordString);
    	boolean b = m.find();
		while(b){
			System.out.println("No letters or special char alowed. Try again!");
			console.printf(msg);
			passwordChars = console.readPassword();
	    	passwordString = new String(passwordChars);
	    	m = p.matcher(passwordString);
	    	b = m.find();
		}
		return passwordString;
	}
    
}