package org.cnmc.painclinic.painreport;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.cnmc.painclinic.event.NotificationLoggerListener;
import org.cnmc.painclinic.event.PainReportEvent;
import org.cnmc.painclinic.event.PainReportEventDispatcher;

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
import android.service.notification.NotificationListenerService;
import android.util.Log;

/**
 * @author Sunil
 *
 */
public class PainReportService extends Service{
	private static final int THREAD_SIZE = 2;
	private  long periodic_delay = 180000;
	private volatile ScheduledExecutorService scheduledThreadPool = null;	
	private static String serverURL;
	private static String submitURL;


	private static String userPIN;

	private NotificationLoggerListener notifyLogger = null;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private String __getNextReadingURL(Context context) {
		// get the server URL and the user PIN from Preferences
		SharedPreferences prefs =
			PreferenceManager.getDefaultSharedPreferences(context);
		serverURL = prefs.getString("prefServer", "No_SERVER_for_alarm");
		userPIN   = prefs.getString("prefPIN", "No_PIN_for_alarm");
		Log.d("PainReportService","__serverURL and __userPIN got is :" + userPIN + serverURL);
		return "http://" + serverURL +  "/painreport/primport?PIN="+userPIN;
	}
	
	 public static String getSubmitURL() {
	        return "http://" + serverURL   + "/painreport/" + submitURL;
	    }



	@Override
	public int onStartCommand(Intent intent, int flags, int startId){				
		return START_NOT_STICKY;
	}

	public void notifyUsers(){
		String contructURL = __getNextReadingURL(this);
		if (PainReportHelper.pastReadingTime(contructURL)) {
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
		/*try {			
			long patientId = Long.parseLong(getUserPIN());
			PainReportEvent notifyEvent = new PainReportEvent(PainReportEvent.EVENT_NOTIFIED,System.currentTimeMillis(), new Date(System.currentTimeMillis()),patientId);
			notifyEvent.setDescription("Pain Report notified to " + patientId);
			PainReportEventDispatcher.getInstance().dispatchEvent(notifyEvent);
			Log.d("PainReportService", "Inside notifyUsers and notify logger");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			Log.d("PainReportService","Error in Notify logger "+ e.getMessage());
		}catch(Exception e){
			Log.d("PainReportService","Error in Notify logger "+ e.getMessage());
		}*/
	}

	@Override
	public void onCreate() {
		if(scheduledThreadPool!=null)
			scheduledThreadPool = null;

		scheduledThreadPool = Executors.newScheduledThreadPool(THREAD_SIZE);	
		Log.d("PainReportService", "Inside onCreate");

		notifyLogger = new NotificationLoggerListener(this);

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
			submitURL = __properties.getProperty("cnmcpr.submiturl");
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

			if (notifyLogger!=null){
				notifyLogger.destroy();
			}
			PainReportEventDispatcher.getInstance().destroy();

			super.onDestroy();
		}
		Log.d("PainReportService", "Inside Destroy");
	}
	
	public static String getServerURL() {
		return serverURL;
	}

	public static void setServerURL(String serverURL) {
		PainReportService.serverURL = serverURL;
	}

	public static String getUserPIN() {
		return userPIN;
	}

	public static void setUserPIN(String userPIN) {
		PainReportService.userPIN = userPIN;
	}


	


}
