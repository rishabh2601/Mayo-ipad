package org.cnmc.painclinic.painreport;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class PainReportHelper {

    /*
    static Intent getPainReportWebIntent() {
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        notificationIntent.setData(Uri.parse(MainActivity.getDeliveryURL()));
        return notificationIntent;
    }
    */

    public static boolean pastReadingTime(String serviceURL) {
        try {
            String nrString = PainReportHelper.getNextReading(serviceURL);
            if (nrString != null) {
                // The String returned now has format <long ms> <String readable date in server TZ>
                String[] rval = nrString.trim().split(" ", 2);
                try {
                    long nrMilliseconds = Long.parseLong(rval[0]);
                    Log.d("MainActivity", "\tnotification time " + nrMilliseconds + "\tSystem " + System.currentTimeMillis());
                    return (nrMilliseconds < System.currentTimeMillis());
                } catch (Exception exc) {
                    Log.d("MainActivity", "Unable to convert " + rval[0] + " to ms, returned from server " + nrString);
                    return false;
                }
            } else {
                Log.d("MainActivity", "Did not get back a valid string for nextReadingTime");
                return false;
            }
        } catch (Exception exc) {
            return false;
        }
    }

	// from http://stackoverflow.com/questions/13705494/android-http-get-request
	public static String getNextReading(String serviceURL) throws Exception {
	    BufferedReader in = null;
	    String data = null;

        if (serviceURL.contains(MainActivity.__NO_SERVER) || serviceURL.contains(MainActivity.__NO_PIN)) {
            return null;
        }

	    try {
	        HttpClient client = new DefaultHttpClient();
	        //client.getConnectionManager().getSchemeRegistry().register(getMockedScheme());

	        URI website = new URI(serviceURL);
            Log.d("PainReportHelper", "Requesting next reading date from service " + serviceURL);
            HttpGet request = new HttpGet();
	        request.setURI(website);
	        HttpResponse response = client.execute(request);
	        response.getStatusLine().getStatusCode();

	        in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	        StringBuffer sb = new StringBuffer("");
	        String l = "";
	        while ((l = in.readLine()) != null) {
                Log.d("PainReportHelper", "Read line from server: " + l);
                if (!l.trim().isEmpty()) {
    	            sb.append(" " + l);
                }
	        }
	        in.close();
	        data = sb.toString();
	        return data;
        } catch (Throwable t) {
            Log.e("PainReportHelper", "EXCEPTION: Unable to get next reading time from server", t);
            throw new Exception (t);
	    } finally {
	        if (in != null) {
	            try {
	                in.close();
	                return data;
	            } catch (Exception e) {
	                Log.e("PainReportHelper", e.getMessage());
	            }
	        }
	    }
	}
}
