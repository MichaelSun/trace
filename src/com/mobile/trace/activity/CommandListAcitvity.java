package com.mobile.trace.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mobile.trace.R;
import com.mobile.trace.activity.WarningRegionOverlay.WarningRegion;
import com.mobile.trace.data_model.StaticDataModel;
import com.mobile.trace.model.CommandModel;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class CommandListAcitvity extends ListActivity {
	
	private AlertDialog mCommandInfoDialog;
	private int mType = -1;
	private boolean mCommandLog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getIntent().getBooleanExtra(WarningViewActivity.COMMAND_LOG, false)) {
            mCommandLog = true;
        } else {
            mCommandLog = false;
        }
        
        setListAdapter(new SimpleAdapter(this, getData(),
                android.R.layout.simple_list_item_1, new String[] { "title" },
                new int[] { android.R.id.text1 }));
        
        showTraceInfoDialog() ;
    } 
    
    private void showTraceInfoDialog() {
    	mCommandInfoDialog = new AlertDialog.Builder(this)
                                    .setTitle(R.string.command_list)
                                    .setNegativeButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    })
                                    .create();
        ListView listView = getListView();
        //listView.setAdapter(new TraceInfoAdapter(this, Environment.tracePointList));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < CommandModel.getInstance().getCommandList().size()) {
                	StringBuilder builder = new StringBuilder();
                	String strCommandInfo = CommandModel.getInstance().getCommandList().get(position).command;
                	builder.append(strCommandInfo);
//                	builder.append(String.format(getString(R.string.trace_info_id)
//                             , tracePointInfo.id) + "\n");
//                	builder.append(String.format(getString(R.string.trace_info_phonenumber)
//                            , tracePointInfo.phoneNumber + "\n"));
//                	builder.append(String.format(getString(R.string.trace_info_point)
//                            , tracePointInfo.geoPoint) + "\n");
//                	builder.append(String.format(getString(R.string.trace_info_distance), "0"));
//                	builder.append("终端ID：" + tracePointInfo.id + "\n");
//                	builder.append("电话：" + tracePointInfo.phoneNumber + "\n");
//                	builder.append("终端经纬度：" + tracePointInfo.geoPoint + "\n");
//                	builder.append("与主控终端距离：" + "0");
                	mCommandInfoDialog.setMessage(builder.toString());
//                    mTraceInfoDialog.dismiss();
//                    TracePointInfo info = Environment.tracePointList.get(position);
//                    showTraceInfoDialog(info);
                	mCommandInfoDialog.show();
                }
            }
        });
    }
    
    protected List getData() {
    	List<Map> myData = new ArrayList<Map>();
    	if (mCommandLog) {
            int iLen = CommandModel.getInstance().getCommandList().size();
            for (int i = 0; i < iLen; i++) {
                addItem(myData, "命令：" + CommandModel.getInstance().getCommandList().get(i).command, null);
            }
    	} else {
    	}
    	return myData;
    }
    
    protected void addItem(List<Map> data, String name, Intent intent) {
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("title", name);
        temp.put("intent", intent);
        data.add(temp);
    }
}
