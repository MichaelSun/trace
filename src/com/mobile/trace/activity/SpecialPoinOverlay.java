package com.mobile.trace.activity;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class SpecialPoinOverlay extends ItemizedOverlay<OverlayItem> {

    private Context mContext;
    private ArrayList<GeoPoint> mPoints = new ArrayList<GeoPoint>();
    private Drawable mMarkerDrawable;
    
    public SpecialPoinOverlay(Context context, Drawable defaultMarker, ArrayList<GeoPoint> points) {
        super(defaultMarker);
        mMarkerDrawable = boundCenterBottom(defaultMarker);
        synchronized (mPoints) {
            mPoints.clear();
            mPoints.addAll(points);
        }
        mContext = context;
        populate();
    }
    
    public void addOverlay(GeoPoint point) {
        synchronized (mPoints) {
            mPoints.add(point);
        }
        populate();
    }
    
    public void clearOverlay() {
        synchronized (mPoints) {
            mPoints.clear();
        }
    }
    
    @Override
    protected OverlayItem createItem(int index) {
        OverlayItem ret = null;
        try {
            GeoPoint point = mPoints.get(index);
            ret = new OverlayItem(point, null, null);
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        return ret;
    }

    @Override
    public int size() {
        return mPoints.size();
    }

}
