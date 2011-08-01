package com.mobile.trace.activity;

import com.google.android.maps.GeoPoint;
import com.mobile.trace.activity.WarningRegionOverlay.WarningRegion;

public class TracePointInfo {

    public String id;
    
    public GeoPoint geoPoint;
    
    public WarningRegion warningRegion;
    
    public String title;
    
    public String summary;
    
    public String phoneNumber;

    @Override
    public String toString() {
        return "TracePointInfo [id=" + id + ", geoPoint=" + geoPoint + ", warningRegion=" + warningRegion + ", title="
                + title + ", summary=" + summary + ", phoneNumber=" + phoneNumber + "]";
    }
    
}
