package com.mobile.trace.activity;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.mobile.trace.utils.Config;

public class PopOverlay extends ItemizedOverlay<OverlayItem> {
	
	private Drawable mMarkDrawable;
	private ArrayList<TracePointInfo> mTraceInfoList;
	
    private static final int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
            | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG;

    private final ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
	
	public PopOverlay(Drawable defaultMarker, ArrayList<TracePointInfo> traceList) {
		super(defaultMarker);
		mMarkDrawable = boundCenterBottom(defaultMarker);
		mTraceInfoList = new ArrayList<TracePointInfo>();
		mTraceInfoList.addAll(traceList);
		
		populate();
	}
	
	public void addOverlay(OverlayItem overlay) {
		overlayItems.add(overlay);
		populate();
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
//		if (!shadow) {
//			canvas.save(LAYER_FLAGS);
//
//			Projection projection = mapView.getProjection();
//			int size = overlayItems.size();
//			Point point = new Point();
//			Paint paint = new Paint();
//			paint.setAntiAlias(true);
//			OverlayItem overLayItem;
//
//			for (int i = 0; i < size; i++) {
//				overLayItem = overlayItems.get(i);
//
//				Drawable marker = overLayItem.getMarker(0);
//				// marker.getBounds()
//				/* 象素点取得转换 */
//				projection.toPixels(overLayItem.getPoint(), point);
//
//				if (marker != null) {
//					boundCenterBottom(marker);
//				}
//
//				/* 圆圈 */
////				paint.setColor(Color.RED);
//				canvas.drawCircle(point.x, point.y, 10, paint);
//
//				/* 标题 */
//				String title = overLayItem.getTitle();
//
//				if (title != null && title.length() > 0) {
//					paint.setColor(Color.BLACK);
//					paint.setTextSize(15);
//					canvas.drawText(title, point.x, point.y, paint);
//				}
//				
//			}
//
//			canvas.restore();
//		}
		super.draw(canvas, mapView, false);
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		
		System.out.println(p.getLatitudeE6());
		System.out.println(p.getLongitudeE6());
		return super.onTap(p, mapView);
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
