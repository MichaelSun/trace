package com.mobile.trace.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mobile.trace.R;

public class SettingManager {

    private static final String TAG = "SettingManager";
    
    private static SettingManager gSettingManager;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    
    public static SettingManager getInstance() {
        if (gSettingManager == null) {
            gSettingManager = new SettingManager();
        }
        return gSettingManager;
    }
    
    public void init(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEditor = mSharedPreferences.edit();
    }
    
    public void setLoginPhone(String phone) {
        mEditor.putString(mContext.getString(R.string.login_phone), phone);
        mEditor.commit();
    }
    
    public String getLoginphone() {
        return mSharedPreferences.getString(mContext.getString(R.string.login_phone), null);
    }
    
    private SettingManager() {
        
    }
    
}
