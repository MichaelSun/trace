package com.mobile.trace.model;

import java.util.ArrayList;

import com.mobile.trace.database.DatabaseOperator;

public class CommandModel {

    public static class CommandItem {
        public String traceId;
        public String command;
        public String time;
        
        @Override
        public String toString() {
            return "CommandItem [traceId=" + traceId + ", command=" + command + ", time=" + time + "]";
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
            mCommadItemList.add(item);
            DatabaseOperator.getInstance().saveCommand(item.traceId, item.command, item.time);
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
}
