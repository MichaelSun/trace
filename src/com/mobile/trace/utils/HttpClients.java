package com.mobile.trace.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.mobile.trace.utils.HttpClients.Interfaces.TransferListener;
//import com.mobile.trace.utils.NetWorkSettingInfoManager.SogouHttpHeader;

//import com.sohu.inputmethod.internet.HttpClients.Interfaces.TransferListener;
//import com.sohu.inputmethod.internet.NetWorkSettingInfoManager.SogouHttpHeader;

public class HttpClients {
    private final static String TAG = "HttpClients";
    private final static boolean DEBUG = false;
    
    public static final int SAVE_FILE_SUCCESS = 0;
    public static final int SAVE_FILE_FAILED = 1;
    
    private static final int TIMEOUT = 15 * 1000;
    
    private Context mContext;
    private int size;
    private HttpClient mHc;
    private HttpPost mPost;
    private HttpGet mGet;
    private HttpResponse mResponse;
    private Set listeners;
    private UploadListener mUploadListener;
    private TransferListener mDownloadListener;
    
    private static String mProxyHost;
    private static int mProxyPort = 0;
    //private static Context mContext;
    
    private boolean doUpload = false;
    private boolean doDownload = false;
    
    public static class MapsDemoHttpHeader {
        public static final String APP_TYPE = "MAP_TYPE";
        public static final String APP_COOKIE = "S-COOKIE";
        public static final String APP_PLATFORM = "MAP_PLATFORM";
        public static final String APP_VERSION = "MAP_VERSION";
        public static final String APP_BIULDTIME = "MAP_BIULDID";
    }
    
    public static class Interfaces {
        public interface TransferListener extends EventListener {
            void onStartTransfer(int totalSize);
            void onTransfer(int transferSize, int totalSize);
            void onFinishTransfer(int transferSize, int totalSize);
        }
    }
    
    public HttpClients(Context context) {
        mContext = context;
        size = 0;
        listeners = new HashSet();
        mPost = new HttpPost();
        mGet = new HttpGet();
    }
    
    public void addSizeListener(SizeListener sizeListener) {
        synchronized (listeners) {
            listeners.add(sizeListener);
        }
    }

    public void setUploadListener(UploadListener listener) {
        mUploadListener = listener;
    }
    
    public void setDownloadListener(TransferListener listener) {
        mDownloadListener = listener;
    }
    
    private void handleSizeChanged(int s) {
        int tSize = getDownloadSize();
//        LOGD("handleSizeChanged");
        Iterator iterator;
        synchronized (listeners) {
            iterator = new HashSet(listeners).iterator();
        }

        while (iterator.hasNext()) {

            SizeListener sListener = (SizeListener) iterator.next();
            SizeEvent se = new SizeEvent(this);
            se.setSize(s);
            se.setTotalSize(tSize);
            sListener.onSizeChanged(se);

        }
    }

    public HttpResponse openConnection(int startOffset) {
//        mHc = new DefaultHttpClient(NetWorkSettingInfoManager.getInstance(mContext).getParams());
//        NetWorkSettingInfoManager.getInstance(mContext).setHeader(mGet);
        mHc = new DefaultHttpClient(getParams());
        setHeader(mGet);
        if (startOffset > 0) {
            mGet.addHeader("Range", "bytes=" + startOffset + "-");
        }
        try {
            mResponse = mHc.execute(mGet);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            mResponse = null;
        } catch (IOException e) {
            e.printStackTrace();
            mResponse = null;
        }
        return mResponse;
    }
    
    public HttpResponse openConnection(File uploadFile) {
//        mHc = new DefaultHttpClient(NetWorkSettingInfoManager.getInstance(mContext).getParams());
//        NetWorkSettingInfoManager.getInstance(mContext).setHeader(mPost, uploadFile);
        mHc = new DefaultHttpClient(getParams());
        setHeader(mPost, uploadFile);
        try {
            mResponse = mHc.execute(mPost);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            mResponse = null;
        } catch (IOException e) {
            e.printStackTrace();
            mResponse = null;
        }
        return mResponse;
    }
    
