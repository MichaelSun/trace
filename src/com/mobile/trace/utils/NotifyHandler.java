package com.mobile.trace.utils;

import java.util.HashSet;

import android.os.Handler;
import android.os.Message;

public class NotifyHandler {
    private static final String TAG = "NotifyHandler";
    
    private final int mFlag;
    private HashSet<Handler> mHandlerSet = new HashSet<Handler>();

    public NotifyHandler(int flag) {
        mFlag = flag;
    }

    public void addObserver(Handler handler) {
        mHandlerSet.add(handler);
    }

    public void removeObserver(Handler handler) {
        mHandlerSet.remove(handler);
    }
    
    public void notifyAll(Object obj) {
        for (Handler handler : mHandlerSet) {
            Message message = Message.obtain();
            message.what = mFlag;
            message.obj = obj;
            handler.sendMessage(message);
        }
    }
    
    public boolean isEmpty() {
        if (mHandlerSet.size() == 0) {
            return true;
        }
        return false;
    }
}
