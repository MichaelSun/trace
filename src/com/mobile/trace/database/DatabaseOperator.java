package com.mobile.trace.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.android.maps.GeoPoint;
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
    
    public ArrayList<WarningRegion> queryWarningInfoList() {
        ArrayList<WarningRegion> ret = new ArrayList<WarningRegion>();
        
        Cursor cursor = null;
        try {
            cursor = mDBProxy.query(Config.WARNING_TABLE_NAME, null, null, null);
            LOGD("[[queryWarningInfoList]] cursor = " + cursor);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    WarningRegion wRegion = new WarningRegion();
                    
                    String point = cursor.getString(cursor.getColumnIndex(Config.WARNING_TABLE_POINT));
                    String region = cursor.getString(cursor.getColumnIndex(Config.WARNING_TABLE_REGION));
                    String type = cursor.getString(cursor.getColumnIndex(Config.WARNING_TABLE_TYPE));
                    String traceid = cursor.getString(cursor.getColumnIndex(Config.WARNING_TABLE_TRACEID));

                    String[] splited = point.split(Config.SPLITOR);
                    wRegion.point = new GeoPoint(Integer.valueOf(splited[0]), Integer.valueOf(splited[1]));
                    wRegion.region = Float.valueOf(region);
                    wRegion.warningType = Integer.valueOf(type);
                    wRegion.tracePointId = Integer.valueOf(traceid);
                    wRegion.regionSquare = wRegion.region * wRegion.region;
                    
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
    
    public void saveWarningInfo(WarningRegion region) {
        String geoInfo = String.valueOf(region.point.getLatitudeE6()) + Config.SPLITOR
                            + String.valueOf(region.point.getLongitudeE6());
        
        ContentValues values = new ContentValues();
        values.put(Config.WARNING_TABLE_POINT, geoInfo);
        values.put(Config.WARNING_TABLE_REGION, String.valueOf(region.region));
        values.put(Config.WARNING_TABLE_TYPE, String.valueOf(region.warningType));
        values.put(Config.WARNING_TABLE_TRACEID, String.valueOf(region.tracePointId));

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
            cursor = mDBProxy.query(Config.COMMAND_TABLE_NAME, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    CommandItem item = new CommandItem();
                    
                    item.traceId = Integer.valueOf(cursor.getString(cursor.getColumnIndex(Config.COMMAND_TABLE_TRACEID)));
                    item.command = cursor.getString(cursor.getColumnIndex(Config.COMMAND_TABLE_COMMAND));
                    
                    ret.add(item);
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
     
    public void saveCommand(int traceId, String command) {
        ContentValues values = new ContentValues();
        values.put(Config.COMMAND_TABLE_TRACEID, String.valueOf(traceId));
        values.put(Config.COMMAND_TABLE_COMMAND, command);

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
