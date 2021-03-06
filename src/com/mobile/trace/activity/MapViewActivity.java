package com.mobile.trace.activity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.mobile.trace.model.CommandModel;
import com.mobile.trace.model.CommandModel.CommandItem;
import com.mobile.trace.model.TraceDeviceInfoModel;
import com.mobile.trace.utils.Config;
import com.mobile.trace.utils.EncryptUtils;
import com.mobile.trace.utils.Environment;
import com.mobile.trace.utils.InternetUtils;
import com.mobile.trace.utils.NotifyUtils;
import com.mobile.trace.utils.SettingManager;

public class MapViewActivity extends MapActivity implements ItemizedOverlay.OnFocusChangeListener {
    private static final String TAG = "MapViewDemo";
	
    private class TraceInfoItem {
        TracePointInfo traceInfo;
        View tipsView;
    }
    
	private View mPopupView;
	private View mPopupWarningView;
	private View mWarningInfoPopupView;
	
	private View mWarningPopupCommand;
	private MapView mMapView;
	private View mSendCommandButton;
	private View mTraceInfo;
	private View mTraceListButton;
	private View mWarningTips;
	private TextView mWarningTV;
	private View mLogoutView;
	
	private Drawable mLocalMarkerImage;
	
	private WarningRegionOverlay mWarningRegionOverlay;
	private WarningRegion mLongPressedWarningRegion;
	private WarningRegion mLongPressedWarningRegionRetain;
	private GeoPoint mCurrentFocusGeoPoint;
	private List<Overlay> mOverLays;
	private PopOverlay mTraceOverlay;
	private SpecialPoinOverlay mSpecialOverlay;
	private ArrayList<WarningRegion> mWarningRegionList;
	private ArrayList<GeoPoint> mSpecialPointList;
	
	private ArrayList<TraceInfoItem> mTraceInfoItemList = new ArrayList<TraceInfoItem>();
	
	private TracePointInfo mCurrentTraceInfo;
	
    private View mWarningEntryView;
    private CheckedTextView mOutWarningView;
    private CheckedTextView mInWarningView;
    private View mTraceListSelectedView;
    private String mTraceSelectedId;
    
    private View mSearchDialogView;
    
    private AlertDialog mTraceListDialog;
    
    private LinearLayout linearLayout;
    private ZoomControls zoomControls;
    private MapController mMapController;
    
    private GestureDetector mGestureDetector;
    
    private GeoPoint mCurrentGeoPoint;
    
    private int mBackKeyPressedCount = 0;
    
    private Projection mProjection;
    private Vibrator mVibrator;
    private static final long[] mVibratePattern = {5, 30 };
    
    private float mLongPressedX;
    private float mLongPressedY;
    private float mLongPressedSqr;
    
    private float mMetersToPixel;
    private Timer mTraceInfoTimer;
    private int mRefreshRate;
    private float mBaseRegionPixel = -1;
    private String mSendCommandContext;
    private byte[] mSendCommandByte;
    
    private NotifyUtils mNotifyUtils;
    
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
                mCurrentGeoPoint = new GeoPoint(lat, lon);
                
                if (mSpecialPointList == null) {
                    mSpecialPointList = new ArrayList<GeoPoint>();
                }
                if (mSpecialOverlay == null) {
                    mSpecialOverlay = new SpecialPoinOverlay(MapViewActivity.this
                                                , getResources().getDrawable(R.drawable.current_location)
                                                , mSpecialPointList);
                }
                mSpecialOverlay.clearOverlay();
                mSpecialOverlay.addOverlay(mCurrentGeoPoint);
                resetOverlay();
                postRefreshOverlay();
                sendMoveLocationMessage(mCurrentGeoPoint);
            } else {
                showLocationError();
            }
