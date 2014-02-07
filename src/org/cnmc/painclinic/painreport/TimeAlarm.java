package org.cnmc.painclinic.painreport;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class TimeAlarm extends BroadcastReceiver 
{
	//final public static String ONE_TIME = "onetime";
    private DateFormat __df = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss z");

    // So the alarm is not picking up the set Shared Prefs, probably because the
    // app is not actually in memory. So we have to have this object actually
    // init the nextReadingService URL itself from the prefs
    private String __serverURL;
    private String __userPIN;

    private String __getNextReadingURL(Context context) {
        // get the server URL and the user PIN from Preferences
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        __serverURL = prefs.getString("prefServer", "No_SERVER_for_alarm");
        __userPIN   = prefs.getString("prefPIN", "No_PIN_for_alarm");

        return "http://" + __serverURL +  "/painreport/primport?PIN="+__userPIN;
    }

	@Override
	public void onReceive(Context context, Intent intent) 
	{
        if (PainReportHelper.pastReadingTime(__getNextReadingURL(context))) {
            // the notification time has passed, display the notification
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
            wl.acquire();

            // see http://stackoverflow.com/questions/2727763/communication-between-android-java-and-phonegap-javascript
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

            wl.release();
        } else {
            Log.d("TimeAlarm", "Notification time is not available or in the future");
        }
	}

	public void SetAlarm(Context context)
	{
		AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, TimeAlarm.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+MainActivity.getAlarmInterval(),
                               MainActivity.getAlarmInterval(), pi); // ms*sec*min=1hr
	}

	public void CancelAlarm(Context context)
	{
		Intent intent = new Intent(context, TimeAlarm.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}
}

