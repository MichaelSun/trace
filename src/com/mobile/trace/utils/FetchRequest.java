package com.mobile.trace.utils;

import android.text.TextUtils;

import com.mobile.trace.utils.FetchAgent.DataFetchCallback;

public class FetchRequest {
    public static final int DEVICE_LOAD_TYPE = 0x01;
    public static final int DEVICE_INFOS_TYPE = 0x02;
    
    private static final String BASE_URL = "http://114.242.178.111/ServiceTest/BackService.asmx/";
    private static final String METHOD_DEVICELOAD = "MonitorDeviceLoad";
    private static final String METHOD_DEVICEINFO_UPDATE = "EndDeviceStatusUpdate";
    
    private String mPostData;
    private String mMethod;
    private String mUrl;
    private int mType;
    private DataFetchCallback mCB;
    
    public static FetchRequest create(int methodType, String data, DataFetchCallback cb) {
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        
        return new FetchRequest(methodType, data, cb);
    }
    
    @Override
    public String toString() {
        return "FetchRequest [mPostData=" + mPostData + ", mMethod=" + mMethod + ", mUrl=" + mUrl + "]";
    }

    String getPostData() {
        return mPostData;
    }
    
    String getUrl() {
        return mUrl;
    }
    
    int getType() {
        return mType;
    }
    
    DataFetchCallback getCB() {
        return mCB;
    }
    
    private FetchRequest(int methodType, String data, DataFetchCallback cb) {
        if ((methodType & DEVICE_LOAD_TYPE) != 0) {
            mMethod = METHOD_DEVICELOAD;
        } else if ((methodType & DEVICE_INFOS_TYPE) != 0) {
            mMethod = METHOD_DEVICEINFO_UPDATE;
        }
        
        mType = methodType;
        mPostData = data;
        mUrl = BASE_URL + mMethod;
        mCB = cb;
    }
}
