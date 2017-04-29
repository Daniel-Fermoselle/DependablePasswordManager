package pt.sec.client;

import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.sec.a03.client_lib.ClientLib;
import pt.sec.a03.crypto.Crypto;

public class ClientApplication {

	public static void main(String args[]) throws Exception {
		
		Map<String,String> m = fileParser(args[0]);
		
		ClientLib cl = new ClientLib(m);

		Console console = System.console();
		if (console == null) {
			System.out.println("Couldn't get Console instance");
			System.exit(0);
		}
		console.printf("Password Manager initiated:\n");
		
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		KeyStore ks = Crypto.readKeystoreFile("ks/client1.jks", "insecure".toCharArray());
		cl.init(ks, "client", "insecure");

		while (true) {
			try {
				console.printf(
						"Choose one of the following:\n\t1 - register_user\n\t2 - save_password\n\t3 - retrieve_password\nExit to quit\n=>");
				String input = console.readLine();

				if ("Exit".equals(input)) {
					cl.close();
					console.printf("CY@!\n");
					System.exit(0);
				} else if ("1".equals(input)) {
					console.printf("Registering....\n");
					cl.register_user();
					console.printf("User registered successfully....\n");
				} else if ("2".equals(input)) {
					String domain = readNormal(console, "Domain: ");
					String username = readNormal(console, "Username: ");
					String password = readPassword(console, "Password: ");
					cl.save_password(domain, username, password);
				} else if ("3".equals(input)) {
					String domain = readNormal(console, "Domain: ");
					String username = readNormal(console, "Username: ");
					console.printf("The password is: " + cl.retrieve_password(domain, username) + "\n");
				} else {
					console.printf("Try again! ^_^\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
				console.printf("There was an error with the method you tried to execute try again\n");
			}
		}
	}

	public static String readNormal(Console console, String msg) {
		console.printf(msg);
		String string = console.readLine();
		Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(string);
		boolean b = m.find();
		while (b) {
			System.out.println("No letters or special char alowed. Try again!");
			console.printf(msg);
			string = console.readLine();
			m = p.matcher(string);
			b = m.find();
		}
		return string;
	}

	public static String readPassword(Console console, String msg) {
		console.printf(msg);
		char[] passwordChars = console.readPassword();
		String passwordString = new String(passwordChars);
		Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(passwordString);
		boolean b = m.find();
		while (b) {
			System.out.println("No letters or special char alowed. Try again!");
			console.printf(msg);
			passwordChars = console.readPassword();
			passwordString = new String(passwordChars);
			m = p.matcher(passwordString);
			b = m.find();
		}
		return passwordString;
	}
	
	public static Map<String, String> fileParser(String path) throws IOException{
		String fileString = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
		Map<String,String> m = new HashMap<String, String>();
		String[] serverData = fileString.split("\n");
		
		for(String p : serverData){
			String[] pair = p.split(",");
			m.put(pair[1], pair[0]);
		}
		
		return m;
	}

}