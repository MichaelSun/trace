package com.mobile.trace.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.mobile.trace.R;
import com.mobile.trace.activity.WarningRegionOverlay.WarningRegion;
import com.mobile.trace.database.DatabaseOperator;

public class WarningListActivity extends Activity {

	private SimpleAdapter mSimpleAdapter;
	private AlertDialog mTraceInfoDialog;
	
	private ArrayList<WarningRegion> mWarningList;
	private AlertDialog mDeleteTraceListDialog;
	private int iDeletePosition;
	private ListView mListView;
	
	private int mType = -1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.warning_list);

        TextView titleTV = (TextView) findViewById(R.id.title_bar_center_text);
        if (getIntent().getBooleanExtra(WarningViewActivity.TRACE_POINT_WARNING, false)) {
            mType = WarningRegion.WARNING_TYPE_REMOTE;
            titleTV.setText(R.string.be_controlled_info);
        } else {
            mType = WarningRegion.WARNING_TYPE_LOCAL;
            titleTV.setText(R.string.controller_info);
        }
        
        if (mType == WarningRegion.WARNING_TYPE_REMOTE) {
            mWarningList = DatabaseOperator.getInstance().queryWarningInfoList(WarningRegion.WARNING_TYPE_REMOTE);
        } else {
            mWarningList = DatabaseOperator.getInstance().queryWarningInfoList(WarningRegion.WARNING_TYPE_LOCAL);
        }
        
        mSimpleAdapter = new SimpleAdapter(this, getData(),
                android.R.layout.simple_list_item_1, new String[] { "title" },//simple_list_item_1
                new int[] { android.R.id.text1 });
        
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(mSimpleAdapter);
        showTraceInfoDialog() ;
    } 
    
    private void showTraceInfoDialog() {
        mTraceInfoDialog = new AlertDialog.Builder(this)
                                    .setTitle(R.string.title_trace_warning)
                                    .setNegativeButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    })
                                    .create();
        //listView.setAdapter(new TraceInfoAdapter(this, Environment.tracePointList));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < mWarningList.size()) {
                	StringBuilder builder = new StringBuilder();
                	WarningRegion warningInfo = mWarningList.get(position);
                	builder.append("警告区域中心(Lat : " 
                	            + String.valueOf((warningInfo.point.getLatitudeE6() * 1.0) / 10E6)
                	            + " lon : "
                	            + String.valueOf((warningInfo.point.getLongitudeE6() * 1.0) / 10E6)
                	            + "\n");
                	builder.append("告警半径 ： " + String.valueOf(warningInfo.region) + " 千米\n");
                	builder.append("被控终端ID ： " + String.valueOf(warningInfo.tracePointId));
                	mTraceInfoDialog.setMessage(builder.toString());
                	mTraceInfoDialog.show();
                }
            }
        });
        
        mDeleteTraceListDialog = new AlertDialog.Builder(this)
        .setTitle(R.string.titile_trace_info_list)
        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener(){
        	public void onClick(DialogInterface dialog, int whichButton) {
				//StaticDataModel.mWarningRegionList.remove(iDeletePosition);
        		//mWarningList.remove(iDeletePosition);
        		mWarningList.remove(mWarningList.get(iDeletePosition));
        		DatabaseOperator.getInstance().deleteWaringInfo(mWarningList.get(iDeletePosition));
//		        for (WarningRegion region : mWarningList) {
//		            DatabaseOperator.getInstance().saveWarningInfo(region);
//		        }
		        ArrayList<WarningRegion> mWarningRegionList = DatabaseOperator.getInstance().queryWarningInfoList(mType);
				//mWarningList.remove(iDeletePosition);
				//mSimpleAdapter.notifyDataSetChanged();		
				WarningListActivity.this.mSimpleAdapter = new SimpleAdapter(WarningListActivity.this, getData(),
		                android.R.layout.simple_list_item_1, new String[] { "title" },//simple_list_item_1
		                new int[] { android.R.id.text1 });
		        
				mListView.setAdapter(mSimpleAdapter);
		        showTraceInfoDialog() ;
            }
        })
        .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
				//StaticDataModel.mWarningRegionList.remove(iDeletePosition);
				//mSimpleAdapter.notifyDataSetChanged();		
            }
        })
        .create();

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
//				StaticDataModel.mWarningRegionList.remove(position);
//				mSimpleAdapter.notifyDataSetChanged();	
				iDeletePosition = position;
				StringBuilder builder = new StringBuilder();
				builder.append("要删除这项么？ " );
				mDeleteTraceListDialog.setMessage(builder.toString());				
				mDeleteTraceListDialog.show();
				return false;
			}
        });

    }
    
    protected List getData() {
    	List<Map> myData = new ArrayList<Map>();
    	int iLen = mWarningList.size();
    	for(int i = 0; i < iLen; i++) {
    		addItem(myData
    		        , "警告区域信息：" + mWarningList.get(i).point.toString()
    		           +  "\n被控终端ID：" + mWarningList.get(i).tracePointId
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
