package com.mobile.trace.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.mobile.trace.R;
import com.mobile.trace.activity.WarningRegionOverlay.WarningRegion;
import com.mobile.trace.utils.Config;
import com.mobile.trace.utils.Environment;
import com.mobile.trace.utils.SettingManager;

public class MapViewActivity extends MapActivity implements ItemizedOverlay.OnFocusChangeListener {
    private static final String TAG = "MapViewDemo";
	
	private View mPopupView;
	private View mPopupWarningView;
	private View mWarningPopupCommand;
	private MapView mMapView;
	private View mSendCommand;
	private View mWarningCommand;
	private View mTraceInfo;
	private View mTraceListButton;
	private View mWarningTips;
	private View mLogoutView;
	
	private Drawable mLocalMarkerImage;
	
	private WarningRegionOverlay mWarningRegionOverlay;
	private WarningRegion mLongPressedWarningRegion;
	private GeoPoint mCurrentFocusGeoPoint;
	private List<Overlay> mOverLays;
	private ItemizedOverlay<OverlayItem> mTraceOverlay;
	private SpecialPoinOverlay mSpecialOverlay;
	private ArrayList<WarningRegion> mWarningRegionList;
	private ArrayList<TracePointInfo> mTracePointList;
	private ArrayList<GeoPoint> mSpecialPointList;
	
	private TracePointInfo mCurrentTraceInfo;
	
    private View mWarningEntryView;
    private CheckedTextView mOutWarningView;
    private CheckedTextView mInWarningView;
	
    private View mSearchDialogView;
    
    private AlertDialog mTraceListDialog;
    
    private LinearLayout linearLayout;
    private ZoomControls zoomControls;
    private MapController mMapController;
    
    private GestureDetector mGestureDetector;
    
    private int mBackKeyPressedCount = 0;
    
    private Projection mProjection;
    private Vibrator mVibrator;
    private static final long[] mVibratePattern = {5, 30 };
    
