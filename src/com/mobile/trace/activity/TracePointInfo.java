package com.mobile.trace.activity;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;
import com.mobile.trace.activity.WarningRegionOverlay.WarningRegion;

public class TracePointInfo {

    public String id;
    
    public GeoPoint geoPoint;
    
    public ArrayList<WarningRegion> localWarningRegion;
    
    public ArrayList<WarningRegion> remoteWaringRegion;
    
    public String title;
    
    public String summary;
    
    public String phoneNumber;
    
    public String imsi;

    @Override
    public String toString() {
        return "TracePointInfo [id=" + id + ", geoPoint=" + geoPoint + ", localWarningRegion=" + localWarningRegion
                + ", remoteWaringRegion=" + remoteWaringRegion + ", title=" + title + ", summary=" + summary
                + ", phoneNumber=" + phoneNumber + ", imsi=" + imsi + "]";
    }

}
