package com.njtransit.departurevision;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 2/14/11
 * Time: 11:50 PM
 */
public class DepartureVision {


    private String nj = "http://dv.njtransit.com/mobile/tid-mobile.aspx?sid=";

    private HttpClient client;

    public DepartureVision() {
        client = new DefaultHttpClient();
    }

    public Object departures(String id) throws IOException {
        HttpPost post = new HttpPost(nj + id);
        HttpResponse response = client.execute(post);
        return null;
    }

    public static void main(String...args) throws IOException {
        DepartureVision v = new DepartureVision();
        v.departures("NY");
    }


}
