package pt.sec.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import pt.sec.a03.client_lib.ClientLib;

public class ClientApplication {

	public static void main(String args[]){
		BufferedReader br = null;
		
		ClientLib cl = new ClientLib();

        try {

            br = new BufferedReader(new InputStreamReader(System.in));

            while (true) {

                System.out.println("Password Manager initiated: ");
                System.out.println("Chose one of the following: register_user, save_password, get_password, exit");
                String input = br.readLine();
                
                if ("Exit".equals(input)) {
                    System.out.println("CY@!");
                    System.exit(0);
                }
                else if("register_user".equals(input)) {
                	System.out.print("Registering....");
                	cl.register_user();
                	System.out.print("User registered successfully....");
                }
                else if("save_password".equals(input)) {
                	System.out.print("Domain: ");
                    String domain = br.readLine();
                    System.out.print("Username: ");
                    String username = br.readLine();
                    System.out.print("Password: ");
                    String password = br.readLine();
                    cl.save_password(domain, username, password);
                }
                else if("get_password".equals(input)) {
                	
                }
                else {
                    System.out.println("Try again! ^_^");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
}