package com.njtransit.departurevision;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 2/14/11
 * Time: 11:50 PM
 */
public class DepartureVision {


    private static String nj = "http://technically.us:7979/";

    private HttpClient client;

    public DepartureVision() {
        client = new DefaultHttpClient();
    }

    public InputStream departures(String id) throws IOException {
    	URL u = new URL("http://technically.us:7979/"+id.toLowerCase());
    	try {
    		InputStream in = u.openStream();
    		return in;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
}
