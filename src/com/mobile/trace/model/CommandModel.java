package com.mobile.trace.model;

import java.util.ArrayList;

import com.mobile.trace.database.DatabaseOperator;

public class CommandModel {

    public static class CommandItem {
        public int traceId;
        public String command;
    }
    
    private static CommandModel gCommandModel;
    private ArrayList<CommandItem> mCommadItemList;

    public static CommandModel getInstance() {
        if (gCommandModel == null) {
            gCommandModel = new CommandModel();
        }
        
        return gCommandModel;
    }
    
    public void addOneComamndItem(CommandItem item) {
        synchronized (mCommadItemList) {
            mCommadItemList.add(item);
        }
    }

    public void saveCommandToDB() {
        for (CommandItem item : mCommadItemList) {
            DatabaseOperator.getInstance().saveCommand(item.traceId, item.command);
        }
    }
    
    private CommandModel() {
        mCommadItemList = DatabaseOperator.getInstance().queryCommandLogList();
    } 
}
