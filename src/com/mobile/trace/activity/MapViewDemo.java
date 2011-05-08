package com.mobile.trace.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.mobile.trace.R;
import com.mobile.trace.activity.WarningRegionOverlay.WarningRegion;
import com.mobile.trace.utils.Config;
import com.mobile.trace.utils.Environment;

public class MapViewDemo extends MapActivity implements ItemizedOverlay.OnFocusChangeListener {
    private static final String TAG = "MapViewDemo";
	
	private View mPopupView;
	private MapView mMapView;
	private View mSendCommand;
	private View mWarningCommand;
	private View mTraceInfo;
	private View mTraceListButton;
	
	private Drawable mLocalMarkerImage;
	
	private WarningRegionOverlay mWarningRegionOverlay;
	private GeoPoint mCurrentFocusGeoPoint;
	private List<Overlay> mOverLays;
	private ItemizedOverlay<OverlayItem> mTraceOverlay;
	private ArrayList<WarningRegion> mWarningRegionList;
	private ArrayList<TracePointInfo> mTracePointList;
	
	private TracePointInfo mCurrentTraceInfo;
	
    private View mWarningEntryView;
    private CheckedTextView mOutWarningView;
    private CheckedTextView mInWarningView;
	
    private AlertDialog mTraceListDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);
        
        initPopupView();
        mLocalMarkerImage = getResources().getDrawable(R.drawable.local_mark);
        
        mMapView = (MapView) findViewById(R.id.map);//获得MapView对象   
        
        mMapView.addView(mPopupView, new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
                MapView.LayoutParams.WRAP_CONTENT, null, MapView.LayoutParams.BOTTOM_CENTER));
        mPopupView.setVisibility(View.GONE);
        
        mWarningRegionList = new ArrayList<WarningRegion>();
        mTracePointList = new ArrayList<TracePointInfo>();
        
        buildTracePointList();
        
        mTraceOverlay = new PopOverlay(getResources().getDrawable(R.drawable.local_mark)
                                , mTracePointList);
        mTraceOverlay.setOnFocusChangeListener(this);
        mOverLays = mMapView.getOverlays();
        mOverLays.clear();
        mOverLays.add(mTraceOverlay);
        
        //test code
//        WarningRegion wr = new WarningRegion();
//        wr.point = new GeoPoint(39971036, 116314659);
//        wr.region = 100;
//        ArrayList<WarningRegion> wrList = new ArrayList<WarningRegion>();
//        wrList.add(wr);
//        WarningRegionOverlay w = new WarningRegionOverlay(this
//                                        , getResources().getDrawable(R.drawable.local_mark)
//                                        , wrList);
//        mOverLays.add(w);
        
