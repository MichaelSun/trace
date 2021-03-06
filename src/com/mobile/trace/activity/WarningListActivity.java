package com.mobile.trace.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.mobile.trace.R;
import com.mobile.trace.activity.WarningRegionOverlay.WarningRegion;
import com.mobile.trace.database.DatabaseOperator;
import com.mobile.trace.model.TraceDeviceInfoModel;
import com.mobile.trace.utils.Config;

public class WarningListActivity extends Activity {

	private InfoAdapter mDataAdapter;
	private AlertDialog mTraceInfoDialog;
	
	private ArrayList<WarningRegion> mWarningList;
	private AlertDialog mDeleteTraceListDialog;
	private int iDeletePosition;
	private ListView mListView;
	private String mSelectWarningItemInfo;
	
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
        
        mDataAdapter = new InfoAdapter(this, R.layout.warning_list_item, getData());
        
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(mDataAdapter);
        showTraceInfoDialog() ;
        
        setResult(Activity.RESULT_CANCELED);
    } 
    
    private void showTraceInfoDialog() {
        mTraceInfoDialog = new AlertDialog.Builder(this)
                                    .setTitle(R.string.title_trace_warning)
                                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    })
                                    .setNegativeButton(R.string.btn_locate, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent retIntent = new Intent();
                                            retIntent.putExtra(Config.WARNING_LOCATION_KEY, mSelectWarningItemInfo);
                                            setResult(Activity.RESULT_OK, retIntent);
                                            mTraceInfoDialog.dismiss();
                                            finish();
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
                	            + String.valueOf((warningInfo.point.getLatitudeE6() * 1.0) / 1E6)
                	            + " lon : "
                	            + String.valueOf((warningInfo.point.getLongitudeE6() * 1.0) / 1E6)
                	            + "\n");
                	builder.append("告警半径 ： " + String.valueOf(warningInfo.region) + " 千米\n");
                	builder.append("被控终端ID ： " + String.valueOf(warningInfo.tracePointId));
                	mTraceInfoDialog.setMessage(builder.toString());
                	
                	mSelectWarningItemInfo = String.valueOf((warningInfo.point.getLatitudeE6() * 1.0) / 1E6) 
                	                            + Config.DEFAULT_SPLIOR
                	                            + String.valueOf((warningInfo.point.getLongitudeE6() * 1.0) / 1E6);
                	mTraceInfoDialog.show();
                }
            }
        });
        
        mDeleteTraceListDialog = new AlertDialog.Builder(this)
        .setTitle(R.string.titile_trace_info_list)
        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener(){
        	public void onClick(DialogInterface dialog, int whichButton) {
        		TraceDeviceInfoModel.getInstance().removeLocalWarningRegion(mWarningList.get(iDeletePosition));
        		mWarningList = DatabaseOperator.getInstance().queryWarningInfoList(mType);
        		//mWarningList.remove(iDeletePosition);
				//mSimpleAdapter.notifyDataSetChanged();		
		        mDataAdapter = new InfoAdapter(WarningListActivity.this, R.layout.warning_list_item, getData());
		        
				mListView.setAdapter(mDataAdapter);
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
    
    protected ArrayList<WarningRegion> getData() {
        return mWarningList;
    }
    
    class InfoAdapter extends ArrayAdapter<WarningRegion> {
        private int mResourceID;
        private Context mContext;
        private LayoutInflater mInflater;
        
        InfoAdapter(Context context, int resourceId, ArrayList<WarningRegion> data) {
            super(context, resourceId, data);
            mResourceID = resourceId;
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        public View  getView(int position, View convertView, ViewGroup parent) {
            View ret = convertView;
            if (ret == null) {
                ret = mInflater.inflate(mResourceID, null);
            }
            
            WarningRegion info = getItem(position);
            TextView tv = (TextView) ret.findViewById(R.id.time);
            String time = "";
            if (info.time != 0) {
                time = formatTime(info.time);
            }
            tv.setText(String.format(getString(R.string.time), time));
            tv = (TextView) ret.findViewById(R.id.id);
            tv.setText(String.format(getString(R.string.id), info.tracePointId));
            String phone = "";
            if (info.phone != null && !info.phone.equals("-1")) {
                phone = info.phone;
            }
            tv = (TextView) ret.findViewById(R.id.phone);
            tv.setText(String.format(getString(R.string.phone), phone));
            
            return ret;
        }
    }
    
    private static String formatTime(long dateTaken) {
        return DateFormat.format("yyyy-MM-dd hh:mm:ss", dateTaken).toString();
    }
}
