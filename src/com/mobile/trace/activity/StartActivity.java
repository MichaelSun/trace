package com.mobile.trace.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;

import com.mobile.trace.R;
import com.mobile.trace.utils.SettingManager;

public class StartActivity extends Activity {
    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAILED = -1;

    private StartTask mStartTask;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.start_activity);
        
        mStartTask = new StartTask();
        mStartTask.execute();
    }
    
    private class StartTask extends AsyncTask<String, Void, Integer> {
        protected Integer doInBackground(String...params) {
            SettingManager.getInstance().init(getApplicationContext());
            SettingManager sm = SettingManager.getInstance();
            
            //test code
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
            }
            
            if (sm.getLoginphone() != null) {
                return LOGIN_SUCCESS;
            } 

            //TODO : should get info from server later
            
            return LOGIN_FAILED;
        }
        
        protected void onPostExecute(Integer result) {
            Intent nextIntent = new Intent();
            switch (result) {
            case LOGIN_FAILED:
                nextIntent.setClass(StartActivity.this, LoginActivity.class);
                break;
            case LOGIN_SUCCESS:
                nextIntent.setClass(StartActivity.this, MapViewActivity.class);
                break;
            }
            startActivity(nextIntent);
            finish();
        }
    }
}
