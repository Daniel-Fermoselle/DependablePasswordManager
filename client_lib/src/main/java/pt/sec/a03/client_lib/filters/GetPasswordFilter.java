package pt.sec.a03.client_lib.filters;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import pt.sec.a03.common_classes.CommonTriplet;

public class GetPasswordFilter implements ClientRequestFilter {

	public GetPasswordFilter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {

		CommonTriplet t = (CommonTriplet) requestContext.getEntity();
		System.out.println(t.getPassword());
		System.out.println("TEST");
		// make signature
		// requestContext.getHeaders().add("Signature", "actual signature");

	}

}