//    	PopOverlay overlay =  new PopOverlay(getResources().getDrawable(R.drawable.local_mark));
//    	    overlay.addOverlay(new OverlayItem(new GeoPoint(39971036,116314659), "沙河", "沙河校区"));  
//	    overlay.addOverlay(new OverlayItem(new GeoPoint(39971036,116314659), "清水河", "清水河校区"));  
	    mMapView.getController().setCenter(new GeoPoint(39971036,116314659));//设置地图中心   
        mMapView.getController().setZoom(15);//设置缩放级别  
        
        mTraceListButton = findViewById(R.id.trace_list_button);
        mTraceListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View paramView) {
//                Intent list_intent = new Intent();
//                list_intent.setClass(MapViewDemo.this, TraceInfoListActivity.class);
//                startActivity(list_intent);
                showTraceInfoListDialog();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        postRefreshOverlay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.map_menu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setIcon(R.drawable.menu_setting);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.setting:
            Intent intent = new Intent();
            intent.setClass(MapViewDemo.this, SettingActivity.class);
            startActivity(intent);
            break;
        }
        return true;
    }
    
    public void onFocusChanged(ItemizedOverlay overlay, OverlayItem newFocus) {
        if (mPopupView != null) {
            mPopupView.setVisibility(View.GONE);
        }
        if (newFocus != null) {
            String[] titleInfo = newFocus.getTitle().split(Config.SPLITOR);
            for (TracePointInfo trace : mTracePointList) {
                if (trace.id.equals(titleInfo[0])) {
                    this.mCurrentTraceInfo = trace;
                    break;
                } else {
                    mCurrentTraceInfo = null;
                }
            }
            
            MapView.LayoutParams geoLP = (MapView.LayoutParams) mPopupView.getLayoutParams();
            geoLP.point = newFocus.getPoint();
            mCurrentFocusGeoPoint = geoLP.point;
            TextView title = (TextView) mPopupView.findViewById(R.id.map_bubbleTitle);
            title.setText(String.format(getString(R.string.title_trace_point)
                                            , titleInfo[0]
                                            , titleInfo[1]));
            TextView desc = (TextView) mPopupView.findViewById(R.id.map_bubbleText);
            if (newFocus.getSnippet() == null || newFocus.getSnippet().length() == 0) {
                desc.setVisibility(View.GONE);
            } else {
                desc.setVisibility(View.VISIBLE);
                desc.setText(newFocus.getSnippet());
            }
            
            Point point = new Point();
            mMapView.getProjection().toPixels(geoLP.point, point);
            Rect markerRect = mLocalMarkerImage.getBounds();
            int imageHeight = markerRect.bottom - markerRect.top;
            LOGD("[[OnFocusChangeListener]] image height = " + imageHeight
                    + " rect for image = " + markerRect.toString());
            GeoPoint showGeoPoint = mMapView.getProjection()
                                                .fromPixels(point.x
                                                        , (point.y - 34));
            
            LOGD("old geo point = " + geoLP.point
                    + " new geo point = " + showGeoPoint);
            geoLP.point = showGeoPoint;
            mMapView.updateViewLayout(mPopupView, geoLP);
            mPopupView.setVisibility(View.VISIBLE);
        }
    }

    private void buildTracePointList() {
        //test code 
        TracePointInfo info = new TracePointInfo();
        info.geoPoint = new GeoPoint(39971036, 116314659);
        info.id = "1";
        info.title = "清水河";
        info.summary = "清水河校区";
        info.phoneNumber = "10086";
        
        mTracePointList.add(info);
        
        Environment.tracePointList.clear();
        Environment.tracePointList.addAll(mTracePointList);
    }
    
    private void initPopupView() {
        mPopupView = View.inflate(this,R.layout.pop, null);
        mSendCommand = mPopupView.findViewById(R.id.send_command);
        mWarningCommand = mPopupView.findViewById(R.id.warning);
        mTraceInfo = mPopupView.findViewById(R.id.trace_info);
        
        mSendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSingleSendCommandDialog();
            }
        });
        
        mWarningCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWarningRegionDialog();
            }
        });
        
        mTraceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View paramView) {
                showTraceInfoDialog(mCurrentTraceInfo);
            }
        });
    }
    
    private void showTraceInfoDialog(TracePointInfo info) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setTitle(R.string.title_trace_info_default)
                                .setPositiveButton(R.string.btn_locate
                                        , new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                            }
                                })
                                .setNegativeButton(R.string.btn_command
                                        , new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                showSingleSendCommandDialog();
                                            }
                                })
                                .create();
        if (info != null) {
            StringBuilder builder = new StringBuilder();
            if (info.id != null) {
                builder.append(String.format(getString(R.string.trace_info_id)
                                                , info.id));
                builder.append("\n");
            }
            if (info.phoneNumber != null) {
                builder.append(String.format(getString(R.string.trace_info_phonenumber)
                                                , info.phoneNumber));
                builder.append("\n");
            }
            if (info.geoPoint != null) {
                String location = String.valueOf((info.geoPoint.getLatitudeE6() * 1.0 / 10E6))
                                    + Config.SPLITOR
                                    + String.valueOf((info.geoPoint.getLongitudeE6() * 1.0 / 10E6));
                builder.append(String.format(getString(R.string.trace_info_point)
                                                , location));
                builder.append("\n");
            }
            builder.append(String.format(getString(R.string.trace_info_distance), "0"));
            dialog.setMessage(builder.toString());
        }
        dialog.show();
    }
    
    private void showSingleSendCommandDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.title_command_send)
                        .setSingleChoiceItems(R.array.commands
                                    , 0
                                    , new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                        })
                        .setPositiveButton(R.string.btn_send
                                    , new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                        })
                        .setNegativeButton(R.string.btn_cancel
                                    , new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                        })
                       .create();
        if (mCurrentTraceInfo != null) {
            dialog.setTitle(String.format(getString(R.string.title_send_command), mCurrentTraceInfo.id));
        }
        dialog.show();
    }
    
    private void showWarningRegionDialog() {
//        if (mWarningEntryView == null) {
            LayoutInflater factory = LayoutInflater.from(this);
            mWarningEntryView = factory.inflate(R.layout.warning_dialog, null);
            mOutWarningView = (CheckedTextView) mWarningEntryView.findViewById(R.id.out_warnging);
            mInWarningView = (CheckedTextView) mWarningEntryView.findViewById(R.id.in_warnging);
            
            mOutWarningView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View paramView) {
                    mOutWarningView.setChecked(true);
                    mInWarningView.setChecked(false);
                }
            });
            
            mInWarningView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View paramView) {
                    mOutWarningView.setChecked(false);
                    mInWarningView.setChecked(true);
                }
            });