//            removeLocationListener();
        }
    };
    
    private String mRemoteWaringInfo;
    public static final String SERVER_SMS_RECEIVED = "com.mobile.trace.reveivesms";

    private class SMSBroadcatReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            LOGD("[[onReceive]] action = " + intent.getAction());
            if (intent != null && intent.getAction().equals(SERVER_SMS_RECEIVED)) {
                String content = intent.getStringExtra("content");
                // save test warning data
                WarningRegion warning = new WarningRegion();
                warning.region = 1;
                warning.point = new GeoPoint(10000, 10000);
                warning.regionPixel = 2;
                warning.regionSquare = 4;
                warning.tracePointId = "1";
                warning.warningRemoteLocalType = WarningRegion.WARNING_TYPE_REMOTE;
                warning.warningType = WarningRegion.WARNING_TYPE_IN;
                warning.time = System.currentTimeMillis();

                TraceDeviceInfoModel.getInstance().addRemoteWarningRegion(warning);
                mRemoteWaringInfo = content;

                LOGD("[[onRecive]] before play sound >>>>>>>>>");

                mNotifyUtils.playRingtone();
                mNotifyUtils.vibrateNow();
                if (mWarningTips.getVisibility() == View.GONE) {
                    mWarningTips.setVisibility(View.VISIBLE);
                    mWarningTips.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intentWarning = new Intent();
                            intentWarning.setClass(MapViewActivity.this, WarningListActivity.class);
                            intentWarning.putExtra(WarningViewActivity.TRACE_POINT_WARNING, true);
                            startActivityForResult(intentWarning, Config.WARNING_LOCATE_REQUEST);
                        }
                    });
                }
            }
        }
    };
    private SMSBroadcatReceiver mSMSReceiver = new SMSBroadcatReceiver();
    
    private static final int REFRESH_MAP = 0;
    private static final int LBS_TIME_OUT = 1;
    private static final int MOVE_TO_LOCATION = 2;
    private static final int SHOW_WARNINGINFO_POPUP = 3;
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
            case SHOW_WARNINGINFO_POPUP:
                updateWarningInfoPopup((WarningRegion) msg.obj);
                break;
            case Config.DEVICE_INFOS:
                if (msg.obj != null) {
                    LOGD("[[Config.DEVICE_INFOS]]");
//                    updateTraceInfoItemPostion();
                    resetOverlay();
                    postRefreshOverlay();
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
        initWarningInfoPopupView();
        mLocalMarkerImage = getResources().getDrawable(R.drawable.local_mark);
        
        mMapView = (MapView) findViewById(R.id.map);
        mWarningTips = findViewById(R.id.warning_tips);
        mWarningTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWarningTV.getVisibility() == View.GONE && !TextUtils.isEmpty(mRemoteWaringInfo)) {
                    mWarningTV.setText(mRemoteWaringInfo);
                    mWarningTV.setVisibility(View.VISIBLE);
                } else {
                    mWarningTV.setVisibility(View.GONE);
                    mWarningTips.setVisibility(View.GONE);
                    mRemoteWaringInfo = null;
                }
            }
        });
        mWarningTV = (TextView) findViewById(R.id.warning_tips_tv);
        mLogoutView = findViewById(R.id.logout);
        mLogoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingManager.getInstance().clearPhone();
                finish();
            }
        });
        
        initMapView();
        buildTracePointList();
