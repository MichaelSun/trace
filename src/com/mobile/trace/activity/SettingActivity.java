package com.mobile.trace.activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mobile.trace.R;
import com.mobile.trace.utils.Config;


public class SettingActivity extends PreferenceActivity {
    private static final String TAG = "SettingActivity";
    String strRefreshRate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.addPreferencesFromResource(R.xml.setting);
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadPreferences(preferences);
        this.findPreference("refresh_rate").setSummary(strRefreshRate + "分钟");
        this.findPreference("refresh_rate").setDefaultValue(strRefreshRate);

        preferences.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("refresh_rate")) {
                    strRefreshRate = sharedPreferences.getString("refresh_rate", "No preferences");

                    findPreference("refresh_rate").setSummary(strRefreshRate + "分钟");
                    saveRefreshRate(sharedPreferences, strRefreshRate);
                } else if (key.equals("server_ip")) {
                    String data = sharedPreferences.getString("server_ip", "");
                    findPreference("server_ip").setSummary(data);
                }
            }
        });

    }
    
    private static void LOGD(String msg) {
        if (Config.DEBUG) {
            Log.d(TAG, msg);
        }
    }
    
	public void saveRefreshRate(SharedPreferences sharedPreferences, String strRefreshRate) {
        Editor editor = sharedPreferences.edit(); 
        editor.putString("@string/refresh_rate", strRefreshRate);
        editor.commit();
    }
	
	public String loadPreferences(SharedPreferences sharedPreferences) {
		strRefreshRate = sharedPreferences.getString("@string/refresh_rate", "1");
		return strRefreshRate;
	}

}
