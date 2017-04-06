package pt.sec.a03.server;

import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

public class MyApplication{
	public static void main(String[] args) throws Exception{
	    HttpServer server = null;
	    try {
	    	ResourceConfig rc = new PackagesResourceConfig("pt.sec.a03.server");
	        server = GrizzlyServerFactory.createHttpServer("http://localhost:5555", rc);
	        System.out.println("Press any key to stop the service...");
	        System.in.read();
	    } finally {
	        try {
	            if (server != null) {
	                server.stop();
	            }
	        } finally {
	            ;
	        }
	    }
	}
}
