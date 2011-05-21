package com.mobile.trace.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mobile.trace.R;
import com.mobile.trace.data_model.StaticDataModel;

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
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Intent intent = getIntent();
        //MapViewActivity.mTracePointList.get(0);
        StaticDataModel.getInstance().commandList.add("CommandTest1");
        StaticDataModel.getInstance().commandList.add("CommandTest2");
        StaticDataModel.getInstance().commandList.add("CommandTest3");
        StaticDataModel.getInstance().commandList.add("CommandTest4");
        
        setListAdapter(new SimpleAdapter(this, getData(),
                android.R.layout.simple_list_item_1, new String[] { "title" },//simple_list_item_1
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
                if (position < StaticDataModel.getInstance().commandList.size()) {
                	StringBuilder builder = new StringBuilder();
                	String strCommandInfo = StaticDataModel.getInstance().commandList.get(position);
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
    	//addItem(myData, "test name A", activityIntent("test pkg","test componentName"));
    	//addItem(myData, "test name B", activityIntent("test pkg","test componentName"));
    	//addItem(myData, "test name C", activityIntent("test pkg","test componentName"));
    	int iLen = StaticDataModel.getInstance().commandList.size();
    	for(int i = 0; i < iLen; i++){
    		//MapViewActivity.mTracePointList.get(i).id;
    		addItem(myData, "命令：" + StaticDataModel.getInstance().commandList.get(i), null);
    	}
    	return myData;
    }
    
//    protected Intent activityIntent(String pkg, String componentName) {
//        Intent result = new Intent();
//        result.setClassName(pkg, componentName);
//        return result;
//    }
    
    protected void addItem(List<Map> data, String name, Intent intent) {
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("title", name);
        temp.put("intent", intent);
        data.add(temp);
    }
}
