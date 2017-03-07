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

                System.out.print("Password Maneger initiated: ");
                String input = br.readLine();

                if ("Exit".equals(input)) {
                    System.out.println("CY@!");
                    System.exit(0);
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