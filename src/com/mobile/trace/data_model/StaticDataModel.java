package com.mobile.trace.data_model;

import java.util.ArrayList;

import com.mobile.trace.activity.TracePointInfo;
import com.mobile.trace.activity.WarningRegionOverlay.WarningRegion;


public class StaticDataModel {
	private static StaticDataModel gStaticDataModel;
	
	public static ArrayList<TracePointInfo> tracePointList = new ArrayList<TracePointInfo>();
	public static ArrayList<String> commandList = new ArrayList<String>();
	public static ArrayList<WarningRegion> warningRegionList = new ArrayList<WarningRegion>();
	
	public static StaticDataModel getInstance(){		
        if (gStaticDataModel == null){
    	    gStaticDataModel = new StaticDataModel();
        }    
        return gStaticDataModel;
    }
	
	public void clear() {
	    tracePointList.clear();
	    commandList.clear();
	    warningRegionList.clear();
	}
}
