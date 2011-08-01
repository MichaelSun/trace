package com.mobile.trace.activity;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.mobile.trace.R;
import com.mobile.trace.data_model.StaticDataModel;

public class TraceInfoListActivity extends ListActivity {

    private ProgressDialog mProgressDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//        initProgressDialog();
        
        this.setListAdapter(new TraceInfoAdapter(this, StaticDataModel.tracePointList));
        
        this.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < StaticDataModel.tracePointList.size()) {
                    
                }
            }
        });
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
