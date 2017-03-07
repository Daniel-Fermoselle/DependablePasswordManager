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
                System.out.println("Chose one of the following: create_user, save_password, get_password");
                String input = br.readLine();
                
                if ("Exit".equals(input)) {
                    System.out.println("CY@!");
                    System.exit(0);
                }
                else if("create_user".equals(input)) {
                	
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