    public HttpResponse openConnection(Map<String, String> uploadContextMap) {
//        mHc = new DefaultHttpClient(NetWorkSettingInfoManager.getInstance(mContext).getParams());
//        NetWorkSettingInfoManager.getInstance(mContext).setHeader(mPost, uploadContextMap);
        mHc = new DefaultHttpClient(getParams());
        setHeader(mPost, uploadContextMap);
        try {
            mResponse = mHc.execute(mPost);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            mResponse = null;
        } catch (IOException e) {
            e.printStackTrace();
            mResponse = null;
        }
        return mResponse;
    }
    
    public HttpResponse openConnection(String uploadData) {
//        mHc = new DefaultHttpClient(NetWorkSettingInfoManager.getInstance(mContext).getParams());
//        NetWorkSettingInfoManager.getInstance(mContext).setHeader(mPost, uploadData);
        
        mHc = new DefaultHttpClient(getParams());
        setHeader(mPost, uploadData);
        try {
            mResponse = mHc.execute(mPost);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            mResponse = null;
        } catch (IOException e) {
            e.printStackTrace();
            mResponse = null;
        }
        return mResponse;
    }

    public void postRequest(String message) {
        
    }
    
    public int downloadFile(HttpResponse response, String outFilePath, boolean append) {
        try {
            File file = new File(outFilePath);
            if (file.exists() == false) {
                file.createNewFile();
            }
            int totalSize;
            if (response.getHeaders(HTTP.CONTENT_LEN) != null) {
                totalSize = Integer.parseInt(response.getFirstHeader(HTTP.CONTENT_LEN).getValue());
            } else {
                totalSize = 0;
                if (mDownloadListener != null) {
                    mDownloadListener.onFinishTransfer(totalSize, totalSize);
                }
                return SAVE_FILE_FAILED;
            }
            if (totalSize == 0) {
                return SAVE_FILE_FAILED;
            }
            
            if (mDownloadListener != null) {
                mDownloadListener.onStartTransfer(totalSize);
            }
            BufferedInputStream fis = new BufferedInputStream(response.getEntity().getContent(), totalSize);
//            LOGD("is available:" + fis.available());
            FileOutputStream fos = new FileOutputStream(file, append);
            
            byte[] buffer = new byte[1024];
            int readLength;
            int downloadSize = 0;
            doDownload = true;
            while (doDownload && (readLength = fis.read(buffer, 0, 1024)) != -1) {
                fos.write(buffer, 0, readLength);
                fos.flush();
                downloadSize += readLength;
                if (mDownloadListener != null) {
                    mDownloadListener.onTransfer(downloadSize, totalSize);
                }
            }
            doDownload = false;
            if (mDownloadListener != null) {
                mDownloadListener.onFinishTransfer(downloadSize, totalSize);
            }
            fis.close();
            fos.close();
        } 
        catch (IOException e) {
            e.printStackTrace();
            return SAVE_FILE_FAILED;
        } catch (Exception e) {
            e.printStackTrace();
            return SAVE_FILE_FAILED;
        }
        return SAVE_FILE_SUCCESS;
    }

    public int getResponseCode() {
        int code = -1;
        if (mResponse != null) {
            code = mResponse.getStatusLine().getStatusCode();
        }
        return code;
    }

    public int getDownloadSize() {
        return Integer.parseInt(mResponse.getFirstHeader(HTTP.CONTENT_LEN).getValue());
    }

    public boolean isValidResponse() {
        if (mResponse != null && mResponse.getHeaders(MapsDemoHttpHeader.APP_TYPE) == null) {
            return false;
        }
        return true;
    }

    public int receiveXMLFile(String outFilePath) {
        try {
//            LOGD(">>>>>>>>>>>> begin save the received xml file <<<<<<<<<");
            File f = new File(outFilePath);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(outFilePath, false);
            InputStream is = mResponse.getEntity().getContent();
            byte[] buffer = new byte[1024];
            int readLength = 0;
            while ((readLength = is.read(buffer, 0, 1024)) != -1) {
                size += readLength;
                handleSizeChanged(size);
                fos.write(buffer, 0, readLength);
                fos.flush();
            }
            fos.close();
            is.close();
//            LOGD("<<<<<<<<< begin dump the received xml file >>>>>>>>>>>");
            dumpReceiveFile(outFilePath);
        } catch (IOException e) {
            LOGD(e.toString());
            return SAVE_FILE_FAILED;
        } catch (NullPointerException npe) {
            LOGD(npe.toString());
            return SAVE_FILE_FAILED;
        } catch (Exception e) {
            LOGD(e.toString());
            return SAVE_FILE_FAILED;
        }
        return SAVE_FILE_SUCCESS;
    }

