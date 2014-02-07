package org.cnmc.painclinic.painreport;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

/**
 * Created by kevinagary on 11/4/13.
 */
public class PainReportSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals("prefPIN") || key.equals("prefServer")) {
            Preference pref = findPreference(key);
            pref.setSummary(prefs.getString(key, ""));
        }
    }
}
