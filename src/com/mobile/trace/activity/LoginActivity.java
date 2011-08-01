package com.mobile.trace.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.mobile.trace.R;
import com.mobile.trace.model.DeviceLoadModel;
import com.mobile.trace.utils.Config;
import com.mobile.trace.utils.SettingManager;

public class LoginActivity extends Activity {

    private EditText mEditText;
    private LoginTask mLoginTask;
    private ProgressDialog mDialog;
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case Config.DEVICE_LOAD:
                mDialog.dismiss();
                int result = (Integer) msg.obj;
                if (result == 1) {
                    Intent nextIntent = new Intent();
                    nextIntent.setClass(LoginActivity.this, MapViewActivity.class);
                    startActivity(nextIntent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        this.setContentView(R.layout.login);
        
        mEditText = (EditText) findViewById(R.id.phone_editor);
        
        View login = findViewById(R.id.do_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = mEditText.getEditableText().toString();
                if (phone.length() != 11) {
                    Toast.makeText(LoginActivity.this
                            , getString(R.string.tips_phone_number)
                            , Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (!TextUtils.isEmpty(mEditText.getEditableText().toString())) {
                    SettingManager.getInstance().setLoginPhone(phone);
                    
                    if (mDialog != null) {
                        mDialog.show();
                    }
                    mLoginTask = new LoginTask();
                    mLoginTask.execute("");
                } else {
                    Toast.makeText(LoginActivity.this
                            , LoginActivity.this.getString(R.string.empty_phone_bumber)
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        initProgressDialog();
        DeviceLoadModel.getInstance().getDeviceLoadObserver().addObserver(mHandler);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        DeviceLoadModel.getInstance().getDeviceLoadObserver().removeObserver(mHandler);
    }

    private void initProgressDialog() {
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.login_progress));
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }
    
    private class LoginTask extends AsyncTask<String, Void, Integer> {
        protected Integer doInBackground(String...params) {
            try {
                DeviceLoadModel.getInstance().getDeviceInfo();
            } catch (Exception e) {
            }
            
            return 0;
        }
    }
}
