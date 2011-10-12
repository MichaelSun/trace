package com.mobile.trace.activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mobile.trace.R;
import com.mobile.trace.utils.Config;
import com.mobile.trace.utils.SettingManager;


public class SettingActivity extends PreferenceActivity {
    private static final String TAG = "SettingActivity";
    
    private ListPreference mRatePreference;
    private EditTextPreference mIpPreference;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.addPreferencesFromResource(R.xml.setting);
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mRatePreference = (ListPreference) findPreference(getString(R.string.pref_refresh_rate));
        mRatePreference.setSummary(String.valueOf(SettingManager.getInstance().getRefreshRate()) + "分钟");
        
        mIpPreference = (EditTextPreference) findPreference(getResources().getString(R.string.pref_server_ip));
        mIpPreference.setSummary(SettingManager.getInstance().getServerIP());

        
        mRatePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean  onPreferenceChange(Preference preference, Object newValue) {
                LOGD("[[onPreferenceChange]] new value = " + newValue.toString());
                mRatePreference.setSummary(((String) newValue) + "分钟");
                return true;
            }
        });
        
        mIpPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean  onPreferenceChange(Preference preference, Object newValue) {
                LOGD("[[onPreferenceChange]] new value = " + newValue.toString());
                mIpPreference.setSummary(newValue.toString());
                return true;
            }
        });
    }
    
    private static void LOGD(String msg) {
        if (Config.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
