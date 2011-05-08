package com.mobile.trace.activity;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.mobile.trace.R;

public class TraceInfoAdapter implements ListAdapter {
    private static final String TAG = "TraceInfoAdapter";
    
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<TracePointInfo> mInfoList;
    
    public TraceInfoAdapter(Context context, ArrayList<TracePointInfo> infoList) {
        mContext = context;
        mInfoList = new ArrayList<TracePointInfo>();
        mInfoList.addAll(infoList);
        
        mInflater = LayoutInflater.from(mContext);
    }
    
    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int paramInt) {
        return true;
    }

    @Override
    public int getCount() {
        return mInfoList.size();
    }

    @Override
    public Object getItem(int paramInt) {
        return null;
    }

    @Override
    public long getItemId(int paramInt) {
        return 0;
    }

    @Override
    public int getItemViewType(int paramInt) {
        return 0;
    }

    @Override
    public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
        TextView ret = (TextView) mInflater.inflate(R.layout.trace_info_item, null);
        TracePointInfo info = mInfoList.get(paramInt);
        StringBuilder builder = new StringBuilder();
        if (info.id != null) {
            builder.append(String.format(mContext.getString(R.string.trace_info_id)
                                            , info.id));
        }
        builder.append("    ");
        if (info.phoneNumber != null) {
            builder.append(String.format(mContext.getString(R.string.trace_info_phonenumber)
                                            , info.phoneNumber));
        }
        
        ret.setText(builder.toString());
        
        return ret;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver paramDataSetObserver) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver paramDataSetObserver) {
    }

}