    public void disConnect() {
        if (mPost.isAborted() == false) mPost.abort();
        if (mGet.isAborted() == false) mGet.abort();
    }

    public void setURL(String urlPath) {
        try {
            String urlPathFixed = urlPath.replace(" ", "");
            URI url = new URI(urlPathFixed);
            if (DEBUG) Log.d(TAG, urlPathFixed);
            if (mPost.isAborted() == true) {
                if (DEBUG) Log.d(TAG, "((((( the original post is aborted, create a new post ))))))");
                mPost = new HttpPost();
            }
            if (mGet.isAborted() == true) {
                if (DEBUG) Log.d(TAG, "((((( the original get is aborted, create a new get ))))))");
                mGet = new HttpGet();
            }
            mPost.setURI(url);
            mGet.setURI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void interruptUpload() {
        doUpload = false;
    }
    
    public void cancelDownload() {
//        LOGD("cancelDownload");
        doDownload = false;
    }

    public class SizeEvent extends EventObject {

        private int mSize;
        private int totalSize;

        public SizeEvent(Object source) {
            super(source);
        }

        public int getSize() {
            return mSize;
        }

        public void setSize(int s) {
            mSize = s;
        }

        public int getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(int s) {
            totalSize = s;
        }
    }

    public interface SizeListener extends EventListener {
        public void onSizeChanged(SizeEvent s);
    }
    
    public interface UploadListener extends EventListener {
       public void onStartUpload(int totalSize);
       public void onUpload(int uploadSize, int totalSize); 
       public void onFinishUpload(int uploadSize, int totalSize);
    }
    
    private void dumpReceiveFile(String filename) {
        if (DEBUG) {
            try {
                LOGD("-------- begin dump the file = " + filename + " --------");
                File file = new File(filename);
                FileInputStream in = new FileInputStream(file);
                int length = (int) file.length();
                byte[] datas = new byte[length];
                in.read(datas, 0, datas.length);
                String result = new String(datas);
                LOGD(result);
            } catch (Exception e) {
            }
        }
    }

    public HttpParams getParams() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        if (getProxy() == true) {
            final HttpHost proxy = new HttpHost(mProxyHost, mProxyPort, "http");
            params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
        return params;
    }
    
    private boolean getProxy() {
        ConnectivityManager ConnMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = ConnMgr.getActiveNetworkInfo();
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                //comment for XT800, it work well without the proxy settings
                //under wifi env
//                mProxyHost = android.net.Proxy.getHost(mContext);
//                mProxyPort = android.net.Proxy.getPort(mContext);
                mProxyPort = 0;
                mProxyHost = null;
            } else {
                mProxyHost = android.net.Proxy.getDefaultHost();
                mProxyPort = android.net.Proxy.getDefaultPort();
            }
            LOGD("[[getProxy]] host = " + mProxyHost + " port = " + mProxyPort);
            return (!TextUtils.isEmpty(mProxyHost) && mProxyPort != 0);
        }
        return false;
    }
    
