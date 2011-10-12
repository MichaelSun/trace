package com.mobile.trace.activity;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.mobile.trace.R;
import com.mobile.trace.utils.Config;

public class PopOverlay extends ItemizedOverlay<OverlayItem> {
	
	private Drawable mMarkDrawable;
	private Drawable mInfoDrawableBg;
	private ArrayList<TracePointInfo> mTraceInfoList;
	private Context mContext;
	
	private static final String CONST_STRING = "12345678901";
	
    private static final int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
            | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG;

    private final ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
	
	public PopOverlay(Context context, Drawable defaultMarker, Drawable infoBg, ArrayList<TracePointInfo> traceList) {
		super(boundCenterBottom(defaultMarker));
//		mMarkDrawable = boundCenterBottom(defaultMarker);
		mMarkDrawable = defaultMarker;
		mInfoDrawableBg = infoBg;
		mTraceInfoList = new ArrayList<TracePointInfo>();
		mTraceInfoList.addAll(traceList);
		mContext = context;
		
		populate();
	}
	
	public void addOverlay(OverlayItem overlay) {
		overlayItems.add(overlay);
		populate();
	}
	
	public void setOverlayList(ArrayList<TracePointInfo> list) {
        synchronized (mTraceInfoList) {
            mTraceInfoList.clear();
            mTraceInfoList.addAll(list);
            populate();
        }
	}
	
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (!shadow) {
            canvas.save(LAYER_FLAGS);
    
            Paint textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(16);
            textPaint.setColor(Color.BLUE);
            textPaint.setFakeBoldText(true);
            
            Projection projection = mapView.getProjection();
            int size = mTraceInfoList.size();
            Point point = new Point();
            TracePointInfo info = null;
            for (int i = 0; i < size; i++) {
                info = mTraceInfoList.get(i);
                
                String showText = String.format(mContext.getString(R.string.title_trace_point)
                                            , info.id
                                            , info.phoneNumber);
                int textWidth = (int) textPaint.measureText(showText);
                projection.toPixels(info.geoPoint, point);
    
                mInfoDrawableBg.setBounds(point.x - textWidth - 25
                                        , point.y - mInfoDrawableBg.getIntrinsicHeight()
                                        , point.x - 10
                                        , point.y);
                mInfoDrawableBg.draw(canvas);
                canvas.drawText(showText
                        , point.x - textWidth - 17
                        , point.y - mInfoDrawableBg.getIntrinsicHeight() + 25
                        , textPaint);
            }
    
            canvas.restore();
        }
        super.draw(canvas, mapView, false);
    }
	
	@Override
	protected synchronized OverlayItem createItem(int i) {
		OverlayItem ret = null;
        try {
            TracePointInfo info = mTraceInfoList.get(i);
            String title = String.valueOf(info.id) + Config.SPLITOR + info.phoneNumber;
            ret = new OverlayItem(info.geoPoint, title, info.title);
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        return ret;
	}

	@Override
	public synchronized int size() {
		return mTraceInfoList.size();
	}
	
}
