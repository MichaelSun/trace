package com.mobile.trace.internet;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;

import android.content.Context;
import android.util.Log;

import com.mobile.trace.utils.Config;
import com.mobile.trace.utils.InternetUtils;

public class FetchAgent {
    private static final String TAG = "FetchAgent";
    private static final boolean DEBUG = Config.DEBUG;
    
    public interface DataFetchCallback {
        boolean onDataFetch(InputStream is, int status, int type);
    }
    
    public static FetchAgent mFetchAgent = new FetchAgent();
    private ArrayList<FetchRequest> mRequests = new ArrayList<FetchRequest>();
    private Fetcher[] fetchers;
    private boolean mInit;
//    private HashSet<DataFetchCallback> mDataCallbackList = new HashSet<DataFetchCallback>();
    
    public static final FetchAgent getInstance() {
        return mFetchAgent;
    }
    
    public void addRequest(FetchRequest r) {
        if (r == null) return;
        synchronized (mRequests) {
            mRequests.add(r);
            mRequests.notifyAll();
        }
    }
    
//    public void addDataFetchCallback(DataFetchCallback cb) {
//        synchronized (mDataCallbackList) {
//            mDataCallbackList.add(cb);
//        }
//    }
    
//    public void removeDataFetchCallback(DataFetchCallback cb) {
//        synchronized (mDataCallbackList) {
//            mDataCallbackList.remove(cb);
//        }
//    }
    
    private FetchAgent() {
    }
    
    private FetchAgent(int nThreads, Context context) {
        init(nThreads, context);
    }
    
    public void init(int nThreads, Context context) {
        if (mInit) {
            return;
        } else {
            mInit = true;
        }
        
        fetchers = new Fetcher[nThreads];
        for (int i = 0; i < fetchers.length; ++i) {
            fetchers[i] = new Fetcher();
            fetchers[i].start();
        }
    }
    
    public void destroy() {
        mInit = false;
        
        for (int i = 0; i < fetchers.length; ++i) {
            boolean retry = true;
            fetchers[i].runnable = false;
            while (retry) {
                try {
                    fetchers[i].join();
                    retry = false;
                } catch (InterruptedException e) {
                    LOGD("interrupted");
                }
            }
            LOGD("dead " + Integer.toString(i));
        }
    }
    
    private class Fetcher extends Thread {
        public volatile boolean runnable;

        public void run() {
            runnable = true;

            while (runnable) {
                FetchRequest rq = null;
                try {
                    while (runnable && mRequests.isEmpty()) {
                        synchronized (mRequests) {
                            mRequests.wait();
                        }
                    }

                    synchronized (mRequests) {
                        if (!mRequests.isEmpty()) {
                            rq = mRequests.remove(0);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (rq != null) {
                    doWork(rq);
                }
            }
        }
    }
    
    private void doWork(FetchRequest rq) {
        LOGD("[[doWork]]  FetchRequest info = " + rq.toString());
        try {
            HttpResponse r = InternetUtils.OpenHttpConnection(rq.getUrl(), rq.getPostData());
            if (r != null) {
                if (rq.getCB() != null) {
                    InputStream data = r.getEntity() != null ? r.getEntity().getContent() : null;
                    rq.getCB().onDataFetch(data,
                                r.getStatusLine().getStatusCode(),
                                rq.getType());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static final void LOGD(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
