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
        public GeoPoint point;
        public float region;
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
              projection.toPixels(region.point, point);

              float distance = projection.metersToEquatorPixels(region.region);
              LOGD("[[draw]] >>> float region = " + distance + " km = " + region.region);
              canvas.drawCircle(point.x, point.y, distance, paint);
          }

          canvas.restore();
      }
        super.draw(canvas, mapView, false);
    }
    
    
    private static void LOGD(String msg) {
        if (Config.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
