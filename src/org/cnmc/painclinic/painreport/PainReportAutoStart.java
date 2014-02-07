package org.cnmc.painclinic.painreport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PainReportAutoStart extends BroadcastReceiver {

	TimeAlarm alarm = new TimeAlarm();
	@Override
	public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.SetAlarm(context);
        }
	}
}
