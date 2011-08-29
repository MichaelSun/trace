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
import com.mobile.trace.activity.WarningRegionOverlay.WarningRegion;
import com.mobile.trace.database.DatabaseOperator;
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
    
    static class TraceDeviceInfoForServer {
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
    
    private ArrayList<TraceDeviceInfoForServer> mTraceDeviceInfoForServerList;
    private ArrayList<TracePointInfo> mTraceInfoList;
    private ArrayList<WarningRegion> mRemoteWarningRegionList;
    private ArrayList<WarningRegion> mLocalWarningRegionList;
    private static TraceDeviceInfoModel gTraceDeviceInfoModel = new TraceDeviceInfoModel();
    private NotifyHandler mDeviceInfosHandler = new NotifyHandler(Config.DEVICE_INFOS);
    
    public static TraceDeviceInfoModel getInstance() {
        return gTraceDeviceInfoModel;
    }
    
    public NotifyHandler getDeviceInfosObserver() {
        return mDeviceInfosHandler;
    }
    
    public void fetchTraceInfoFromServer() {
        String testData = "{\"MsgType\":2,\"MsgValue\":{\"IMSI\":\"22222222222222\"}}";
        FetchRequest rq = FetchRequest.create(FetchRequest.DEVICE_INFOS_TYPE, testData, this);
        
        FetchAgent.getInstance().addRequest(rq);
    }
    
    public ArrayList<TracePointInfo> getTracePointInfo() {
        return mTraceInfoList;
    }
    
    public ArrayList<WarningRegion> getLocalWarninRegion() {
        return mLocalWarningRegionList;
    }
    
    public void addTracePointInfo(TracePointInfo info) {
        if (info != null) {
            boolean hasExists = false;
            for (TracePointInfo item : mTraceInfoList) {
                if (item.id.equals(info.id)) {
                    hasExists = true;
                }
            }
            
            if (!hasExists) {
                mTraceInfoList.add(info);
                DatabaseOperator.getInstance().saveTraceInfo(info);
                constructWarngingRegionForTracePoint(info);
            }
        }
    }
    
    public void addLocalWarningRegion(WarningRegion region) {
        if (region != null) {
            if (!mLocalWarningRegionList.contains(region)) {
                mLocalWarningRegionList.add(region);
            }
            DatabaseOperator.getInstance().saveWarningInfo(region);
            for (TracePointInfo info : mTraceInfoList) {
                if (info.id.equals(region.tracePointId)) {
                    if (info.localWarningRegion == null) {
                        info.localWarningRegion = new ArrayList<WarningRegion>();
                    }
                    info.localWarningRegion.add(region);
                }
            }
        }
    }
    
    public void removeLocalWarningRegion(WarningRegion region) {
        if (region != null) {
            DatabaseOperator.getInstance().deleteWaringInfo(region);
            String geoInfo = String.valueOf(region.point.getLatitudeE6()) + Config.SPLITOR
                    + String.valueOf(region.point.getLongitudeE6());
            WarningRegion regionRemove = null;
            for (int index = 0; index < mLocalWarningRegionList.size(); ++index) {
                WarningRegion r = mLocalWarningRegionList.get(index);
                String geoStr = String.valueOf(r.point.getLatitudeE6()) + Config.SPLITOR
                        + String.valueOf(r.point.getLongitudeE6());
                if (geoStr.equals(geoInfo)) {
                    regionRemove = r;
                    break;
                }
            }
            if (regionRemove != null) {
                mLocalWarningRegionList.remove(regionRemove);
            }
            for (TracePointInfo info : mTraceInfoList) {
                if (info.localWarningRegion != null) {
                    info.localWarningRegion.remove(region);
                }
            }
        }
    }
    
    public void flushWarningRegion() {
        for (WarningRegion region : mLocalWarningRegionList) {
            DatabaseOperator.getInstance().saveWarningInfo(region);
        }
        
        for (WarningRegion region : mRemoteWarningRegionList) {
            DatabaseOperator.getInstance().saveWarningInfo(region);
        }
    }
    
    @Override
    public boolean onDataFetch(InputStream in, int status, int type) {
        mTraceDeviceInfoForServerList = TraceDeviceEngine.parser(in);
        
        if (mTraceDeviceInfoForServerList != null && mTraceDeviceInfoForServerList.size() != 0) {
            ArrayList<ServerTraceInfo> infoList = new ArrayList<ServerTraceInfo>();
            for (TraceDeviceInfoForServer Orginfo : mTraceDeviceInfoForServerList) {
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
                DatabaseOperator.getInstance().deleteAllTraceInfo();
                mTraceInfoList.clear();
                for (ServerTraceInfo info : infoList) {
                    TracePointInfo pointInfo = new TracePointInfo();
                    pointInfo.id = info.imsi;
                    pointInfo.geoPoint = new GeoPoint(info.latitude, info.logtitude);
                    pointInfo.title = "";
                    pointInfo.summary = "";
                    pointInfo.phoneNumber = info.imsi;
                    pointInfo.imsi = info.imsi;
                    constructWarngingRegionForTracePoint(pointInfo);
                    
                    DatabaseOperator.getInstance().saveTraceInfo(pointInfo);
                    mTraceInfoList.add(pointInfo);
                    
                    LOGD("[[onDataFetch]] trace point to show item info = " + pointInfo);
                }
            }
            
            mDeviceInfosHandler.notifyAll(1);
        }
        mDeviceInfosHandler.notifyAll(null);
        return true;
    }
    
    @Override
    public boolean onDataFetchError(int reason, int type) {
        mDeviceInfosHandler.notifyAll(null);
        return false; 
    }

    private void constructWarngingRegionForTracePoint(TracePointInfo info) {
        for (WarningRegion region : mRemoteWarningRegionList) {
            if (region.tracePointId.equals(info.id)) {
                if (info.remoteWaringRegion == null) {
                    info.remoteWaringRegion = new ArrayList<WarningRegion>();
                }
                info.remoteWaringRegion.add(region);
            }
        }
        
        for (WarningRegion region : mLocalWarningRegionList) {
            if (region.tracePointId.equals(info.id)) {
                if (info.localWarningRegion == null) {
                    info.localWarningRegion = new ArrayList<WarningRegion>();
                }
                info.localWarningRegion.add(region);
            }
        }
    }
    
    private TraceDeviceInfoModel() {
        mTraceInfoList = DatabaseOperator.getInstance().queryTracePointInfoList();
        mRemoteWarningRegionList = DatabaseOperator.getInstance().queryWarningInfoList(WarningRegion.WARNING_TYPE_REMOTE);
        mLocalWarningRegionList = DatabaseOperator.getInstance().queryWarningInfoList(WarningRegion.WARNING_TYPE_LOCAL);
        
        for (TracePointInfo info : mTraceInfoList) {
            constructWarngingRegionForTracePoint(info);
        }
    }
    
    static class TraceDeviceEngine extends BaseEngine {
        static ArrayList<TraceDeviceInfoForServer> parser(InputStream in) {
            ArrayList<TraceDeviceInfoForServer> ret = new ArrayList<TraceDeviceInfoForServer>();
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
                            TraceDeviceInfoForServer info = new TraceDeviceInfoForServer();
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