    private float mLongPressedX;
    private float mLongPressedY;
    private float mLongPressedSqr;
    
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }
        
        @Override
        public void onProviderEnabled(String s) {
        }
        
        @Override
        public void onProviderDisabled(String s) {
        }
        
        @Override
        public void onLocationChanged(Location location) {
//            mHandler.removeMessages(LBS_TIME_OUT);
            
            mLocation = location;
            if (location != null) {
                int lat = (int) (location.getLatitude() * 1E6);
                int lon = (int) (location.getLongitude() * 1E6);
                GeoPoint point = new GeoPoint(lat, lon);
                
                if (mSpecialPointList == null) {
                    mSpecialPointList = new ArrayList<GeoPoint>();
                }
                if (mSpecialOverlay == null) {
                    mSpecialOverlay = new SpecialPoinOverlay(MapViewActivity.this
                                                , getResources().getDrawable(R.drawable.current_location)
                                                , mSpecialPointList);
                }
                mSpecialOverlay.clearOverlay();
                mSpecialOverlay.addOverlay(point);
                resetOverlay();
                postRefreshOverlay();
                sendMoveLocationMessage(point);
            } else {
                showLocationError();
            }
            removeLocationListener();
        }
    };
    
    private static final int REFRESH_MAP = 0;
    private static final int LBS_TIME_OUT = 1;
    private static final int MOVE_TO_LOCATION = 2;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case REFRESH_MAP:
                postRefreshOverlay();
                break;
            case LBS_TIME_OUT:
                removeLocationListener();
                break;
            case MOVE_TO_LOCATION:
                if (msg.obj != null) {
                    mMapController.animateTo((GeoPoint) msg.obj);
                }
                break;
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);
        
        initPopupView();
        initPopupWaringView();
        mLocalMarkerImage = getResources().getDrawable(R.drawable.local_mark);
        
        mMapView = (MapView) findViewById(R.id.map);
        mWarningTips = findViewById(R.id.warning_tips);
        mWarningTips.setVisibility(View.VISIBLE);
        mLogoutView = findViewById(R.id.logout);
        mLogoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingManager.getInstance().clearPhone();
                finish();
            }
        });
        
        initMapView();
        mWarningRegionList = new ArrayList<WarningRegion>();
        mTracePointList = new ArrayList<TracePointInfo>();
        
        buildTracePointList();
        
        mTraceOverlay = new PopOverlay(getResources().getDrawable(R.drawable.local_mark)
                                , mTracePointList);
        mTraceOverlay.setOnFocusChangeListener(this);
        mOverLays = mMapView.getOverlays();
        mOverLays.clear();
        mOverLays.add(mTraceOverlay);
        
	    mMapView.getController().setCenter(new GeoPoint(39971036,116314659));
        mMapView.getController().setZoom(Environment.MAP_ZOOM_LEVEL);  
        
        mTraceListButton = findViewById(R.id.trace_list_button);
        mTraceListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View paramView) {
                showTraceInfoListDialog();
            }
        });
        
        if (Config.ZOOM_BUTTON_SUPPROT) {
            initZoomControl();
        }
        
        locateCurrentPoint();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        postRefreshOverlay();
        mBackKeyPressedCount = 0;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mBackKeyPressedCount == 0) {
                mBackKeyPressedCount++;
                Toast.makeText(this, R.string.tips_exit, Toast.LENGTH_SHORT).show();
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.map_menu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.map_type).setIcon(android.R.drawable.ic_menu_more);
        menu.findItem(R.id.setting).setIcon(R.drawable.menu_setting);
        menu.findItem(R.id.warning_list).setIcon(android.R.drawable.ic_menu_manage);
        menu.findItem(R.id.search).setIcon(android.R.drawable.ic_menu_search);
        menu.findItem(R.id.locate).setIcon(R.drawable.my_location);
        menu.findItem(R.id.command_list).setIcon(android.R.drawable.ic_menu_manage);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.setting:
            Intent intent = new Intent();
            intent.setClass(MapViewActivity.this, SettingActivity.class);
            startActivity(intent);
            break;
        case R.id.map_type:
            showMapTypeChangeDialog();
            break;
        case R.id.warning_list:
            break;
        case R.id.search:
            showSearchDialog();
            break;
        case R.id.locate:
            locateCurrentPoint();
            break;
        case R.id.command_list:
            break;
        }
        return true;
    }
    
    private void initMapView() {
        mMapView.addView(mPopupView, new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
                MapView.LayoutParams.WRAP_CONTENT, null, MapView.LayoutParams.BOTTOM_CENTER));
        mMapView.addView(mPopupWarningView, new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
                MapView.LayoutParams.WRAP_CONTENT, null, MapView.LayoutParams.BOTTOM_CENTER));
        mPopupWarningView.setVisibility(View.GONE);
        mPopupView.setVisibility(View.GONE);
        mMapController = mMapView.getController();
        mGestureDetector = new GestureDetector(this, new OnGestureListener());
        mProjection = mMapView.getProjection();
        
        mMapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                if (mGestureDetector.onTouchEvent(ev) == true) {
                    return true;
                }
                
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    mPopupWarningView.setVisibility(View.GONE);
                }
                
                
                if (((MotionEvent.ACTION_DOWN) == ev.getAction()) && (mCurrentFocusGeoPoint != null)) {
//                    fDownX = ev.getX();
//                    fDownY = ev.getY();
//                    Projection projection = mMapView.getProjection();
//                    Point point = new Point();
//                    projection.toPixels(mCurrentFocusGeoPoint, point);
//                    focusX = point.x;
//                    focusY = point.y;
//
//                    float RawX = ev.getX();
//                    float RawY = ev.getY();
//                    float a = (RawX - focusX) * (RawX - focusX);
//                    float b = (RawY - focusY) * (RawY - focusY);
//                    float c = distance * distance;
//                    float del = c - (a + b);
//                    if ((del < 10) || (del > -10)) {
//                        mIfMove = true;
//                    }
                } else if ((MotionEvent.ACTION_MOVE == ev.getAction())
                        && mLongPressedWarningRegion != null) {
                    float fDisX = ev.getX();
                    float fDisY = ev.getY();
                    Point point = new Point();
                    mProjection.toPixels(mLongPressedWarningRegion.point, point);
                    if (pointInRound(mLongPressedX, mLongPressedY, mLongPressedSqr, fDisX, fDisY)) {
                        // small
                        float temp = (fDisX - point.x) * (fDisX - point.x) + (fDisY - point.y) * (fDisY - point.y);
                        float move = mLongPressedSqr - temp;
                        mLongPressedWarningRegion.region = (float) Math.sqrt((mLongPressedWarningRegion.regionSquare - move));
                    } else {
                        // large
                        float temp = (fDisX - point.x) * (fDisX - point.x) + (fDisY - point.y) * (fDisY - point.y);
                        float move = temp - mLongPressedSqr;
                        mLongPressedWarningRegion.region = (float) Math.sqrt((mLongPressedWarningRegion.regionSquare + move));
                    }

                    if (mWarningRegionOverlay == null) {
                        mWarningRegionOverlay = new WarningRegionOverlay(MapViewActivity.this
                                                        , getResources().getDrawable(R.drawable.local_mark)
                                                        , mWarningRegionList);
                    } else {
                        mWarningRegionOverlay.setWarningRegionList(mWarningRegionList);
                    }

                    mOverLays.clear();
                    mOverLays.add(mWarningRegionOverlay);
                    postRefreshOverlay();
                    return true;
                } else if (MotionEvent.ACTION_UP == ev.getAction()
                        && mLongPressedWarningRegion != null) {
                    mLongPressedWarningRegion.regionSquare = mLongPressedWarningRegion.region * mLongPressedWarningRegion.region;
                    mLongPressedWarningRegion = null;
                }
                
                return false;
            }
        });
    }
    
    private double recountRegion(float fDisX, float fDisY){
        return Math.sqrt(fDisX + fDisY);
    }
    
    private void showSearchDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        mSearchDialogView = factory.inflate(R.layout.search_dialog, null);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle(R.string.title_search_dialog)
        .setView(mSearchDialogView)
        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                EditText lat = (EditText) mSearchDialogView.findViewById(R.id.lat_edit);
                String latStr = lat.getEditableText().toString();
                EditText lon = (EditText) mSearchDialogView.findViewById(R.id.lon_edit);
                String lonStr = lon.getEditableText().toString();
                if (!TextUtils.isEmpty(lonStr) && !TextUtils.isEmpty(latStr)) {
                    int latInt = (int) (Double.valueOf(latStr) * 1E6);
                    int lonInt = (int) (Double.valueOf(lonStr) * 1E6);
                    GeoPoint point = new GeoPoint(latInt, lonInt);
                    mMapController.animateTo(point);
                }
            }
        })
        .create();
        dialog.show();
    }
    
    private void locateCurrentPoint() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        if (mLocationManager == null) {
            showLocationError();
            return;
        }
        
        if (!mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && 
                !mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showOpenGpsDialog();
            return;
        }
        
        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
                    0, 0, mLocationListener);
        } else if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
                    0, 0, mLocationListener);
        } else {
            showLocationError();
            return;
        }
        
        mHandler.sendEmptyMessageDelayed(LBS_TIME_OUT, Config.LOCATION_TIMEOUT);
    }
    
    private void removeLocationListener() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
        mHandler.removeMessages(LBS_TIME_OUT);
    }
    
    private void showOpenGpsDialog() {
        new AlertDialog.Builder(this)
            .setMessage(R.string.open_gps_msg)
            .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            })
            .setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        try {
                            intent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(intent);
                        } catch (ActivityNotFoundException e1) {
                        }
                    }
                    
                }
            })
            .show();
    }
    
    private void showLocationError() {
        new AlertDialog.Builder(this)
            .setMessage(R.string.location_error)
            .setPositiveButton(R.string.btn_ok, null)
            .show();
    }
    
    private void showMapTypeChangeDialog() {
        Dialog typeDialog = new AlertDialog.Builder(this)
                                .setTitle(R.string.map_type)
                                .setItems(R.array.maptype, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                        case 0:
                                            mMapView.setSatellite(false);
                                            break;
                                        case 1:
                                            mMapView.setSatellite(true);
                                            break;
                                        }
                                    }
                                }).create();
        typeDialog.show();
    }
    
    private void initZoomControl() {
        linearLayout = (LinearLayout) findViewById(R.id.zoomview);
        zoomControls = (ZoomControls) mMapView.getZoomControls();
        linearLayout.addView(zoomControls);
        zoomControls.setOnZoomInClickListener(new OnClickListener() {
            public void onClick(View v) {
                Environment.MAP_ZOOM_LEVEL = Math.min(Environment.MAP_ZOOM_LEVEL + 1
                                                    , Environment.MAX_MAP_ZOOM_LEVEL);
                mMapController.setZoom(Environment.MAP_ZOOM_LEVEL);
                LOGD("reload overlay because the zoom in action");
                mHandler.sendEmptyMessage(REFRESH_MAP);
            }
        });

        zoomControls.setOnZoomOutClickListener(new OnClickListener() {
            public void onClick(View v) {
                Environment.MAP_ZOOM_LEVEL = Math.max(Environment.MAP_ZOOM_LEVEL - 1
                                                    , Environment.MIN_MAP_ZOOM_LEVEL);
                mMapController.setZoom(Environment.MAP_ZOOM_LEVEL);
                mHandler.sendEmptyMessage(REFRESH_MAP);
            }
        });
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
            mPopupWarningView.setVisibility(View.GONE);
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
    
    private void initPopupWaringView() {
        mPopupWarningView = View.inflate(this, R.layout.popup_warning, null);
        mWarningPopupCommand = mPopupWarningView.findViewById(R.id.warning);
        mWarningPopupCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWarningRegionDialog(false);
            }
        });
    }
    
    private void initPopupView() {
        mPopupView = View.inflate(this, R.layout.pop, null);
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
                showWarningRegionDialog(true);
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
        mCurrentFocusGeoPoint = info.geoPoint;
        AlertDialog dialog = new AlertDialog.Builder(this)
                                .setTitle(R.string.title_trace_info_default)
                                .setPositiveButton(R.string.btn_locate
                                        , new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                if (mCurrentFocusGeoPoint != null) {
                                                    mMapController.animateTo(mCurrentFocusGeoPoint);
                                                }
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
    
    private void showWarningRegionDialog(boolean hideOption) {
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
        
        if (hideOption) {
            mOutWarningView.setVisibility(View.GONE);
            mInWarningView.setVisibility(View.GONE);
        }
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.title_warning_region)
            .setView(mWarningEntryView)
            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    EditText editor = (EditText) mWarningEntryView.findViewById(R.id.region_edit);
                    int distance = Integer.valueOf(editor.getText().toString());
                    
                    WarningRegion warning = null;
                    if (mCurrentTraceInfo != null) {
                        for (WarningRegion region : mWarningRegionList) {
                            if (region.tracePointId == Integer.valueOf(mCurrentTraceInfo.id)) {
                                warning = region;
                            }
                        }
                        if (warning == null) {
                            warning = new WarningRegion();
                            warning.point = mCurrentTraceInfo.geoPoint;
                            mWarningRegionList.add(warning);
                        }
                    } else if (mCurrentFocusGeoPoint != null) {
                        for (WarningRegion region : mWarningRegionList) {
                            if (region.point.getLatitudeE6() == mCurrentFocusGeoPoint.getLatitudeE6()
                                    && region.point.getLongitudeE6() == mCurrentFocusGeoPoint.getLongitudeE6()) {
                                warning = region;
                            }
                        }
                        if (warning == null) {
                            warning = new WarningRegion();
                            warning.tracePointId = -1;
                            warning.point = mCurrentFocusGeoPoint;
                            mWarningRegionList.add(warning);
                        }
                    } else {
                        return;
                    }
                    
                    warning.region = mProjection.metersToEquatorPixels((float) distance * 1000);
                    warning.regionSquare = warning.region * warning.region;
                    
                    LOGD("[[showWarningRegionDialog::onClick]] waring info = " + warning.toString());
                    
                    if (mWarningRegionOverlay == null) {
                        mWarningRegionOverlay = new WarningRegionOverlay(MapViewActivity.this
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
        mBackKeyPressedCount = 0;
    }
    
    private void resetOverlay() {
        synchronized (mOverLays) {
            mOverLays.clear();
            mOverLays.add(mTraceOverlay);
            if (mWarningRegionOverlay != null) {
                mOverLays.add(mWarningRegionOverlay);
            }
            if (mSpecialOverlay != null) {
                mOverLays.add(mSpecialOverlay);
            }
        }
    }
    
    private void sendMoveLocationMessage(GeoPoint point) {
        Message msg = new Message();
        msg.obj = point;
        msg.what = MOVE_TO_LOCATION;
        mHandler.sendMessage(msg);
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    private class OnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            
            if (mWarningRegionList != null && mWarningRegionList.size() > 0) {
                Point point = new Point();
                for (WarningRegion region : mWarningRegionList) {
                    mProjection.toPixels(region.point, point);
                    float pointDist = ((x - point.x) * (x - point.x)) + ((y - point.y) * (y - point.y));
                    if (pointDist < region.regionSquare) {
                        //long pressed in one warning region
                        vibrateNow();
                        mLongPressedWarningRegion = region;
                        mLongPressedX = event.getX();
                        mLongPressedY = event.getY();      
                        mLongPressedSqr = pointDist;
                        return;
                    }
                }
            }
            
            Projection projection = mMapView.getProjection();
            mCurrentFocusGeoPoint = projection.fromPixels((int) x, (int) y);
            mCurrentTraceInfo = null;
            
            mPopupView.setVisibility(View.GONE);
            mPopupWarningView.setVisibility(View.VISIBLE);
            TextView title = (TextView) mPopupWarningView.findViewById(R.id.map_bubbleTitle);
            title.setText(String.format(getString(R.string.title_warning_point)
                                            , (mCurrentFocusGeoPoint.getLatitudeE6() * 1.0) / 10E6
                                            , (mCurrentFocusGeoPoint.getLongitudeE6() * 1.0) / 10E6));
            MapView.LayoutParams geoLP = (MapView.LayoutParams) mPopupWarningView.getLayoutParams();
            geoLP.point = mMapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
            mMapView.updateViewLayout(mPopupWarningView, geoLP);
        }
    }
    
    public void vibrateNow() {
        if (mVibrator == null) {
            mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        mVibrator.vibrate(mVibratePattern, -1);
    }
    
    private boolean pointInRound(float srcX, float srcY, float distanceSqr, float x , float y) {
        float pointDist = ((x - srcX) * (x - srcX)) + ((y - srcY) * (y - srcY));
        return distanceSqr > pointDist;
    }
    
    private static void LOGD(String msg) {
        if (Config.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
