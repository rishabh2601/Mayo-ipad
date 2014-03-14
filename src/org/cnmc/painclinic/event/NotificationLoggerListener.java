package org.cnmc.painclinic.event;

import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.cnmc.painclinic.painreport.PainReportService;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

public class NotificationLoggerListener implements PainReportListener {
	
	private Context ctxt;
	
	public NotificationLoggerListener(Context ctx){
		this.ctxt = ctx;
		init();
	}
	
	public void init(){
		PainReportEventDispatcher.getInstance().addListener(this);
		Log.d("NotificationLoggerListener", "Inside init");
	}

	public void destroy(){
		PainReportEventDispatcher.getInstance().removeListener(this);
	}

	public void OnEventCreated(PainReportEvent event) {
		
	}

	public void onEventDeleted(PainReportEvent event) {
		
	}

	public void onEventModified(PainReportEvent event) {
		
	}
	
	public void onEventNotified(PainReportEvent event){
		//TODO send JSON to Server
		
		String json =  new Gson().toJson(event);
		Log.d("NotificationLoggerListener", "Inside onEventNotified - val of json is : "+ json);
		PainReportService pService = (PainReportService)ctxt;
		String uri = "http://" + pService.getServerURL() +  "/AQMEcho/aqmecho";
		
		 try {
		        HttpPost httpPost = new HttpPost(uri);
		        httpPost.setEntity(new StringEntity(json));
		        httpPost.setHeader("Accept", "application/json");
		        httpPost.setHeader("Content-type", "application/json");
		        
		        HttpParams myParams = new BasicHttpParams();
		        myParams.setParameter("jason", json);
		        HttpConnectionParams.setConnectionTimeout(myParams, 10000);
		        HttpConnectionParams.setSoTimeout(myParams, 10000);
		        
		        HttpClient httpclient = new DefaultHttpClient(myParams);
		        
		        httpclient.execute(httpPost);
		    } catch (UnsupportedEncodingException e) {
		    	Log.d("NotificationLoggerListener","Error in onEventNotified "+ e.getMessage());
		    } catch (ClientProtocolException e) {
		    	Log.d("NotificationLoggerListener","Error in onEventNotified "+ e.getMessage());
		    } catch (Exception e) {
		    	Log.d("NotificationLoggerListener","Error in onEventNotified "+ e.getMessage());
		    }
		   
		
	}


}

