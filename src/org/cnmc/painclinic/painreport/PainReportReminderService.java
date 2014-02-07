package org.cnmc.painclinic.painreport;

import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.IBinder;

public class PainReportReminderService extends Service {

	TimeAlarm alarm = new TimeAlarm();
	public void onCreate()
	{
		super.onCreate();       
	}

	public void onStart(Context context,Intent intent, int startId)
	{
		alarm.SetAlarm(context);
	}

	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
}
