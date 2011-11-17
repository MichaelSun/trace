package com.mobile.trace.activity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mobile.trace.R;
import com.mobile.trace.model.CommandModel;
import com.mobile.trace.model.CommandModel.CommandItem;

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
        
        if (mCommandLog) {
            setListAdapter(new CommandAdapter(this, R.layout.command_item, CommandModel.getInstance().getCommandList()));
        }
        
//        showTraceInfoDialog() ;
    }
    
    private class CommandAdapter extends ArrayAdapter<CommandItem> {
        private int mResourceID;
        private Context mContext;
        private LayoutInflater mInflater;
        
        CommandAdapter(Context context, int resourceId, ArrayList<CommandItem> data) {
            super(context, resourceId, data);
            mResourceID = resourceId;
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        public View  getView(int position, View convertView, ViewGroup parent) {
            View ret = mInflater.inflate(mResourceID, null);
            TextView time = (TextView) ret.findViewById(R.id.time);
            TextView target = (TextView) ret.findViewById(R.id.target);
            TextView command = (TextView) ret.findViewById(R.id.command);
            
            CommandItem item = getItem(getCount() - 1 - position);
            
            time.setText(String.format(getString(R.string.command_time), formatTime(Long.valueOf(item.time))));
            target.setText(String.format(getString(R.string.command_target), item.traceId
                    , (item.phoneNum != null ? item.phoneNum : "")));
            command.setText(String.format(getString(R.string.command_command), item.command));
            
            return ret;
        }
    }
    
//    private void showTraceInfoDialog() {
//    	mCommandInfoDialog = new AlertDialog.Builder(this)
//                                    .setTitle(R.string.command_list)
//                                    .setNegativeButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int whichButton) {
//                                        }
//                                    })
//                                    .create();
//        ListView listView = getListView();
//        //listView.setAdapter(new TraceInfoAdapter(this, Environment.tracePointList));
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (position < CommandModel.getInstance().getCommandList().size()) {
//                	StringBuilder builder = new StringBuilder();
//                	String strCommandInfo = CommandModel.getInstance().getCommandList().get(position).command;
//                	builder.append(strCommandInfo);
////                	builder.append(String.format(getString(R.string.trace_info_id)
////                             , tracePointInfo.id) + "\n");
////                	builder.append(String.format(getString(R.string.trace_info_phonenumber)
////                            , tracePointInfo.phoneNumber + "\n"));
////                	builder.append(String.format(getString(R.string.trace_info_point)
////                            , tracePointInfo.geoPoint) + "\n");
////                	builder.append(String.format(getString(R.string.trace_info_distance), "0"));
////                	builder.append("终端ID：" + tracePointInfo.id + "\n");
////                	builder.append("电话：" + tracePointInfo.phoneNumber + "\n");
////                	builder.append("终端经纬度：" + tracePointInfo.geoPoint + "\n");
////                	builder.append("与主控终端距离：" + "0");
//                	mCommandInfoDialog.setMessage(builder.toString());
////                    mTraceInfoDialog.dismiss();
////                    TracePointInfo info = Environment.tracePointList.get(position);
////                    showTraceInfoDialog(info);
//                	mCommandInfoDialog.show();
//                }
//            }
//        });
//    }
    
//    public static String formatTime(String time) {
//        try {
//            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date date = df.parse(time);
//            return date.toString();
//        }  catch (Exception e) {
//            e.printStackTrace();
//        }
//        
//        return null;
//    }
    
    private static String formatTime(long dateTaken) {
        return DateFormat.format("yyyy-MM-dd hh:mm:ss", dateTaken).toString();
    }
}
