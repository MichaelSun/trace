package com.mobile.trace.model;

import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.mobile.trace.activity.TracePointInfo;
import com.mobile.trace.data_model.StaticDataModel;
import com.mobile.trace.engine.BaseEngine;
import com.mobile.trace.internet.FetchAgent;
import com.mobile.trace.internet.FetchAgent.DataFetchCallback;
import com.mobile.trace.internet.FetchRequest;
import com.mobile.trace.utils.Config;
import com.mobile.trace.utils.NotifyHandler;

public class TraceDeviceInfoModel implements
        DataFetchCallback {
    private static final String TAG = "TraceDeviceInfoModel";
    private static final boolean DEBUG = Config.DEBUG;

    public static class ServerTraceInfo {
        public String imsi;
        public long time;
        public int latitude;
        public int logtitude;
        public int flag;
        
        
        @Override
        public String toString() {
            return "TraceInfo [imsi=" + imsi + ", time=" + time + ", latitude=" + latitude + ", logtitude=" + logtitude
                    + ", flag=" + flag + "]";
        }
    }
    
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
            ArrayList<ServerTraceInfo> infoList = new ArrayList<ServerTraceInfo>();
            for (TraceDeviceInfo Orginfo : mTraceDeviceInfoList) {
                LOGD("[[onDataFetch]] orgin device info = " + Orginfo.toString());
                ServerTraceInfo infoItem = new ServerTraceInfo();
                infoItem.imsi = Orginfo.imsi != null ? Orginfo.imsi.trim() : null;
                String[] splitedInfo = Orginfo.gpsInfo != null ? Orginfo.gpsInfo.split(",") : null;
                if (splitedInfo != null && splitedInfo.length == 11) {
                    SimpleDateFormat dayInFormat = new SimpleDateFormat("ddMMyyHHmmss");
                    String hourTime = splitedInfo[0].trim();
                    hourTime = hourTime.substring(0, hourTime.lastIndexOf("."));
                    String timeStr = splitedInfo[9].trim() + hourTime;
                    Date date = dayInFormat.parse(timeStr, new ParsePosition(0));
                    LOGD("[[onDataFetch]] time = " + date.toLocaleString());
                    infoItem.time = date.getTime();
                    String latStr = splitedInfo[1].trim();
                    latStr = latStr.substring(0, latStr.length() - 1);
                    LOGD("[[onDataFetch]] latStr = " + latStr + " Double = " + Double.valueOf(latStr)
                            + " 10E4 = " + 10E4);
                    infoItem.latitude = (int) (Double.valueOf(latStr) * 1E4);
                    String lonStr = splitedInfo[2].trim();
                    lonStr = lonStr.substring(0, lonStr.length() - 1);
                    infoItem.logtitude = (int) (Double.valueOf(lonStr) * 1E4);
                }
                infoItem.flag = Orginfo.flag;
                
                infoList.add(infoItem);
                LOGD("[[onDataFetch]] info parsed = " + infoItem.toString());
            }
            
            if (infoList.size() != 0) {
                StaticDataModel.tracePointList.clear();
                for (ServerTraceInfo info : infoList) {
                    TracePointInfo pointInfo = new TracePointInfo();
                    pointInfo.id = info.imsi;
                    pointInfo.geoPoint = new GeoPoint(info.latitude, info.logtitude);
                    pointInfo.title = "";
                    pointInfo.summary = "";
                    pointInfo.phoneNumber = info.imsi;
                    StaticDataModel.tracePointList.add(pointInfo);
                    LOGD("[[onDataFetch]] trace point to show item info = " + pointInfo);
                }
            }
            
            mDeviceInfosHandler.notifyAll(1);
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
