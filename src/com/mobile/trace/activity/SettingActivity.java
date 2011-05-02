package com.mobile.trace.activity;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.mobile.trace.R;


public class SettingActivity extends PreferenceActivity {
    private static final String TAG = "SettingActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.addPreferencesFromResource(R.xml.setting);
    }

}
