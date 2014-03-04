package org.cnmc.painclinic.painreport;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author Sunil
 *
 */
public class PainReportService extends Service{
	private static final int THREAD_SIZE = 2;
	private  long periodic_delay = 180000;
	private volatile ScheduledExecutorService scheduledThreadPool = null;	
    private String __serverURL;
    private String __userPIN;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method st
		return null;
	}
	
	private String __getNextReadingURL(Context context) {
		// get the server URL and the user PIN from Preferences
		SharedPreferences prefs =
			PreferenceManager.getDefaultSharedPreferences(context);
		__serverURL = prefs.getString("prefServer", "No_SERVER_for_alarm");
		__userPIN   = prefs.getString("prefPIN", "No_PIN_for_alarm");
		Log.d("PainReportService","__serverURL and __userPIN got is :" + __userPIN + __serverURL);
		return "http://" + __serverURL +  "/painreport/primport?PIN="+__userPIN;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){				
		return START_NOT_STICKY;
	}
	
	public void notifyUsers(){
		if (PainReportHelper.pastReadingTime(__getNextReadingURL(this))) {
			Context context = getApplicationContext();
			NotificationManager notManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification(R.drawable.ic_launcher, "Pain report is now due", System.currentTimeMillis());
			// How do I know if 1) the app is not running (visible), 2) the webView Activity is onscreen, or 3) the MainActivity is?
			Intent notificationIntent = new Intent(context, MainActivity.class);
			//Intent notificationIntent = PainReportHelper.getPainReportWebIntent();

			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

			PendingIntent pIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			notification.setLatestEventInfo(context, "Pain Report", "Pain Report Due!", pIntent);		
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notManager.notify(0, notification);
		} else {
			Log.d("PainReportService", "Notification time is not available or in the future");
		}

		Log.d("PainReportService", "Inside notifyUsers");
	}
	
	 @Override
	 public void onCreate() {
		 if(scheduledThreadPool!=null)
			 scheduledThreadPool = null;

		 scheduledThreadPool = Executors.newScheduledThreadPool(THREAD_SIZE);	
		 Log.d("PainReportService", "Inside onCreate");
		 // Read our props
		 Resources resources = this.getResources();
		 AssetManager assetManager = resources.getAssets();		
		 // Read from the /assets directory
		 try {
			 InputStream inputStream = assetManager.open("cnmcpr.properties");
			 Properties __properties = new Properties();
			 __properties.load(inputStream);
			 Log.d("PainReportService", "properties are now loaded");
			 periodic_delay = Long.parseLong(__properties.getProperty("cnmcpr.alarminterval", "604800000"));
			 Log.d("PainReportService", "periodic_delay val is " + periodic_delay);	            

			 if(!scheduledThreadPool.isShutdown()){
				 scheduledThreadPool.scheduleWithFixedDelay(new Runnable() {
					 public void run() {
						 notifyUsers(); 
					 }
				 }, periodic_delay,periodic_delay, TimeUnit.MILLISECONDS);
			 }

		 } catch (IOException e) {
			 Log.d("PainReportService", "Failed to open cnmcpr.properties file");
		 }catch (RejectedExecutionException e){
			 if(!scheduledThreadPool.isShutdown()){
				 Log.d("PainReportService","Task Submission Rejected",e);
			 }
		 }catch (Exception e){
			 Log.d("PainReportService","Task Submission failed",e);
		 }		  

	 }
	 
	 @Override
	 public void onDestroy(){
		 if(!scheduledThreadPool.isShutdown()) {			
			 try {
				 scheduledThreadPool.shutdownNow();
				 scheduledThreadPool.awaitTermination(200, TimeUnit.MILLISECONDS);
			 } catch (InterruptedException e) {
				 // TODO Auto-generated catch block
				 Log.d("PainReportService","Executor Service Termination exception",e);
			 }finally{
				 scheduledThreadPool = null;
			 }

			 super.onDestroy();
		 }
		 Log.d("PainReportService", "Inside Destroy");
	 }
	

}
