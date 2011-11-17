package com.mobile.trace.model;

import java.util.ArrayList;
import java.util.Arrays;

import android.util.Log;

import com.mobile.trace.database.DatabaseOperator;
import com.mobile.trace.utils.Config;

public class CommandModel {

    public static class CommandItem {
        public String traceId;
        public String command;
        public String time;
        public String phoneNum;
        
        public byte[] command_byte;

        @Override
        public String toString() {
            return "CommandItem [traceId=" + traceId + ", command=" + command + ", time=" + time + ", command_byte="
                    + Arrays.toString(command_byte) + "]";
        }

    }
    
    private static CommandModel gCommandModel;
    private ArrayList<CommandItem> mCommadItemList;

    public static CommandModel getInstance() {
        if (gCommandModel == null) {
            gCommandModel = new CommandModel();
        }
        
        return gCommandModel;
    }
    
    public ArrayList<CommandItem> getCommandList() {
        return mCommadItemList;
    }
    
    public void addOneComamndItem(CommandItem item) {
        synchronized (mCommadItemList) {
            LOGD("[[addOneCommandItem]] send command item = " + item.toString());
            
            mCommadItemList.add(item);
            DatabaseOperator.getInstance().saveCommand(item.traceId, item.command, item.time, item.phoneNum);
        }
    }
    
    public void deleteCommandItem(int position) {
        synchronized (mCommadItemList) {
            if (position >= mCommadItemList.size()) {
                CommandItem item = mCommadItemList.remove(position);
                DatabaseOperator.getInstance().deleteCommandByTime(item.time);
            }
        }
    }

//    public void saveCommandToDB() {
//        for (CommandItem item : mCommadItemList) {
//            DatabaseOperator.getInstance().saveCommand(item.traceId, item.command, item.time);
//        }
//    }
    
    private CommandModel() {
        mCommadItemList = DatabaseOperator.getInstance().queryCommandLogList();
    } 
    
    private final void LOGD(String msg) {
        if (Config.DEBUG) {
            String tag = this.getClass().getName();
            Log.d(tag, msg);
        }
    }
}