//        updateTraceInfoItemPostion();
        
        mTraceOverlay = new PopOverlay(this, getResources().getDrawable(R.drawable.local_mark)
                                , getResources().getDrawable(R.drawable.tarce_info_tips)
                                , TraceDeviceInfoModel.getInstance().getTracePointInfo());
        mTraceOverlay.setOnFocusChangeListener(this);
        mOverLays = mMapView.getOverlays();
        mOverLays.clear();
        mOverLays.add(mTraceOverlay);
        
        mMapView.getController().setCenter(new GeoPoint(39971036, 116314659));
        mMapView.getController().setZoom(Environment.MAP_INIT_ZOOM_LEVEL);  
        
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
        
        TraceDeviceInfoModel.getInstance().getDeviceInfosObserver().addObserver(mHandler);
        mRefreshRate = Integer.valueOf(SettingManager.getInstance().getRefreshRate());
        this.mTraceInfoTimer = new Timer();
        mTraceInfoTimer.schedule(new TraceInfoTimerTask(), 5 * 1000, mRefreshRate * 60 * 1000);
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(SERVER_SMS_RECEIVED);
        this.registerReceiver(mSMSReceiver, filter);
        
        mNotifyUtils = new NotifyUtils(this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        LOGD("[[onResume]]");
        
        mMetersToPixel = mProjection.metersToEquatorPixels((float) (1.0 * 1000));
        
        initWarningRegionList();        
        resetOverlay();
        postRefreshOverlay();
        mBackKeyPressedCount = 0;
        
        int curRate = Integer.valueOf(SettingManager.getInstance().getRefreshRate());
        LOGD("[[onResume]] curRate = " + curRate + " mRefreshRate = " + mRefreshRate);
        if (mRefreshRate != curRate) {
            if (mTraceInfoTimer != null) {
                mTraceInfoTimer.cancel();
                mTraceInfoTimer = null;
            }
            mRefreshRate = curRate;
            this.mTraceInfoTimer = new Timer();
            LOGD("[[onResume]] start timer withd mRefreshRate = " + mRefreshRate);
            mTraceInfoTimer.schedule(new TraceInfoTimerTask(), 5 * 1000, mRefreshRate * 60 * 1000);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
//        for (WarningRegion region : mWarningRegionList) {
//            DatabaseOperator.getInstance().saveWarningInfo(region);
//        }
        this.unregisterReceiver(mSMSReceiver);
        
        removeLocationListener();
        TraceDeviceInfoModel.getInstance().getDeviceInfosObserver().removeObserver(mHandler);
        if (mTraceInfoTimer != null) {
            mTraceInfoTimer.cancel();
            mTraceInfoTimer = null;
        }
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
        menu.findItem(R.id.about).setIcon(android.R.drawable.ic_menu_help);
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
            Intent intentWarning = new Intent();
            intentWarning.setClass(MapViewActivity.this, WarningViewActivity.class);
            intentWarning.putExtra(WarningViewActivity.ACTION_TYPE, WarningViewActivity.WARNING_TYPE);
            startActivityForResult(intentWarning, Config.WARNING_LOCATE_REQUEST);
            break;
        case R.id.search:
//            serviceTestCode();
            showSearchDialog();
            break;
        case R.id.locate:
            locateCurrentPoint();
            break;
        case R.id.command_list:
            Intent traceIntent = new Intent();
            traceIntent.setClass(MapViewActivity.this, WarningViewActivity.class);
            traceIntent.putExtra(WarningViewActivity.ACTION_TYPE, WarningViewActivity.TRACE_TYPE);
            startActivityForResult(traceIntent, Config.WARNING_LOCATE_REQUEST);
            break;
        case R.id.about:
            showAboutDialog();
            break;
        case R.id.logout:
            SettingManager.getInstance().clearPhone();
            finish();
            break;
        }
        return true;
    }
    
    private void showAboutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                                    .setTitle(R.string.about)
                                    .setMessage(R.string.about_text)
                                    .create();
        dialog.show();
    }
    
    private void initWarningRegionList() {
//        mWarningRegionList = DatabaseOperator.getInstance().queryWarningInfoList(-1);
        mWarningRegionList = TraceDeviceInfoModel.getInstance().getLocalWarninRegion();
        for (WarningRegion region : mWarningRegionList) {
            region.regionPixel = mProjection.metersToEquatorPixels((float) region.region * 1000);
            region.regionSquare = region.regionPixel * region.regionPixel;
        }
    }
    
    private void initMapView() {
        mMapView.addView(mPopupView, new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
                MapView.LayoutParams.WRAP_CONTENT, null, MapView.LayoutParams.BOTTOM_CENTER));
        mMapView.addView(mPopupWarningView, new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
                MapView.LayoutParams.WRAP_CONTENT, null, MapView.LayoutParams.BOTTOM_CENTER));
        mMapView.addView(mWarningInfoPopupView, new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
                MapView.LayoutParams.WRAP_CONTENT, null, MapView.LayoutParams.BOTTOM_CENTER));
        mPopupWarningView.setVisibility(View.GONE);
        mPopupView.setVisibility(View.GONE);
        mWarningInfoPopupView.setVisibility(View.GONE);
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
                    mWarningInfoPopupView.setVisibility(View.GONE);
