package com.mobile.trace.activity;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.mobile.trace.R;

public class TraceInfoListActivity extends ListActivity {

    private ProgressDialog mProgressDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initProgressDialog();
    }
    
    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.wait);
        mProgressDialog.setButton(getString(R.string.btn_cancel)
                            , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mProgressDialog.dismiss();
                                }
                            });
        mProgressDialog.show();
    }
}