    public void setHeader(HttpRequestBase post, File uploadFile) {
        //post.setHeader(MapsDemoHttpHeader.APP_COOKIE, getS_COOKIE());
        post.setHeader(HTTP.CONTENT_TYPE, "text/plain");
        post.setHeader("Accept", "*/*");
        post.setHeader("Accept-Encoding", "gzip,deflate");
        post.setHeader(MapsDemoHttpHeader.APP_PLATFORM, "Android");
        //post.setHeader(MapsDemoHttpHeader.APP_VERSION, mCurSoftwareVersion);
        //post.setHeader(MapsDemoHttpHeader.APP_BIULDTIME, mCurGetCookieTime);
        if (uploadFile != null) {
            InputStreamEntity entity = null;
            try {
                FileInputStream fis = new FileInputStream(uploadFile);
                entity = new InputStreamEntity(fis, fis.available());
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ((HttpPost) post).setEntity(entity);
        }
    }
    
    public void setHeader(HttpRequestBase post, String uploadData) {
        //post.setHeader(SogouHttpHeader.SOGOU_COOKIE, getS_COOKIE());
        post.setHeader(HTTP.CONTENT_TYPE, "text/plain");
        post.setHeader("Accept", "*/*");
        //post.setHeader("Accept-Encoding", "gzip,deflate");
        post.setHeader(MapsDemoHttpHeader.APP_PLATFORM, "Android");
        //post.setHeader(SogouHttpHeader.SOGOU_VERSION, mCurSoftwareVersion);
        //post.setHeader(SogouHttpHeader.SOGOU_BIULDTIME, mCurGetCookieTime);
        InputStreamEntity entity = null;
//        try {
//            StringBufferInputStream fis = new StringBufferInputStream(getEncryptData(uploadData));
//            entity = new InputStreamEntity(fis, fis.available());
//            ((HttpPost) post).setEntity(entity);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        
        LOGD("-------- HttpPost header Info ----------");
        LOGD("         The Content-Type = " + "application/x-www-form-urlencoded");
        LOGD("         The Accept-Encoding = " + "gzip,deflate");
        LOGD("         The SOGOU_PLATFORM = " + "Android");
        //LOGD("         The SOGOU_VERSION = " + mCurSoftwareVersion);
        //LOGD("         The SOGOU_BIULDTIME = " + mCurGetCookieTime);
        LOGD("         The entity Content type = " + ((HttpPost) post).getEntity().getContentType());
        LOGD("         The entity body = " + ((HttpPost) post).getEntity().toString());
        LOGD("----------------------------------------");
    }
    
    public void setHeader(HttpRequestBase post, Map<String, String> uploadContextMap) {
        //post.setHeader(SogouHttpHeader.SOGOU_COOKIE, getS_COOKIE());
        post.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
        post.setHeader("Accept", "*/*");
        //post.setHeader("Accept-Encoding", "gzip,deflate");
        post.setHeader(MapsDemoHttpHeader.APP_PLATFORM, "Android");
        //post.setHeader(SogouHttpHeader.SOGOU_VERSION, mCurSoftwareVersion);
        //post.setHeader(SogouHttpHeader.SOGOU_BIULDTIME, mCurGetCookieTime);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (String key : uploadContextMap.keySet()) {
            nvps.add(new BasicNameValuePair(key, uploadContextMap.get(key)));
        }
        try {
            ((HttpPost) post).setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        } catch(UnsupportedEncodingException e) {
            LOGD(e.getMessage());
        }
        LOGD("-------- HttpPost header Info ----------");
        LOGD("         The Content-Type = " + "application/x-www-form-urlencoded");
        LOGD("         The Accept-Encoding = " + "gzip,deflate");
        LOGD("         The SOGOU_PLATFORM = " + "Android");
        //LOGD("         The SOGOU_VERSION = " + mCurSoftwareVersion);
        //LOGD("         The SOGOU_BIULDTIME = " + mCurGetCookieTime);
        LOGD("         The entity Content type = " + ((HttpPost) post).getEntity().getContentType());
        LOGD("         The entity body = " + ((HttpPost) post).getEntity().toString());
        LOGD("----------------------------------------");
    }
    
    public void setHeader(HttpGet get) {
        //get.setHeader(SogouHttpHeader.SOGOU_COOKIE, getS_COOKIE());
        get.setHeader(HTTP.CONTENT_TYPE, "text/plain");
        get.setHeader("Accept", "*/*");
        //get.setHeader("Accept-Encoding", "gzip,deflate");
        get.setHeader(MapsDemoHttpHeader.APP_PLATFORM, "Android");
        //get.setHeader(SogouHttpHeader.SOGOU_VERSION, mCurSoftwareVersion);
        //get.setHeader(SogouHttpHeader.SOGOU_BIULDTIME, mCurGetCookieTime);
    }
    
    public void setDefaultHeaderParams(HttpRequestBase requestBase) {
        //requestBase.setHeader(SogouHttpHeader.SOGOU_COOKIE, getS_COOKIE());
        requestBase.setHeader(HTTP.CONTENT_TYPE, "text/plain");
        requestBase.setHeader("Accept", "*/*");
        requestBase.setHeader(MapsDemoHttpHeader.APP_PLATFORM, "Android");
        //requestBase.setHeader(SogouHttpHeader.SOGOU_VERSION, mCurSoftwareVersion);
        //requestBase.setHeader(SogouHttpHeader.SOGOU_BIULDTIME, mCurGetCookieTime);
    }
    
    private void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }
}
