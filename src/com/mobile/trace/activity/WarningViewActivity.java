package com.mobile.trace.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.mobile.trace.R;

public class WarningViewActivity extends Activity {

    public static final int WARNING_TYPE = 0x00;
    public static final int TRACE_TYPE = 0x01;
    
    public static final String ACTION_TYPE = "type";
    
    public static final String TRACE_POINT_WARNING = "trace";
    public static final String LOCAL_POINT_WARNING = "local";
    
    public static final String COMMAND_LOG = "command";
    public static final String WARING_LOG = "warning";
    
    private int mActionType;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        this.setContentView(R.layout.warning_view);
        
        mActionType = getIntent().getIntExtra(ACTION_TYPE, 0);
        if (mActionType == 0x00) {
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
        } else {
            TextView beControlled = (TextView) findViewById(R.id.do_be_controlled_warning);
            beControlled.setText(R.string.command_log_info);
            beControlled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  Intent intentWarning = new Intent();
                  intentWarning.setClass(WarningViewActivity.this, CommandListAcitvity.class);
                  intentWarning.putExtra(COMMAND_LOG, true);
                  startActivity(intentWarning);
                }
            });
            
            TextView Controller = (TextView) findViewById(R.id.do_control_warning);
            Controller.setText(R.string.warning_log_info);
            Controller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentWarning = new Intent();
                    intentWarning.setClass(WarningViewActivity.this, CommandListAcitvity.class);
                    intentWarning.putExtra(WARING_LOG, true);
                    startActivity(intentWarning);
                }
            });
        }
        
 
    }
}
