package com.mobile.trace.utils;

public class Config {

    public static final boolean LOCAL_DEBUG = false;
    
    public static final boolean DEBUG = true;
    
    public static final byte[] COMMAND_GPS = new byte[] {0x01, 0x01};
    public static final byte[] COMMAND_NETWORK = new byte[] {0x01, 0x02};
    public static final byte[] COMMAND_CLOSE_UPDATE = new byte[] {0x02, 0x01};
    public static final byte[] COMMAND_UPDATE_DELAY = new byte[] {0x02, 0x02, 0x00, 0x0a};
    public static final byte[] COMMAND_UPDATE_MOVE = new byte[] {0x02, 0x04, (byte) 0xff, (byte) 0xee};
    public static final byte[] COMMAND_UPDATE_NOW = new byte[] {0x02, 0x08};
    public static final byte[] COMMAND_UPDATE_DAY = new byte[] {0x02, 0x10, 0x0a, 0x05};
    public static final byte[] COMMAND_OUTWARNING_PREFIX = new byte[] {0x02, 0x20};
    public static final byte[] COMMAND_INWARNING_PREFIX = new byte[] {0x02, 0x40};
    
    public static final String BASE_SERVER_IP = "114.242.178.103";
 
    public static final String SPLITOR = ";";
    
    public static final boolean ZOOM_BUTTON_SUPPROT = true;
    
    public static final int LOCATION_TIMEOUT = 20 * 1000;
    
    public static final String WARNING_INFO_SPLIT = ";";
    
    public static final String WARNING_DATABASE_CREATE = "create table warning "
                    + "(_id INTEGER primary key autoincrement, "
                    + "point TEXT not null, "
                    + "region TEXT not null, "
                    + "type TEXT not null, "
                    + "traceid TEXT not null, " 
                    + "rltype TEXT not null, "
                    + "time TEXT not null, "
                    + "phone TEXT not null)";
    
    public static final String WARNING_TABLE_NAME = "warning";
    public static final String WARNING_TABLE_POINT = "point";
    public static final String WARNING_TABLE_REGION = "region";
    public static final String WARNING_TABLE_TYPE = "type";
    public static final String WARNING_TABLE_TRACEID = "traceid";
    public static final String WARNING_TABLE_RLTYPE = "rltype";
    public static final String WARNING_TABLE_TIME = "time";
    public static final String WARNING_TABLE_PHONE = "phone";
    
    public static final String COMMAND_DATABASE_CREATE = "create table command "
                    + "(_id INTEGER primary key autoincrement, "
                    + "traceid TEXT not null, "
                    + "command TEXT not null, " 
                    + "time TEXT not null, "
                    + "phone TEXT not null)";
    
    public static final String COMMAND_TABLE_NAME = "command";
    public static final String COMMAND_TABLE_TRACEID = "traceid";
    public static final String COMMAND_TABLE_COMMAND = "command";
    public static final String COMMAND_TABLE_TIME = "time";
    public static final String COMMAND_TABLE_PHONE = "phone";
    
    public static final String TRACE_INFO_DATABASE_CREATE = "create table traceinfo"
    		        + "(_id INTEGER primary key autoincrement, "
    		        + "traceid TEXT not null, " +
    		        "point TEXT not null, " +
    		        "title TEXT not null, " +
    		        "summary TEXT not null, " +
    		        "phone TEXT not null, " +
    		        "imsi TEXT not null)";
    
    public static final String TRACE_INFO_TABLE_NAME = "traceinfo";
    public static final String TRACE_INFO_ID = "traceid";
    public static final String TRACE_INFO_POINT = "point";
    public static final String TRACE_INFO_TITLE = "title";
    public static final String TRACE_INFO_SUMMARY = "summary";
    public static final String TRACE_INFO_PHONE = "phone";
    public static final String TRACE_INFO_IMSI = "imsi";
    
    public static final int DEVICE_LOAD = 5000;
    public static final int DEVICE_INFOS = 5001;
    
    public static final int WARNING_LOCATE_REQUEST = 1000;
    
    public static final String WARNING_LOCATION_KEY = "location";
    
    public static final String DEFAULT_SPLIOR = ";";
    
    public static final String DATABSE_EMPTY_STRING = "!";
}
