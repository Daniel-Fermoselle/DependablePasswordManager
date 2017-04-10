package pt.sec.a03.server;

import java.io.Closeable;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.simple.container.SimpleServerFactory;

public class MyApplication{
	
	public static String PORT;


	public static void main(String[] args) throws Exception{
		Closeable server = null;
	    try {
	    	if (args.length != 1) { System.out.println("Invalid number of arguments");return; }
			PORT = args[0];

	    	ResourceConfig rc = new PackagesResourceConfig("pt.sec.a03.server");
	        server = SimpleServerFactory.create("http://" + args[0], rc);

	        System.out.println("Server running on " + args[0] + "...");
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
