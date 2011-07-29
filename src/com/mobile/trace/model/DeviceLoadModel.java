package com.mobile.trace.model;

import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.mobile.trace.engine.BaseEngine;
import com.mobile.trace.internet.FetchAgent;
import com.mobile.trace.internet.FetchAgent.DataFetchCallback;
import com.mobile.trace.internet.FetchRequest;
import com.mobile.trace.utils.Config;
import com.mobile.trace.utils.NotifyHandler;

public class DeviceLoadModel implements DataFetchCallback {
    private static final String TAG = "DeviceLoadModel";
    private static final boolean DEBUG = Config.DEBUG;
    
    private static DeviceLoadModel gDeviceLoadModel = new DeviceLoadModel();
    
    static class DeviceInfo {
        String mIMSI;
        int mPermission;
        String mKey;
        
        @Override
        public String toString() {
            return "DeviceInfo [mIMSI=" + mIMSI + ", mPermission=" + mPermission + ", mKey=" + mKey + "]";
        }
    }
    private DeviceInfo mDeviceInfo;
    private NotifyHandler mDeviceLoadHandler = new NotifyHandler(Config.DEVICE_LOAD);
    
    public static DeviceLoadModel getInstance() {
        return gDeviceLoadModel;
    }
    
    public NotifyHandler getDeviceLoadObserver() {
        return mDeviceLoadHandler;
    }
    
    public void getDeviceInfo() {
        String testData = "{\"MsgType\":0,\"MsgValue\":{\"IMSI\":\"11111111112222222222\",\"PhoneNum\":\"12345678901\"}}";
        FetchRequest rq = FetchRequest.create(FetchRequest.DEVICE_LOAD_TYPE, testData, this);
        
        FetchAgent.getInstance().addRequest(rq);
    }
    
    @Override
    public boolean onDataFetch(InputStream in, int status, int type) {
        if (type == FetchRequest.DEVICE_LOAD_TYPE) {
            if (in != null && status == 200) {
                mDeviceInfo = DeviceLoadInfoEngine.parser(in);
                if (mDeviceInfo != null) {
                    LOGD("[[onDataFetch]] device info = " + mDeviceInfo.toString());
                    mDeviceLoadHandler.notifyAll(1);
                    return true;
                }
            }
        }
        
        mDeviceLoadHandler.notifyAll(0);
        return false;
    }
    
    private DeviceLoadModel() {
    }
    
    static class DeviceLoadInfoEngine extends BaseEngine {
        static DeviceInfo parser(InputStream in) {
            String data = BaseEngine.getJSonContextData(in);
            if (data != null) {
                try {
                    JSONObject jsonObj = new JSONObject(data);
                    JSONObject jsonInfo = (JSONObject) jsonObj.get("MsgValue");
                    if (jsonInfo != null) {
                        DeviceInfo info = new DeviceInfo();
                        info.mIMSI = jsonInfo.optString("IMSI");
                        info.mPermission = jsonInfo.optInt("Permission");
                        info.mKey = jsonInfo.optString("EncryKey");
                        
                        return info;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
