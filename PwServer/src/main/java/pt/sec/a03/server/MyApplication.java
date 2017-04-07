package pt.sec.a03.server;

import java.io.Closeable;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.simple.container.SimpleServerFactory;

public class MyApplication{
	
	private static final String DATABASE_URI = "jdbc:mysql://localhost:3306/%s?useSSL=false";
	
	public static void main(String[] args) throws Exception{
		Closeable server = null;
	    try {
	    	String databaseURI = String.format(DATABASE_URI, args[1]);
			Files.write(Paths.get("metadata.in"), databaseURI.getBytes());
			
	    	ResourceConfig rc = new PackagesResourceConfig("pt.sec.a03.server");
	        server = SimpleServerFactory.create("http://" + args[0], rc);
	        System.out.println("Server running on " + args[0] + "...");
	        System.out.println("Database on URI " + databaseURI + "...");
	        System.out.println("Press any key to stop the service...");
	        System.in.read();
	    } finally {
	        try {
	            if (server != null) {
	                server.close();
	            }
	        } finally {
	            ;
	        }
	    }
	}
}
