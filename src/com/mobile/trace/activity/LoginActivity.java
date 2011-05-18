package com.mobile.trace.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.mobile.trace.R;
import com.mobile.trace.utils.SettingManager;

public class LoginActivity extends Activity {

    private EditText mEditText;
    
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
                    
                    Intent nextIntent = new Intent();
                    nextIntent.setClass(LoginActivity.this, MapViewActivity.class);
//                    nextIntent.setAction("com.mobile.trace.maps");
                    startActivity(nextIntent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this
                            , LoginActivity.this.getString(R.string.empty_phone_bumber)
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