//                    mLongPressedWarningRegionRetain = null;
                }
                
                if (((MotionEvent.ACTION_DOWN) == ev.getAction()) && (mCurrentFocusGeoPoint != null)) {
                    
                } else if ((MotionEvent.ACTION_MOVE == ev.getAction())
                        && mLongPressedWarningRegion != null) {
                    float fDisX = ev.getX();
                    float fDisY = ev.getY();
                    Point point = new Point();
                    mProjection.toPixels(mLongPressedWarningRegion.point, point);
                    if (mBaseRegionPixel < 0) {
                        mBaseRegionPixel = mLongPressedWarningRegion.regionPixel;
                    } else {
                        mLongPressedWarningRegion.regionPixel = mBaseRegionPixel;
                    }
                    if (pointInRound(mLongPressedX, mLongPressedY, mLongPressedSqr, fDisX, fDisY)) {
                        // small
                        float temp = (fDisX - point.x) * (fDisX - point.x) + (fDisY - point.y) * (fDisY - point.y);
                        float move = (float) Math.sqrt(mLongPressedSqr) - (float) Math.sqrt(temp);
                        mLongPressedWarningRegion.regionPixel = mLongPressedWarningRegion.regionPixel - move;
                        LOGD("small the warning region, temp = " + temp + " move = " + move);
//                        if (mLongPressedWarningRegion.regionPixel < 200) {
//                            mLongPressedWarningRegion.regionPixel = (float) 200;
//                        }
                    } else {
                        // large
                        float temp = (fDisX - point.x) * (fDisX - point.x) + (fDisY - point.y) * (fDisY - point.y);
                        float move = (float) Math.sqrt(temp) - (float) Math.sqrt(mLongPressedSqr);
                        LOGD("large the warning region, temp = " + temp + " move = " + move);
                        mLongPressedWarningRegion.regionPixel = mLongPressedWarningRegion.regionPixel + move;
                    }

                    if (mWarningRegionOverlay == null) {
                        mWarningRegionOverlay = new WarningRegionOverlay(MapViewActivity.this
                                                        , getResources().getDrawable(R.drawable.local_mark)
                                                        , mWarningRegionList);
                    } else {
                        mWarningRegionOverlay.setWarningRegionList(mWarningRegionList);
                    }

                    mLongPressedWarningRegion.region = mLongPressedWarningRegion.regionPixel / mMetersToPixel;
                    
                    LOGD("[[OnTouch]] long pressed region pixel = " + mLongPressedWarningRegion.regionPixel
                            + " meter to pixel = " + mMetersToPixel
                            + " region = " + mLongPressedWarningRegion.region
                            + " long pressed Sqr = " + mLongPressedSqr);
                    sendMessage(SHOW_WARNINGINFO_POPUP, mLongPressedWarningRegion);
                    resetOverlay();
                    postRefreshOverlay();
                    return true;
                } else if (MotionEvent.ACTION_UP == ev.getAction()
                        && mLongPressedWarningRegion != null) {
                    mLongPressedWarningRegion.region = mLongPressedWarningRegion.regionPixel / mMetersToPixel;
                    sendMessage(SHOW_WARNINGINFO_POPUP, mLongPressedWarningRegion);
                    mLongPressedWarningRegion.regionSquare = mLongPressedWarningRegion.regionPixel * mLongPressedWarningRegion.regionPixel;
                    mLongPressedWarningRegion = null;
//                    for (WarningRegion region : mWarningRegionList) {
//                        DatabaseOperator.getInstance().saveWarningInfo(region);
//                    }
                    TraceDeviceInfoModel.getInstance().flushWarningRegion();
                }
                
                return false;
            }
        });
    }
    
    private class TraceInfoTimerTask extends TimerTask {
        public void run() {
            TraceDeviceInfoModel.getInstance().fetchTraceInfoFromServer();
        }
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
        
//        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
//                    2000, 0, mLocationListener);
//        } else if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
//                    0, 0, mLocationListener);
//        } else {
//            showLocationError();
//            return;
//        }
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
              2000, 0, mLocationListener);
        } else {
            showLocationError();
            return;
        }
        
