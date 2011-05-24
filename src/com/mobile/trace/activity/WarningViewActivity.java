package com.mobile.trace.activity;

import com.mobile.trace.R;
import com.mobile.trace.utils.SettingManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class WarningViewActivity extends Activity {

    public static final String TRACE_POINT_WARNING = "trace";
    public static final String LOCAL_POINT_WARNING = "local";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        this.setContentView(R.layout.warning_view);
        
        
        View beControlled = findViewById(R.id.do_be_controlled_warning);

        beControlled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent intentWarning = new Intent();
              intentWarning.setClass(WarningViewActivity.this, WarningListActivity.class);
              intentWarning.putExtra(TRACE_POINT_WARNING, true);
              startActivity(intentWarning);
            }
        });
        
        View Controller = findViewById(R.id.do_control_warning);
        
        Controller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentWarning = new Intent();
                intentWarning.setClass(WarningViewActivity.this, WarningListActivity.class);
                intentWarning.putExtra(LOCAL_POINT_WARNING, true);
                startActivity(intentWarning);
            }
        });
    }
}
