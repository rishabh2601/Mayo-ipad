package org.cnmc.painclinic.painreport;

import java.io.IOException;
import java.io.InputStream;
import java.io.WriteAbortedException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
    private static final int RESULT_SETTINGS = 1;
    private static final int WEBVIEW_RESULT  = 2;
    public  static final String __NO_SERVER = "NO SERVER";
    public  static final String __NO_PIN = "NO PIN";

	private static TimeAlarm __timeAlarm;
	private static String __serverURL = "";
    private static String __deliveryURL = "";
    private static String __userPIN   = "";
    //private static String __unlockPIN = "2662";  // cnmc
    //private static Date __nextReading = null;
    private static String __nextReadingService = "";
    private static String __submitURL = "";
    private static long __alarmInterval = 1000L;
    private static String __timezone = "MST"; // timezone of server, not app
    private Properties __properties = null;


    public static long getAlarmInterval() {
        return __alarmInterval;
    }
    public static String getServerURL() {
        if (__serverURL.equals(__NO_SERVER)) return __serverURL;
        return "http://" + __serverURL + "/painreport/";
    }
    public static String getDeliveryURL() {
        return __deliveryURL; // + "?PIN=" + __userPIN + "&server=" + getSubmitURL();
    }
    public static String getNextReadingService() {
        return getServerURL() + __nextReadingService + "?PIN="+__userPIN;
    }
    public static String getUserPIN() {
        return __userPIN;
    }
    public static String getSubmitURL() {
        return getServerURL() + __submitURL;
    }

	// Button event handler
	public void runApp(View view) {
        // check if the server is properly set
        if (__serverURL.equals(__NO_SERVER) || __userPIN.equals(__NO_PIN)) {
            // create an Android dialog alert here
            __createSimpleAlert("Incomplete Settings", "PIN and Server not set");
        } else if (!PainReportHelper.pastReadingTime(MainActivity.getNextReadingService())) {
            // create an alert saying there is no reading due
           __createSimpleAlert("Pain Report", "Next Reading Not Yet Due!");
        } else {
            // OK start the app!
            __jumpToApp();
        }
	}



    private void __createSimpleAlert(String title, String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
            }
        });
        alertDialog.show();
    }

    private void __jumpToApp() {
        Intent intent = new Intent(this,Browselocal.class);
        startActivityForResult(intent, WEBVIEW_RESULT);
    }

    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //Context context = this.getApplicationContext();
        Log.d("MainActivity", "LIFECYCLE STATE: in onCreate after super");

        // Read our props
        Resources resources = this.getResources();
        AssetManager assetManager = resources.getAssets();

        // Read from the /assets directory
        try {
            InputStream inputStream = assetManager.open("cnmcpr.properties");
            __properties = new Properties();
            __properties.load(inputStream);
            Log.d("Main", "The properties are now loaded");
            __alarmInterval = Long.parseLong(__properties.getProperty("cnmcpr.alarminterval", "604800000"));
            __nextReadingService = __properties.getProperty("cnmcpr.nextReadingUrl", "primport");
            __deliveryURL = __properties.getProperty("cnmcpr.deliveryurl");
            __submitURL = __properties.getProperty("cnmcpr.submiturl");
        } catch (IOException e) {
            Log.d("Main", "Failed to open cnmcpr.properties file");
        }

        // get the server URL and the user PIN from Preferences
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        __serverURL = prefs.getString("prefServer", __NO_SERVER);
        __userPIN   = prefs.getString("prefPIN", __NO_PIN);

        // Sets the content view to be our native app
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.main);
        showUserSettings();

        // This is needed to prevent network errors after Gingerbread
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // now turn on the alarm
        makeAlarm();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_settings:
                Intent i = new Intent(this, PainReportSettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;

        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                showUserSettings();
                makeAlarm();  // we make an alarm after completing settings in case they changed.
                break;
            case WEBVIEW_RESULT:
                finish();
                //killApp(true);
                break;
        }
    }

    private void makeAlarm() {
        Context context = this.getApplicationContext();
        // if there was an alarm cancel it - we do in case settings change
        if (__timeAlarm != null) {
            __timeAlarm.CancelAlarm(context);  // cancel any existing first
        }

        // setup the alarm
        __timeAlarm = new TimeAlarm();
        if (__timeAlarm != null) {
            // strange to see 2 same ifs but if constructor fails this is what docs say
            __timeAlarm.SetAlarm(context);
        } else {
            Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
        }
    }
    private void showUserSettings() {
        // get the server URL and the user PIN from Preferences
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        __serverURL = prefs.getString("prefServer", __NO_SERVER);
        __userPIN   = prefs.getString("prefPIN", __NO_PIN);

        // Find our UI elements and set the defaults
        TextView tvServer     = (TextView)findViewById(R.id.textServer);
        TextView tvUserPIN    = (TextView)findViewById(R.id.textPIN);
        if (tvServer != null) tvServer.setText(getServerURL());
        if (tvUserPIN != null) tvUserPIN.setText(getUserPIN());
    }

    @Override
	protected void onStart() {
		super.onStart();
        Log.d("MainActivity", "LIFECYCLE STATE: in onStart after super");
	}

    public static void killApp(boolean killSafely) {
        if (killSafely) {
            /*
             * Notify the system to finalize and collect all objects of the app
             * on exit so that the virtual machine running the app can be killed
             * by the system without causing issues. NOTE: If this is set to
             * true then the virtual machine will not be killed until all of its
             * threads have closed.
             */
            System.runFinalizersOnExit(true);

            /*
             * Force the system to close the app down completely instead of
             * retaining it in the background. The virtual machine that runs the
             * app will be killed. The app will be completely created as a new
             * app in a new virtual machine running in a new process if the user
             * starts the app again.
             */
            System.exit(0);
        } else {
            /*
             * Alternatively the process that runs the virtual machine could be
             * abruptly killed. This is the quickest way to remove the app from
             * the device but it could cause problems since resources will not
             * be finalized first. For example, all threads running under the
             * process will be abruptly killed when the process is abruptly
             * killed. If one of those threads was making multiple related
             * changes to the database, then it may have committed some of those
             * changes but not all of those changes when it was abruptly killed.
             */
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}