//        }
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.title_warning_region)
            .setView(mWarningEntryView)
            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    EditText editor = (EditText) mWarningEntryView.findViewById(R.id.region_edit);
                    int distance = Integer.valueOf(editor.getText().toString());
                    for (TracePointInfo info : mTracePointList) {
                        if (info.geoPoint.getLatitudeE6() == mCurrentFocusGeoPoint.getLatitudeE6()
                                && info.geoPoint.getLongitudeE6() == mCurrentFocusGeoPoint.getLongitudeE6()) {
                            if (info.warningRegion == null) {
                                info.warningRegion = new WarningRegion();
                            }
                            info.warningRegion.point = mCurrentFocusGeoPoint;
                            info.warningRegion.region = distance;
                            
                            mWarningRegionList.remove(info.warningRegion);
                            mWarningRegionList.add(info.warningRegion);
                            break;
                        }
                    }
                    
                    if (mWarningRegionOverlay == null) {
                        mWarningRegionOverlay = new WarningRegionOverlay(MapViewDemo.this
                                                        , getResources().getDrawable(R.drawable.local_mark)
                                                        , mWarningRegionList); 
                    } else {
                        mWarningRegionOverlay.setWarningRegionList(mWarningRegionList);
                    }
                    mOverLays.add(mWarningRegionOverlay);
                    postRefreshOverlay();
                }
            })
            .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            })
            .create();
        dialog.show();
    }
    
    private void showTraceInfoListDialog() {
        mTraceListDialog = new AlertDialog.Builder(this)
                                    .setTitle(R.string.titile_trace_info_list)
                                    .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    })
                                    .create();
        ListView listView = (ListView) View.inflate(this,R.layout.trace_info_list, null);
        listView.setAdapter(new TraceInfoAdapter(this, Environment.tracePointList));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < Environment.tracePointList.size()) {
                    mTraceListDialog.dismiss();
                    TracePointInfo info = Environment.tracePointList.get(position);
                    showTraceInfoDialog(info);
                }
            }
        });
        mTraceListDialog.setView(listView);
        mTraceListDialog.show();
    }
    
    private void postRefreshOverlay() {
        mMapView.postInvalidate();
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    private static void LOGD(String msg) {
        if (Config.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
