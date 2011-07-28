package com.mobile.trace.model;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.mobile.trace.engine.BaseEngine;
import com.mobile.trace.utils.Config;
import com.mobile.trace.utils.FetchAgent;
import com.mobile.trace.utils.FetchAgent.DataFetchCallback;
import com.mobile.trace.utils.FetchRequest;
import com.mobile.trace.utils.NotifyHandler;

public class TraceDeviceInfoModel implements
        DataFetchCallback {
    private static final String TAG = "TraceDeviceInfoModel";
    private static final boolean DEBUG = Config.DEBUG;

    static class TraceDeviceInfo {
        String imsi;
        String time;
        String cellId;
        String gpsInfo;
        int flag;
        
        @Override
        public String toString() {
            return "TraceDeviceInfo [imsi=" + imsi + ", time=" + time + ", cellId=" + cellId + ", gpsInfo=" + gpsInfo
                    + ", flag=" + flag + "]";
        }
    }
    
    private ArrayList<TraceDeviceInfo> mTraceDeviceInfoList;
    private static TraceDeviceInfoModel gTraceDeviceInfoModel = new TraceDeviceInfoModel();
    private NotifyHandler mDeviceInfosHandler = new NotifyHandler(Config.DEVICE_INFOS);
    
    public static TraceDeviceInfoModel getInstance() {
        return gTraceDeviceInfoModel;
    }
    
    public NotifyHandler getDeviceInfosObserver() {
        return mDeviceInfosHandler;
    }
    
    public void getTraceDeviceInfos() {
        String testData = "{\"MsgType\":2,\"MsgValue\":{\"IMSI\":\"22222222222222\"}}";
        FetchRequest rq = FetchRequest.create(FetchRequest.DEVICE_INFOS_TYPE, testData, this);
        
        FetchAgent.getInstance().addRequest(rq);
    }
    
    @Override
    public boolean onDataFetch(InputStream in, int status, int type) {
        mTraceDeviceInfoList = TraceDeviceEngine.parser(in);
        if (mTraceDeviceInfoList != null && mTraceDeviceInfoList.size() != 0) {
            mDeviceInfosHandler.notifyAll(mDeviceInfosHandler);
        }
        mDeviceInfosHandler.notifyAll(null);
        return true;
    }

    private TraceDeviceInfoModel() {
    }
    
    static class TraceDeviceEngine extends BaseEngine {
        static ArrayList<TraceDeviceInfo> parser(InputStream in) {
            ArrayList<TraceDeviceInfo> ret = new ArrayList<TraceDeviceInfo>();
            String data = BaseEngine.getJSonContextData(in);
            LOGD("[[parser]] data = " + data);
            try {
                JSONObject jsonObj = new JSONObject(data);
                JSONArray jsonArray = jsonObj.optJSONArray("MsgValue");
                if (jsonArray != null) {
                    JSONObject obj = null;
                    for (int index = 0; index < jsonArray.length(); ++index) {
                        obj = jsonArray.getJSONObject(index);
                        if (obj != null) {
                            TraceDeviceInfo info = new TraceDeviceInfo();
                            info.imsi = obj.optString("IMSI");
                            info.time = obj.optString("Date");
                            info.cellId = obj.optString("CellId");
                            info.gpsInfo = obj.optString("GpsInfo");
                            info.flag = obj.optInt("Flag");
                            ret.add(info);
                        }
                    }
                    
                    return ret;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    
    private static final void LOGD(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
