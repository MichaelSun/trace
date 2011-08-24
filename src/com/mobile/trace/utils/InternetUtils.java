package com.mobile.trace.utils;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public class InternetUtils {
    private static final String TAG = "InternetUtils";
    private static final boolean DEBUG = true;
    
    private static final int TIMEOUT = 30000;

    public static HttpResponse OpenHttpConnection(String urlString) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        DefaultHttpClient hc = new DefaultHttpClient(params);
        HttpGet get = new HttpGet();
        try {
            get.setURI(new URI(urlString));
            return hc.execute(get);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HttpResponse OpenHttpConnection(String urlString, String postData) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        DefaultHttpClient hc = new DefaultHttpClient(params);
        HttpPost post = new HttpPost();
        try {
            post.setURI(new URI(urlString));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("data", postData));
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        
        LOGD("[[OpenHttpConnection]] open the url = " + urlString
                + " post data = " + nvps.toString());
        
        try {
            return hc.execute(post);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static HttpResponse OpenHttpConnection(String urlString, byte[] postData) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        DefaultHttpClient hc = new DefaultHttpClient(params);
        HttpPost post = new HttpPost();
        try {
            post.setURI(new URI(urlString));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        post.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
        post.setHeader("Accept-Encoding", "gzip,deflate");
        ByteArrayEntity byteData = new ByteArrayEntity(postData);
        post.setEntity(byteData);
        
        LOGD("[[OpenHttpConnection]] open the url = " + urlString
                + " post data = " + byteData.toString());
        
        try {
            return hc.execute(post);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static HttpResponse OpenHttpConnection(String postUrl, Map<String, String> uploadContextMap) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        DefaultHttpClient hc = new DefaultHttpClient(params);
        HttpPost post = new HttpPost();
        try {
            post.setURI(new URI(postUrl));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        post.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
        post.setHeader("Accept-Encoding", "gzip,deflate");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (String key : uploadContextMap.keySet()) {
            nvps.add(new BasicNameValuePair(key, uploadContextMap.get(key)));
        }
        try {
            post.setEntity(new UrlEncodedFormEntity(nvps));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        try {
            return hc.execute(post);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // private boolean getProxy() {
    // ConnectivityManager ConnMgr = (ConnectivityManager)
    // mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    // NetworkInfo info = ConnMgr.getActiveNetworkInfo();
    // if (info != null) {
    // if (info.getType() == ConnectivityManager.TYPE_WIFI) {
    // mProxyHost = android.net.Proxy.getHost(mContext);
    // mProxyPort = android.net.Proxy.getPort(mContext);
    // } else {
    // mProxyHost = android.net.Proxy.getDefaultHost();
    // mProxyPort = android.net.Proxy.getDefaultPort();
    // }
    // return (!TextUtils.isEmpty(mProxyHost) && mProxyPort != 0);
    // }
    // return false;
    // }
    
    private static void LOGD(String msg) {
        if (Config.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
