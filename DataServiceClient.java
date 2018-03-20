package org.vishal.webservice.client;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class DataServiceClient {
	
	static final String Rest_URI="http://localhost:7777/JAXRSJsonExample/rest/countries";
	
	public static void main(String args[]){
		
		ClientConfig config=new DefaultClientConfig();
		Client client= Client.create(config);
		WebResource service=client.resource(Rest_URI);
		
		System.out.println("JSON Data status : "+getResponse(service));
		System.out.println("Data is : "+getOutputAsJson(service));
		
	}
	
	private static String getResponse(WebResource service){
		return service.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class).toString();
	}
	
	private static String getOutputAsJson(WebResource service){
		return service.accept(MediaType.APPLICATION_JSON).get(String.class).toString();
	}
}
