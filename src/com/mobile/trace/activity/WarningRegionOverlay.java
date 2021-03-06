package com.mobile.trace.activity;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.mobile.trace.R;
import com.mobile.trace.utils.Config;

public class WarningRegionOverlay extends ItemizedOverlay<OverlayItem> {
    private static final String TAG = "WarningRegionOverlay";
    
    public static class WarningRegion {
        public static final int WARNING_TYPE_IN = 0;
        public static final int WARNING_TYPE_OUT = 1;
        
        public static final int WARNING_TYPE_REMOTE = 0x02;
        public static final int WARNING_TYPE_LOCAL = 0x04;
        
        public static final String INTERNAL_SPLITOR = "#";
        
        public GeoPoint point;
        public float region;
        public float regionPixel;
        public float regionSquare;
        public int warningType;
        public String tracePointId;
        
        public int warningRemoteLocalType;
        
        public long time;
        
        public String phone = "-1";
        
        public WarningRegion() {
        }
        
        public WarningRegion(String info) {
            if (info != null) {
                String[] infos = info.split(INTERNAL_SPLITOR);
                if (infos != null) {
                    point = new GeoPoint(Integer.valueOf(infos[0])
                                    , Integer.valueOf(infos[1]));
                    regionPixel = Float.valueOf(infos[2]);
                    regionSquare = Float.valueOf(infos[3]);
                    warningType = Integer.valueOf(infos[4]);
                    tracePointId = infos[5];
                }
            }
        }
        
        public String makeSaveString() {
            StringBuilder ret = new StringBuilder();
            ret.append(String.valueOf(point.getLatitudeE6())).append(INTERNAL_SPLITOR);
            ret.append(String.valueOf(point.getLongitudeE6())).append(INTERNAL_SPLITOR);
            ret.append(String.valueOf(regionPixel)).append(INTERNAL_SPLITOR);
            ret.append(String.valueOf(regionSquare)).append(INTERNAL_SPLITOR);
            ret.append(String.valueOf(warningType)).append(INTERNAL_SPLITOR);
            ret.append(String.valueOf(tracePointId));
            
            return ret.toString();
        }
        
        @Override
        public String toString() {
            return "Geoppint = " + point + " region = " + region + " regionPixel = " + regionPixel + " regionSquare = " + regionSquare
                        + " traceId = " + tracePointId + " warning Type = " + warningType;
        }
        
    }
    
    private static final int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG 
                                                    | Canvas.CLIP_SAVE_FLAG
                                                    | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG 
                                                    | Canvas.FULL_COLOR_LAYER_SAVE_FLAG 
                                                    | Canvas.CLIP_TO_LAYER_SAVE_FLAG;
    private static final int WARNING_REGION_COLOR = 0x3f0000ff;
    
    private ArrayList<WarningRegion> mWarningPoints = new ArrayList<WarningRegion>();
    private Context mContext;
    
    public WarningRegionOverlay(Context context, Drawable defaultMarker, ArrayList<WarningRegion> warningPoint) {
        super(defaultMarker);
        synchronized (mWarningPoints) {
            mWarningPoints.clear();
            mWarningPoints.addAll(warningPoint);
        }
        mContext = context;
        populate();
    }

    public ArrayList<WarningRegion> getWarningRegionList() {
        ArrayList<WarningRegion> ret = new ArrayList<WarningRegion>();
        synchronized (mWarningPoints) {
            ret.clear();
            ret.addAll(mWarningPoints);
        }
        return ret;
    }
    
    public void setWarningRegionList(ArrayList<WarningRegion> list) {
        synchronized (mWarningPoints) {
            mWarningPoints.clear();
            mWarningPoints.addAll(list);
            populate();
        }
    }
    
    @Override
    protected OverlayItem createItem(int paramInt) {
        OverlayItem ret = null;
        try {
            synchronized (mWarningPoints) {
                WarningRegion region = mWarningPoints.get(paramInt);
                ret = new OverlayItem(region.point, null, null);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        return ret;
    }

    @Override
    public int size() {
        synchronized (mWarningPoints) {
            return mWarningPoints.size();
        }
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        LOGD("[[WarngingRegionOverlay::draw]]");
        
        if (!shadow) {
            canvas.save(LAYER_FLAGS);

            Projection projection = mapView.getProjection();
            int size = mWarningPoints.size();
            Point point = new Point();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(WARNING_REGION_COLOR);
            WarningRegion region = null;

            for (int i = 0; i < size; i++) {
                region = mWarningPoints.get(i);
                region.regionPixel = projection.metersToEquatorPixels((float) region.region * 1000);
                region.regionSquare = region.regionPixel * region.regionPixel;
                
                projection.toPixels(region.point, point);

                LOGD("[[draw]] >>> float region = " + region.regionPixel);
                canvas.drawCircle(point.x, point.y, region.regionPixel, paint);
            }

            canvas.restore();
        }
//        super.draw(canvas, mapView, false);
    }
    
    
    private static void LOGD(String msg) {
        if (Config.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
