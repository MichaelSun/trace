package com.mobile.trace.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.mobile.trace.database.DatabaseOperator;
import com.mobile.trace.utils.SettingManager;

public class StartActivity extends Activity {
    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAILED = -1;

    private StartTask mStartTask;
    private View mDialogView;
    
    private static final int SHOW_PASSWORD_DIALOG = 0;
    private static final int START_LOGIN = 1;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SHOW_PASSWORD_DIALOG:
                showPasswordDialog();
                break;
            case START_LOGIN:
                Intent nextIntent = new Intent();
                if (SettingManager.getInstance().getLoginphone() != null) {
                    nextIntent.setClass(StartActivity.this, MapViewActivity.class);
                } else {
                    nextIntent.setClass(StartActivity.this, LoginActivity.class);
                }
                startActivity(nextIntent);
                finish();
                break;
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.start_activity);
        
        mStartTask = new StartTask();
        mStartTask.execute();
    }
    
    private void showPasswordDialog() {
        mDialogView = View.inflate(this, R.layout.login_passwd_dialog, null);
        String passwd = SettingManager.getInstance().getLoginPassword();
        if (passwd != null) {
            mDialogView.findViewById(R.id.passwd_confirm).setVisibility(View.GONE);
            mDialogView.findViewById(R.id.passwd_confirm_edit).setVisibility(View.GONE);
        } else {
            mDialogView.findViewById(R.id.passwd_confirm).setVisibility(View.VISIBLE);
            mDialogView.findViewById(R.id.passwd_confirm_edit).setVisibility(View.VISIBLE); 
        }
        AlertDialog diaog = new AlertDialog.Builder(this)
                                .setTitle(R.string.title_login_passwd)
                                .setView(mDialogView)
                                .setPositiveButton(R.string.btn_ok
                                    , new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            
                                            String passwd = ((EditText) mDialogView.findViewById(R.id.passwd_edit))
                                                                .getEditableText().toString();
                                            if (SettingManager.getInstance().getLoginPassword() != null) {
                                                if (SettingManager.getInstance().getLoginPassword().equals(passwd)) {
                                                    mHandler.sendEmptyMessage(START_LOGIN);
                                                } else {
                                                    Toast.makeText(StartActivity.this, R.string.tips_passwd_error, Toast.LENGTH_LONG).show();
                                                    mHandler.sendEmptyMessage(SHOW_PASSWORD_DIALOG);
                                                }
                                            } else {
                                                String passwd_confirm = ((EditText) mDialogView.findViewById(R.id.passwd_confirm_edit))
                                                        .getEditableText().toString();
                                                if (TextUtils.isEmpty(passwd_confirm) || TextUtils.isEmpty(passwd)) {
                                                    Toast.makeText(StartActivity.this, R.string.tips_empty_passwd, Toast.LENGTH_LONG).show();
                                                    mHandler.sendEmptyMessage(SHOW_PASSWORD_DIALOG);
                                                }
                                                if (passwd_confirm.endsWith(passwd)) {
                                                    SettingManager.getInstance().setLoginPassword(passwd_confirm);
                                                    mHandler.sendEmptyMessage(START_LOGIN);
                                                } else {
                                                    Toast.makeText(StartActivity.this, R.string.tips_passwd_confirm_error, Toast.LENGTH_LONG).show();
                                                    mHandler.sendEmptyMessage(SHOW_PASSWORD_DIALOG);
                                                }
                                            }
                                        }
                                })
                                .setNegativeButton(R.string.btn_cancel, null)
                                .create();
        diaog.show();
    }
    
    private class StartTask extends AsyncTask<String, Void, Integer> {
        protected Integer doInBackground(String...params) {
            SettingManager.getInstance().init(getApplicationContext());
            DatabaseOperator.getInstance().init(getApplicationContext());
            
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
            }
            
            return 0;
        }
        
        protected void onPostExecute(Integer result) {
            mHandler.sendEmptyMessage(SHOW_PASSWORD_DIALOG);
        }
    }
}
