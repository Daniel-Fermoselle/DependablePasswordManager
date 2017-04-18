package pt.sec.a03.server;

import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class MyApplication{
	
	public static String PORT;

	public static void main(String[] args) throws Exception{
		HttpServer server = null;
	    try {
	    	if (args.length != 1) { System.out.println("Invalid number of arguments");return; }
			PORT = args[0];

			server = startServer("http://" + args[0] + "/PwServer");

	        System.out.println("Server running on " + args[0] + "...");
	        System.out.println("Press any key to stop the service...");
	        System.in.read();
	    } finally {
	        try {
	            if (server != null) {
	                server.stop();
	            }
	        } finally {

	        }
	    }
	}
	
    public static HttpServer startServer(String uri) {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example.rest package
        final ResourceConfig rc = new ResourceConfig().packages("pt.sec.a03.server");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(uri), rc);
    }

    /*public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.stop();
    }*/
}
