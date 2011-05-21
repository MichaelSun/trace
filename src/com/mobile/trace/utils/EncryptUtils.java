package com.mobile.trace.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.zip.Deflater;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

public class EncryptUtils {
    private static final String TAG = "EncryptUtils";
    private static final boolean DEBUG = true;
    
    public static final String CHARSET = "UTF-8";
    
    public static String getMD5Data(byte[] src) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(src);
            return EncryptUtils.byte2hex(md5.digest()).toLowerCase();
        } catch (Exception e) {
            LOGD(e.getMessage());
        }
        return null;
    }
    
    public static byte[] zip(byte[] data) {
        return compress(data);
    }
    
    public static byte[] compress(byte[] data) {
        byte[] output = new byte[0];

        Deflater compresser = new Deflater();

        compresser.reset();
        compresser.setInput(data);
        compresser.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        compresser.end();
        return output;
    }
    
    public static byte[] Encrypt2Bytes(byte[] sSrc, String sKey) throws Exception {
        byte[] raw = sKey.getBytes(CHARSET);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        byte[] encrypted = cipher.doFinal(sSrc);
        return encrypted;
    }
    
    public static String Encrypt(String sSrc, String sKey) throws Exception {
        byte[] raw = sKey.getBytes(CHARSET);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        byte[] encrypted = cipher.doFinal(sSrc.getBytes());
        return byte2hex(encrypted).toLowerCase();
    }

    public static String Decrypt(String sSrc, String sKey) throws Exception {
        try {
            byte[] raw = sKey.getBytes(CHARSET);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted1 = hex2byte(sSrc.getBytes());
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                originalString = new String(originalString.getBytes(), "UTF-8");
                return originalString;
            } catch (Exception e) {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public static String Decrypt(byte[] sSrc, String sKey) throws Exception {
        try {
            byte[] raw = sKey.getBytes("GBK");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] encrypted1 = hex2byte(sSrc);
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                originalString = new String(originalString.getBytes(), "UTF-8");
                return originalString;
            } catch (Exception e) {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    private static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {

            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }
    
    private static byte[] hex2byte(byte[] b) { 
        if((b.length%2)!=0) 
             throw new IllegalArgumentException("length is not even"); 
        byte[] b2 = new byte[b.length/2]; 
        for (int n = 0; n < b.length; n+=2) { 
           String item = new String(b,n,2);
           b2[n/2] = (byte)Integer.parseInt(item,16); 
         } 
        return b2; 
    } 
    
    private static void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }
}