//        mHandler.sendEmptyMessageDelayed(LBS_TIME_OUT, Config.LOCATION_TIMEOUT);
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
                Environment.MAP_ZOOM_LEVEL = mMapView.getZoomLevel();
                Environment.MAP_ZOOM_LEVEL = Environment.MAP_ZOOM_LEVEL + 1;
                mMapController.setZoom(Environment.MAP_ZOOM_LEVEL);
                LOGD("reload overlay because the zoom in action");
                mMetersToPixel = mProjection.metersToEquatorPixels(1000);
                
                for (WarningRegion region : mWarningRegionList) {
                    region.regionPixel = mProjection.metersToEquatorPixels((float) region.region * 1000);
                    region.regionSquare = region.regionPixel * region.regionPixel;
                }
                mHandler.sendEmptyMessage(REFRESH_MAP);
            }
        });

        zoomControls.setOnZoomOutClickListener(new OnClickListener() {
            public void onClick(View v) {
                Environment.MAP_ZOOM_LEVEL = mMapView.getZoomLevel();
                Environment.MAP_ZOOM_LEVEL = Environment.MAP_ZOOM_LEVEL - 1;
                mMapController.setZoom(Environment.MAP_ZOOM_LEVEL);
                mMetersToPixel = mProjection.metersToEquatorPixels(1000);
                
                for (WarningRegion region : mWarningRegionList) {
                    region.regionPixel = mProjection.metersToEquatorPixels((float) region.region * 1000);
                    region.regionSquare = region.regionPixel * region.regionPixel;
                }
                mHandler.sendEmptyMessage(REFRESH_MAP);
            }
        });
    }
    
    @Override
    public void onFocusChanged(ItemizedOverlay overlay, OverlayItem newFocus) {
        LOGD("[[onFocusChanged]]");
        if (mPopupView != null) {
            mPopupView.setVisibility(View.GONE);
        }
        if (newFocus != null) {
            String[] titleInfo = newFocus.getTitle().split(Config.SPLITOR);
            for (TracePointInfo trace : TraceDeviceInfoModel.getInstance().getTracePointInfo()) {
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
            desc.setVisibility(View.GONE);
//            if (newFocus.getSnippet() == null || newFocus.getSnippet().length() == 0) {
//                desc.setVisibility(View.GONE);
//            } else {
//                desc.setVisibility(View.VISIBLE);
//                desc.setText(newFocus.getSnippet());
//            }
            
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
            mWarningInfoPopupView.setVisibility(View.GONE);
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
        
        TracePointInfo infoa = new TracePointInfo();
        infoa.geoPoint = new GeoPoint(39979036, 116318659);
        infoa.id = "2";
        infoa.title = "aaa";
        infoa.summary = "aaaaaa";
        infoa.phoneNumber = "10086a";
        
        TraceDeviceInfoModel.getInstance().addTracePointInfo(info);
        TraceDeviceInfoModel.getInstance().addTracePointInfo(infoa);
    }
    
//    private void updateTraceInfoItemPostion() {
//        for (TraceInfoItem item : mTraceInfoItemList) {
//            mMapView.removeView(item.tipsView);
//        }
//        mTraceInfoItemList.clear();
//        for (TracePointInfo info : StaticDataModel.tracePointList) {
//            TraceInfoItem item = new TraceInfoItem();
//            item.traceInfo = info;
//            item.tipsView = View.inflate(this, R.layout.trace_info_tips_popup, null);
//            ((TextView) item.tipsView.findViewById(R.id.phone)).setText(info.phoneNumber);
//            
//            mTraceInfoItemList.add(item);
//            
//            mMapView.addView(item.tipsView, new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
//                    MapView.LayoutParams.WRAP_CONTENT
//                    , null
//                    , MapView.LayoutParams.RIGHT | MapView.LayoutParams.BOTTOM));
//        }
//    }
    
//    private void showTraceInfoItem() {
//        for (TraceInfoItem item : mTraceInfoItemList) {
//            MapView.LayoutParams geoLP = (MapView.LayoutParams) item.tipsView.getLayoutParams();
//            geoLP.point = item.traceInfo.geoPoint;
//            LOGD("[[showTraceInfoItem]] show point = " + geoLP.point);
//            mMapView.updateViewLayout(item.tipsView, geoLP);
//        }
//    }
    
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
    
    private void initWarningInfoPopupView() {
        mWarningInfoPopupView = View.inflate(this, R.layout.warning_popup, null);
        View remove = mWarningInfoPopupView.findViewById(R.id.remove);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLongPressedWarningRegionRetain != null) {
                    TraceDeviceInfoModel.getInstance().removeLocalWarningRegion(mLongPressedWarningRegionRetain);
                }
                mWarningRegionList = TraceDeviceInfoModel.getInstance().getLocalWarninRegion();
                resetOverlay();
                postRefreshOverlay();
                mWarningInfoPopupView.setVisibility(View.GONE);
            }
        });
    }
    
    private void initPopupView() {
        mPopupView = View.inflate(this, R.layout.pop, null);
        mSendCommandButton = mPopupView.findViewById(R.id.send_command);
        View mWarningButon = mPopupView.findViewById(R.id.warning);
        mTraceInfo = mPopupView.findViewById(R.id.trace_info);
        
        mSendCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSingleSendCommandDialog();
            }
        });
        
        mWarningButon.setOnClickListener(new View.OnClickListener() {
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
                String location = String.valueOf((info.geoPoint.getLatitudeE6() * 1.0 / 1E6))
                                    + Config.SPLITOR
                                    + String.valueOf((info.geoPoint.getLongitudeE6() * 1.0 / 1E6));
                builder.append(String.format(getString(R.string.trace_info_point)
                                                , location));
                builder.append("\n");
            }
            
            double distance = 0.0;
            if (mCurrentGeoPoint != null) {
                distance = this.getDistance((mCurrentGeoPoint.getLatitudeE6() * 1.0) / 1E6
                        , (mCurrentGeoPoint.getLongitudeE6() * 1.0) / 1E6
                        , (info.geoPoint.getLatitudeE6() * 1.0) / 1E6
                        , (info.geoPoint.getLongitudeE6() * 1.0) / 1E6);
            }
            builder.append(String.format(getString(R.string.trace_info_distance), String.valueOf(distance)));
            dialog.setMessage(builder.toString());
        }
        dialog.show();
    }
    
    private void showSingleSendCommandDialog() {
        String[] commands = getResources().getStringArray(R.array.commands);
        mSendCommandContext = commands[0];
        mSendCommandByte = null;
        AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.title_command_send)
                        .setSingleChoiceItems(R.array.commands
                                    , 0
                                    , new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            String[] commands = getResources().getStringArray(R.array.commands);
                                            mSendCommandContext = commands[whichButton];
                                            mSendCommandByte = getCommandByte(whichButton);
                                        }
                        })
                        .setPositiveButton(R.string.btn_send
                                    , new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            CommandItem item = new CommandItem();
                                            item.traceId = mCurrentTraceInfo.id;
                                            item.command = mSendCommandContext;
                                            item.time = String.valueOf(System.currentTimeMillis());
                                            if (mSendCommandByte != null) {
                                                item.command_byte = mSendCommandByte;
                                            }
                                            CommandModel.getInstance().addOneComamndItem(item);
                                            mSendCommandContext = null;
                                            mSendCommandByte = null;
                                            
                                            Toast.makeText(MapViewActivity.this, getString(R.string.command_send_success), Toast.LENGTH_SHORT).show();
                                        }
                        })
                        .setNegativeButton(R.string.btn_cancel
                                    , new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            mSendCommandContext = null;
                                        }
                        })
                       .create();
        if (mCurrentTraceInfo != null) {
            dialog.setTitle(String.format(getString(R.string.title_send_command), mCurrentTraceInfo.id));
        }
        dialog.show();
    }
    
    private byte[] getCommandByte(int index) {
        switch (index) {
        case 0:
            return Config.COMMAND_GPS;
        case 1:
            return Config.COMMAND_NETWORK;
        case 2:
            return Config.COMMAND_CLOSE_UPDATE;
        case 3:
            return Config.COMMAND_UPDATE_NOW;
        case 4:
            return Config.COMMAND_UPDATE_DELAY;
        }
        
        return null;
    }
    
    private void showWarningRegionDialog(final boolean hideOption) {
        LayoutInflater factory = LayoutInflater.from(this);
        mWarningEntryView = factory.inflate(R.layout.warning_dialog, null);
        mOutWarningView = (CheckedTextView) mWarningEntryView.findViewById(R.id.out_warnging);
        mInWarningView = (CheckedTextView) mWarningEntryView.findViewById(R.id.in_warnging);
        mTraceListSelectedView = mWarningEntryView.findViewById(R.id.trace_selected);
        mTraceListSelectedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View paramView) {
                int size = TraceDeviceInfoModel.getInstance().getTracePointInfo().size();
                String[] items = new String[size];
                for (int index = 0; index < size; index++) {
                    items[index] = "被控终端 : " +
                            TraceDeviceInfoModel.getInstance().getTracePointInfo().get(index).id;
                    LOGD("[[showWarningRegionDialog]] item info = " + items[index]);
                }

                AlertDialog dialog = new AlertDialog.Builder(MapViewActivity.this)
                                .setTitle(getString(R.string.title_trace_dialog))
                                .setItems(items, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (TraceDeviceInfoModel.getInstance().getTracePointInfo().size() > which) {
                                            mTraceSelectedId = TraceDeviceInfoModel.getInstance().getTracePointInfo().get(which).id;
                                        } else {
                                            mTraceSelectedId = null;
                                        }
                                    }
                                })
                                .create();
                dialog.show();
            }
        });

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
            mTraceListSelectedView.setVisibility(View.GONE);
        }
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.title_warning_region)
            .setView(mWarningEntryView)
            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    EditText editor = (EditText) mWarningEntryView.findViewById(R.id.region_edit);
                    if (TextUtils.isEmpty(editor.getText().toString())) {
                        Toast.makeText(MapViewActivity.this, R.string.waring_region_empty, Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    int distance = Integer.valueOf(editor.getText().toString());
                    if (distance < 0) {
                        Toast.makeText(MapViewActivity.this, R.string.waring_region_error, Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    if (!hideOption) {
                        if (!mOutWarningView.isChecked() && !mInWarningView.isChecked()) {
                            Toast.makeText(MapViewActivity.this, R.string.warning_in_out_select_error, Toast.LENGTH_LONG).show();
                            return;
                        }
    
                        if (mTraceSelectedId == null) {
                            Toast.makeText(MapViewActivity.this, R.string.warning_region_trace_error, Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    
                    WarningRegion warning = null;
                    if (mCurrentTraceInfo != null) {
                        for (WarningRegion region : mWarningRegionList) {
                            if (region.tracePointId != null 
                                    && region.tracePointId.equals(mCurrentTraceInfo.id)
                                    && region.warningRemoteLocalType == WarningRegion.WARNING_TYPE_REMOTE) {
                                warning = region;
                            }
                        }
                        if (warning == null) {
                            warning = new WarningRegion();
                            warning.point = mCurrentTraceInfo.geoPoint;
                            warning.tracePointId = mCurrentTraceInfo.id;
                            warning.warningRemoteLocalType = WarningRegion.WARNING_TYPE_REMOTE;
                            mWarningRegionList.add(warning);
                        }
                    } else if (mCurrentFocusGeoPoint != null) {
                        for (WarningRegion region : mWarningRegionList) {
                            if (region.point.getLatitudeE6() == mCurrentFocusGeoPoint.getLatitudeE6()
                                    && region.point.getLongitudeE6() == mCurrentFocusGeoPoint.getLongitudeE6()) {
                                warning = region;
                            }
                        }
                        if (warning == null && mTraceSelectedId != null) {
                            warning = new WarningRegion();
                            warning.tracePointId = mTraceSelectedId;
                            warning.point = mCurrentFocusGeoPoint;
                            warning.warningRemoteLocalType = WarningRegion.WARNING_TYPE_LOCAL;
                            mWarningRegionList.add(warning);
                        }
                    } else {
                        return;
                    }
                    
                    warning.region = distance;
                    warning.regionPixel = mProjection.metersToEquatorPixels((float) distance * 1000);
                    warning.regionSquare = warning.regionPixel * warning.regionPixel;
                    if (mOutWarningView.isChecked()) {
                        warning.warningType = WarningRegion.WARNING_TYPE_OUT;
                    } else if (mInWarningView.isChecked()) {
                        warning.warningType = WarningRegion.WARNING_TYPE_IN;
                    }
                    
                    LOGD("[[showWarningRegionDialog::onClick]] waring info = " + warning.toString());
                    
                    if (mWarningRegionOverlay == null) {
                        mWarningRegionOverlay = new WarningRegionOverlay(MapViewActivity.this
                                                        , getResources().getDrawable(R.drawable.local_mark)
                                                        , mWarningRegionList); 
                    } else {
                        mWarningRegionOverlay.setWarningRegionList(mWarningRegionList);
                    }
                    mOverLays.remove(mWarningRegionOverlay);
                    mOverLays.add(mWarningRegionOverlay);
                    postRefreshOverlay();
                    
//                    for (WarningRegion region : mWarningRegionList) {
//                        DatabaseOperator.getInstance().saveWarningInfo(region);
//                    }
                    TraceDeviceInfoModel.getInstance().addLocalWarningRegion(warning);
                    mTraceSelectedId = null;
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
        listView.setAdapter(new TraceInfoAdapter(this, TraceDeviceInfoModel.getInstance().getTracePointInfo()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < TraceDeviceInfoModel.getInstance().getTracePointInfo().size()) {
                    mTraceListDialog.dismiss();
                    TracePointInfo info = TraceDeviceInfoModel.getInstance().getTracePointInfo().get(position);
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
            mTraceOverlay.setOverlayList(TraceDeviceInfoModel.getInstance().getTracePointInfo());
            mOverLays.add(mTraceOverlay);
            if (mWarningRegionOverlay != null) {
                mWarningRegionOverlay.setWarningRegionList(mWarningRegionList);
                mOverLays.add(mWarningRegionOverlay);
            } else {
                mWarningRegionOverlay = new WarningRegionOverlay(MapViewActivity.this
                                , getResources().getDrawable(R.drawable.local_mark)
                                , mWarningRegionList);
                mOverLays.add(mWarningRegionOverlay);
            }
            if (mSpecialOverlay != null) {
                mOverLays.add(mSpecialOverlay);
            }
            
//            for (WarningRegion region : mWarningRegionList) {
//                DatabaseOperator.getInstance().saveWarningInfo(region);
//            }
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
                        mLongPressedWarningRegionRetain = region;
                        mLongPressedWarningRegion = region;
                        mLongPressedX = event.getX();
                        mLongPressedY = event.getY();      
                        mLongPressedSqr = pointDist;
                        mBaseRegionPixel = -1;

                        mWarningInfoPopupView.setVisibility(View.VISIBLE);
                        MapView.LayoutParams geoLP = (MapView.LayoutParams) mPopupWarningView.getLayoutParams();
                        geoLP.point = region.point;
                        mMapView.updateViewLayout(mWarningInfoPopupView, geoLP);
//                        mMapController.animateTo(region.point);
                        
                        sendMessage(SHOW_WARNINGINFO_POPUP, region);
                        return;
                    }
                }
            }
            
            Projection projection = mMapView.getProjection();
            mCurrentFocusGeoPoint = projection.fromPixels((int) x, (int) y);
            mCurrentTraceInfo = null;
            
            mPopupView.setVisibility(View.GONE);
            mWarningInfoPopupView.setVisibility(View.GONE);
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
    
    public void updateWarningInfoPopup(WarningRegion region) {
        float distance = region.region;
        
        TextView tv = (TextView) mWarningInfoPopupView.findViewById(R.id.info);
        StringBuilder builder = new StringBuilder();
        WarningRegion warningInfo = region;
        builder.append("警告区域中心(" 
                    + String.valueOf((warningInfo.point.getLatitudeE6() * 1.0) / 1E6)
                    + " "
                    + String.valueOf((warningInfo.point.getLongitudeE6() * 1.0) / 1E6)
                    + "\n");
        builder.append("告警半径 ： " + String.valueOf(distance) + " 千米\n");
        builder.append("被控终端ID ： " + String.valueOf(warningInfo.tracePointId));
        
        tv.setText(builder.toString());
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
    
    private void sendMessage(int what, Object obj) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        mHandler.sendMessage(msg);
    }
    
    private void serviceTestCode() {
        String testCommand = "{\"MsgType\":0,\"MsgValue\":{\"IMSI\":\"11111111112222222222\",\"PhoneNum\":\"12345678901\"}}";
        String serverUrl = "http://114.242.178.111/ServiceTest/BackService.asmx/MonitorDeviceLoad";

        try {
            byte[] dataEncrpty = EncryptUtils.Encrypt2Bytes(testCommand.getBytes(), "dongbinhuiasxiny");
            String dataLog = EncryptUtils.Encrypt(testCommand, "dongbinhuiasxiny");
            LOGD("[[serviceTestCode]] data log = " + dataLog);
//            HttpResponse response = InternetUtils.OpenHttpConnection(serverUrl, testCommand.getBytes());
            HttpResponse response = InternetUtils.OpenHttpConnection(serverUrl, testCommand);
            if (response != null) {
                LOGD("[[serviceTestCode]] response code = " + response.getStatusLine().getStatusCode());
                String data = null;
                if (response.getStatusLine().getStatusCode() == 200) {
                    InputStream in = response.getEntity().getContent();
                    if (in != null) {
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.setNamespaceAware(true);
                        dbf.setCoalescing(true);
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document doc = db.parse(in);
                        in.close();
                        doc.getDocumentElement().normalize();
                        NodeList nl = doc.getElementsByTagName("string");
                        Node node = nl.item(0);
                        data = node.getFirstChild().getNodeValue();
                    }
                }
                LOGD("[[serviceTestCode]] data = " + data);
            } else {
                LOGD("[[serviceTestCode]] open service interface response == null");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }
    
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == Config.WARNING_LOCATE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String locationStr = data.getStringExtra(Config.WARNING_LOCATION_KEY);
                if (locationStr != null) {
                    String[] splited = locationStr.split(Config.DEFAULT_SPLIOR);
                    GeoPoint point = new GeoPoint((int) (Double.valueOf(splited[0]) * 1E6)
                                                    , (int) (Double.valueOf(splited[1]) * 1E6));
                    sendMoveLocationMessage(point);
                }
            }
        }
    }
    
    private static void LOGD(String msg) {
        if (Config.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
