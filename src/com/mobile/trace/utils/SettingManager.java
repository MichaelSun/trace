package com.mobile.trace.utils;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mobile.trace.R;
import com.mobile.trace.activity.WarningRegionOverlay.WarningRegion;

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
    
    public void clearPhone() {
        mEditor.remove(mContext.getString(R.string.login_phone));
        mEditor.commit();
    }
    
    public void setLoginPhone(String phone) {
        mEditor.putString(mContext.getString(R.string.login_phone), phone);
        mEditor.commit();
    }
    
    public String getLoginphone() {
        return mSharedPreferences.getString(mContext.getString(R.string.login_phone), null);
    }
    
    public void saveWarningRegion(ArrayList<WarningRegion> warningList) {
        StringBuilder saver = new StringBuilder();
        for (WarningRegion region : warningList) {
            saver.append(region.makeSaveString()).append(Config.WARNING_INFO_SPLIT);
        }
        mEditor.putString(mContext.getString(R.string.pref_warning)
                , saver.substring(0, saver.length() - 1));
        mEditor.commit();
    }
    
    public void loadWarningRegion(ArrayList<WarningRegion> warningList) {
        String infos = mSharedPreferences.getString(mContext.getString(R.string.pref_warning), null);
        if (infos != null) {
            String[] regions = infos.split(Config.WARNING_INFO_SPLIT);
            for (String region : regions) {
                WarningRegion r = new WarningRegion(region);
                warningList.add(r);
            }
        }
    }
    
    private SettingManager() {
        
    }
    
}
