package com.mobile.trace.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.mobile.trace.R;
import com.mobile.trace.activity.WarningRegionOverlay.WarningRegion;
import com.mobile.trace.data_model.StaticDataModel;

public class WarningListActivity extends ListActivity {

//	public WarningListActivity() {
//		// TODO Auto-generated constructor stub
//	}
	
	private AlertDialog mTraceInfoDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Intent intent = getIntent();
        //MapViewActivity.mTracePointList.get(0);
        
        setListAdapter(new SimpleAdapter(this, getData(),
                android.R.layout.simple_list_item_1, new String[] { "title" },//simple_list_item_1
                new int[] { android.R.id.text1 }));
        
        showTraceInfoDialog() ;
    } 
    
    private void showTraceInfoDialog() {
        mTraceInfoDialog = new AlertDialog.Builder(this)
                                    .setTitle(R.string.titile_trace_info_list)
                                    .setNegativeButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    })
                                    .create();
        ListView listView = getListView();
        //listView.setAdapter(new TraceInfoAdapter(this, Environment.tracePointList));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < StaticDataModel.mWarningRegionList.size()) {
                	StringBuilder builder = new StringBuilder();
                	WarningRegion warningInfo = StaticDataModel.mWarningRegionList.get(position);
                	builder.append("警告区域中心(Lat : " 
                	            + String.valueOf((warningInfo.point.getLatitudeE6() * 1.0) / 10E6)
                	            + " lon : "
                	            + String.valueOf((warningInfo.point.getLongitudeE6() * 1.0) / 10E6)
                	            + "\n");
                	builder.append("警告区域 ： " + String.valueOf(warningInfo.region) + " 千米\n");
                	builder.append("被控终端ID ： " + String.valueOf(warningInfo.tracePointId));
                	mTraceInfoDialog.setMessage(builder.toString());
                	mTraceInfoDialog.show();
                }
            }
        });
    }
    
    protected List getData() {
    	List<Map> myData = new ArrayList<Map>();
    	int iLen = StaticDataModel.mWarningRegionList.size();
    	for(int i = 0; i < iLen; i++) {
    		addItem(myData
    		        , "警告区域信息：" + StaticDataModel.mWarningRegionList.get(i).point.toString()
    		        , null);
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
