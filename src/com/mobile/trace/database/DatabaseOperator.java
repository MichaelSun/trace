package com.mobile.trace.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.mobile.trace.activity.TracePointInfo;
import com.mobile.trace.activity.WarningRegionOverlay.WarningRegion;
import com.mobile.trace.model.CommandModel.CommandItem;
import com.mobile.trace.utils.Config;

public class DatabaseOperator {
    private static final String TAG = "DatabaseOperator";
    private static final boolean DEBUG = true;
    
    private static DatabaseOperator gDatabaseOperator;
    private static Object mObj = new Object();
    
    private DatabaseProxy mDBProxy;

    public static DatabaseOperator getInstance() {
        if (gDatabaseOperator == null) {
            synchronized (mObj) {
                if (gDatabaseOperator == null) {
                    gDatabaseOperator = new DatabaseOperator();
                }
            }
        }
        
        return gDatabaseOperator;
    }
    
    public void init(Context context) {
        mDBProxy = DatabaseProxy.getDBInstance(context);
    }
    
    public ArrayList<WarningRegion> queryWarningInfoList(int warningType) {
        ArrayList<WarningRegion> ret = new ArrayList<WarningRegion>();
        
        String selection = Config.WARNING_TABLE_RLTYPE + "=?";
        String[] selectionArgs = new String[]{ String.valueOf(warningType) };
        
        Cursor cursor = null;
        try {
            if (warningType != WarningRegion.WARNING_TYPE_REMOTE
                    && warningType != WarningRegion.WARNING_TYPE_LOCAL) {
                cursor = mDBProxy.query(Config.WARNING_TABLE_NAME, null, null, null);
            } else {
                cursor = mDBProxy.query(Config.WARNING_TABLE_NAME, selection, selectionArgs, null);
            }
            LOGD("[[queryWarningInfoList]] cursor = " + cursor);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    WarningRegion wRegion = new WarningRegion();
                    
                    String point = cursor.getString(cursor.getColumnIndex(Config.WARNING_TABLE_POINT));
                    String region = cursor.getString(cursor.getColumnIndex(Config.WARNING_TABLE_REGION));
                    String type = cursor.getString(cursor.getColumnIndex(Config.WARNING_TABLE_TYPE));
                    String traceid = cursor.getString(cursor.getColumnIndex(Config.WARNING_TABLE_TRACEID));
                    String rlType = cursor.getString(cursor.getColumnIndex(Config.WARNING_TABLE_RLTYPE));

                    String[] splited = point.split(Config.SPLITOR);
                    wRegion.point = new GeoPoint(Integer.valueOf(splited[0]), Integer.valueOf(splited[1]));
                    wRegion.region = Float.valueOf(region);
                    wRegion.warningType = Integer.valueOf(type);
                    wRegion.tracePointId = traceid;
                    wRegion.warningRemoteLocalType = Integer.valueOf(rlType);
                    
                    ret.add(wRegion);
                    
                    LOGD("[[queryWarningInfoList]] region info = " + wRegion);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        
        return ret;
    }
    
    public void deleteWaringInfo(WarningRegion region) {
        String geoInfo = String.valueOf(region.point.getLatitudeE6()) + Config.SPLITOR
                            + String.valueOf(region.point.getLongitudeE6());
        String selection = Config.WARNING_TABLE_POINT + "=?";
        String[] selectionArgs = new String[]{ geoInfo };
        
        mDBProxy.delete(Config.WARNING_TABLE_NAME, selection, selectionArgs);
    }
    
    public void saveWarningInfo(WarningRegion region) {
        String geoInfo = String.valueOf(region.point.getLatitudeE6()) + Config.SPLITOR
                            + String.valueOf(region.point.getLongitudeE6());
        
        ContentValues values = new ContentValues();
        values.put(Config.WARNING_TABLE_POINT, geoInfo);
        values.put(Config.WARNING_TABLE_REGION, String.valueOf(region.region));
        values.put(Config.WARNING_TABLE_TYPE, String.valueOf(region.warningType));
        values.put(Config.WARNING_TABLE_TRACEID, String.valueOf(region.tracePointId));
        values.put(Config.WARNING_TABLE_RLTYPE, String.valueOf(region.warningRemoteLocalType));

        String selection = Config.WARNING_TABLE_POINT + "=? AND " + Config.WARNING_TABLE_TRACEID + "=?";
        String[] selectionArgs = new String[]{geoInfo, String.valueOf(region.tracePointId) };
        
        Cursor cursor = null;
        
        try {
            cursor = mDBProxy.query(Config.WARNING_TABLE_NAME,
                    selection, selectionArgs, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    mDBProxy.update(Config.WARNING_TABLE_NAME, values, selection, selectionArgs);
                } else {
                    mDBProxy.insert(Config.WARNING_TABLE_NAME, values);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    public ArrayList<CommandItem> queryCommandLogList() {
        ArrayList<CommandItem> ret = new ArrayList<CommandItem>();
        
        Cursor cursor = null;
        try {
            LOGD("[[queryCommandLogList]] before get command list from database");
            cursor = mDBProxy.query(Config.COMMAND_TABLE_NAME, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    CommandItem item = new CommandItem();
                    
                    item.traceId = cursor.getString(cursor.getColumnIndex(Config.COMMAND_TABLE_TRACEID));
                    item.command = cursor.getString(cursor.getColumnIndex(Config.COMMAND_TABLE_COMMAND));
                    item.time = cursor.getString(cursor.getColumnIndex(Config.COMMAND_TABLE_TIME));
                    
                    ret.add(item);
                    LOGD("[[queryCommandLogList]] command item = " + item.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        
        return ret;
    }
    
    public void deleteCommandByTime(String time) {
        if (time == null) {
            return;
        }
        String selection = Config.COMMAND_TABLE_TIME + "=?";
        String[] selectionArgs = new String[]{ time };
        
        mDBProxy.delete(Config.COMMAND_TABLE_NAME, selection, selectionArgs);
    }
     
    public void saveCommand(String traceId, String command, String time) {
        ContentValues values = new ContentValues();
        values.put(Config.COMMAND_TABLE_TRACEID, traceId);
        values.put(Config.COMMAND_TABLE_COMMAND, command);
        values.put(Config.COMMAND_TABLE_TIME, time);

        mDBProxy.insert(Config.COMMAND_TABLE_NAME, values);
    }
    
    public ArrayList<TracePointInfo> queryTracePointInfoList() {
        ArrayList<TracePointInfo> ret = new ArrayList<TracePointInfo>();
        
        Cursor cursor = null;
        try {
            LOGD("[[queryTracePointInfoList]]");
            cursor = mDBProxy.query(Config.TRACE_INFO_TABLE_NAME, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    TracePointInfo info = new TracePointInfo();
                    
                    info.id = cursor.getString(cursor.getColumnIndex(Config.TRACE_INFO_ID));
                    info.title = cursor.getString(cursor.getColumnIndex(Config.TRACE_INFO_TITLE));
                    info.summary = cursor.getString(cursor.getColumnIndex(Config.TRACE_INFO_SUMMARY));
                    info.phoneNumber = cursor.getString(cursor.getColumnIndex(Config.TRACE_INFO_PHONE));
                    info.imsi = cursor.getString(cursor.getColumnIndex(Config.TRACE_INFO_IMSI));
                    String point = cursor.getString(cursor.getColumnIndex(Config.TRACE_INFO_POINT));
                    
                    String[] splited = point.split(Config.SPLITOR);
                    info.geoPoint = new GeoPoint(Integer.valueOf(splited[0]), Integer.valueOf(splited[1]));
                    
                    if (info.title.equals(Config.DATABSE_EMPTY_STRING)) {
                        info.title = "";
                    }
                    if (info.summary.equals(Config.DATABSE_EMPTY_STRING)) {
                        info.summary = "";
                    }
                    if (info.imsi.equals(Config.DATABSE_EMPTY_STRING)) {
                        info.imsi = "";
                    }
                    
                    ret.add(info);
                    LOGD("[[queryTracePointInfoList]] TraceInfo item = " + info.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return ret;
    }
    
    public void deleteTraceInfo(TracePointInfo info) {
        if (info == null || info.id == null) {
            return;
        }
        
        String selection = Config.TRACE_INFO_ID + "=?";
        String[] selectionArgs = new String[]{ info.id };

        mDBProxy.delete(Config.TRACE_INFO_TABLE_NAME, selection, selectionArgs);
    }
    
    public void deleteAllTraceInfo() {
        mDBProxy.delete(Config.TRACE_INFO_TABLE_NAME, null, null);
    }
    
    public void saveTraceInfo(TracePointInfo info) {
        if (info == null || info.id == null) {
            return;
        }
        
        ContentValues values = new ContentValues();
        values.put(Config.TRACE_INFO_ID, info.id);
        values.put(Config.TRACE_INFO_IMSI, (!TextUtils.isEmpty(info.imsi) ? info.imsi : Config.DATABSE_EMPTY_STRING));
        values.put(Config.TRACE_INFO_PHONE, info.phoneNumber);
        values.put(Config.TRACE_INFO_SUMMARY, (!TextUtils.isEmpty(info.summary) ? info.summary : Config.DATABSE_EMPTY_STRING));
        values.put(Config.TRACE_INFO_TITLE, (!TextUtils.isEmpty(info.title) ? info.title : Config.DATABSE_EMPTY_STRING));
        String geoInfo = String.valueOf(info.geoPoint.getLatitudeE6()) + Config.SPLITOR
                + String.valueOf(info.geoPoint.getLongitudeE6());
        values.put(Config.TRACE_INFO_POINT, geoInfo);

        mDBProxy.insert(Config.COMMAND_TABLE_NAME, values);
    }
    
    private DatabaseOperator() {
    }
    
    private void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }
}
