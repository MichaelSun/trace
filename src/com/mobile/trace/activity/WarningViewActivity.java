package com.mobile.trace.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.mobile.trace.R;
import com.mobile.trace.utils.Config;

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
            TextView title = (TextView) findViewById(R.id.title_bar_center_text);
            title.setText(R.string.title_warning_activity);
            
            View beControlled = findViewById(R.id.do_be_controlled_warning);
            beControlled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  Intent intentWarning = new Intent();
                  intentWarning.setClass(WarningViewActivity.this, WarningListActivity.class);
                  intentWarning.putExtra(TRACE_POINT_WARNING, true);
                  startActivityForResult(intentWarning, Config.WARNING_LOCATE_REQUEST);
                }
            });
            
            View Controller = findViewById(R.id.do_control_warning);
            
            Controller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentWarning = new Intent();
                    intentWarning.setClass(WarningViewActivity.this, WarningListActivity.class);
                    intentWarning.putExtra(LOCAL_POINT_WARNING, true);
                    startActivityForResult(intentWarning, Config.WARNING_LOCATE_REQUEST);
                }
            });
            
            View tips = findViewById(R.id.be_controlled_tips);
            tips.setVisibility(View.VISIBLE);
            tips = findViewById(R.id.do_controlled_tips);
            tips.setVisibility(View.VISIBLE);
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
    
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == Config.WARNING_LOCATE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        }
    }
}
