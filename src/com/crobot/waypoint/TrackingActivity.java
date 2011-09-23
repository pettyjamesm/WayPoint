package com.crobot.waypoint;

import java.text.DecimalFormat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crobot.waypoint.R;
import com.crobot.waypoint.tracking.ArrowView;
import com.crobot.waypoint.tracking.WPMapOverlay;
import com.crobot.waypoint.tracking.WayPointSvc;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class TrackingActivity extends MapActivity implements WayPointSvc.IListener {
	//	Public Keys for Adding Location Extra
	public static final String			LOCATION_KEY	= "com.crobot.waypoint.premium.location";
	// 	Private Preferences Keys
	private static final String			MAP_PREFS_NAME	= "com.crobot.waypoint.mapprefs";
	private static final String			SHOW_MAP_PREF	= "com.crobot.waypoint.showmap";
	private static final String			SATELLITE_PREF	= "com.crobot.waypoint.showsatellite";
	//	Text Formatters for updating display
	private static final DecimalFormat	DISTANCE_FORMAT = new DecimalFormat("#,##0 meters");
	private static final DecimalFormat	MERIDIAN_FORMAT = new DecimalFormat("##0.000000");
	private static final DecimalFormat	DISTANCE_FMT_M	= new DecimalFormat("#,##0 m");
	private static final DecimalFormat	DISTANCE_FMT_KM = new DecimalFormat("#,##0 km");
	
	//	WayPoint Service
	private WayPointSvc 		mWPSvc;
	private boolean				mUpdate		= false;
	private ServiceConnection 	mConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName className, IBinder service){
			mWPSvc = ((WayPointSvc.LocalBinder)service).getService();
			mWPSvc.addServiceListener(TrackingActivity.this);
		}
		@Override
		public void onServiceDisconnected(ComponentName className){
			if (mWPSvc != null){ mWPSvc.removeServiceListener(TrackingActivity.this); }			
			mWPSvc = null; 
		}
	};
	
	//	Destination WayPoint
	private Location	mDest = null;
	
	//	Activity Constituent Views
	private MapView		mMap;	
	private ArrowView	mArrow;
	private TextView	mLat;
	private TextView	mLon;
	private TextView	mAcc;
	private TextView	mDist;
	private LinearLayout mGpsBarLayout;
	private LinearLayout mArrowPaneLayout;
	private LinearLayout mRootLayout;
	
	//	MapView Overlay Handler
	private WPMapOverlay mOverlay;
	
	//	MapView Preferences Object
	private SharedPreferences	 mMapPrefs;
	
	private void getActivityViews(){
		this.mMapPrefs = this.getSharedPreferences(MAP_PREFS_NAME, MODE_PRIVATE);
		this.mMap = (MapView)this.findViewById(R.id.wp_tracking_mapView);
		this.mArrow = (ArrowView)this.findViewById(R.id.wp_tracking_arrowView);
		this.mLat = (TextView)this.findViewById(R.id.wp_tracking_currentLatitude);
		this.mLon = (TextView)this.findViewById(R.id.wp_tracking_currentLongitude);
		this.mAcc = (TextView)this.findViewById(R.id.wp_tracking_currentAccuracy);
		this.mDist = (TextView)this.findViewById(R.id.wp_tracking_distance);
		this.mGpsBarLayout = (LinearLayout)this.findViewById(R.id.tracking_gpsBarLayout);
		this.mArrowPaneLayout = (LinearLayout)this.findViewById(R.id.tracking_arrowPaneLayout);
		this.mRootLayout = (LinearLayout)this.findViewById(R.id.tracking_rootLayout);
		this.mOverlay = new WPMapOverlay(this.mMap);
		this.mMap.getOverlays().add(this.mOverlay);
		this.mMap.setSatellite(this.mMapPrefs.getBoolean(SATELLITE_PREF, true));
		if (!this.mMapPrefs.getBoolean(SHOW_MAP_PREF, true)){this.toggleMapViewVisibility();}
	}
	private void resetFieldContents(){
		this.mArrow.setRotationAngle(0);
		this.mLat.setText("-");
		this.mLon.setText("-");
		this.mAcc.setText("-");
		this.mDist.setText("Calculating Distance...");
	}
	private void doBindWayPointSvc(){
		this.bindService(new Intent(this, WayPointSvc.class), this.mConnection, Context.BIND_AUTO_CREATE);
	}
	private void doUnBindWayPointSvc(){
		this.mWPSvc.removeServiceListener(this);
		this.unbindService(this.mConnection);
	}
	private void getDestinationLocation(){
		final Bundle extras = this.getIntent().getExtras();
		if (extras != null && extras.containsKey(LOCATION_KEY)){
			final double[] loc	= extras.getDoubleArray(LOCATION_KEY);
			final double lat	= loc[0];
			final double lon	= loc[1];
			this.mDest = new Location(LocationManager.GPS_PROVIDER);
			this.mDest.setLatitude(lat);
			this.mDest.setLongitude(lon);
			this.mOverlay.setWayPointLocation(this.mDest);
		} else {
			this.mDest = null;
		}
	}
	
	//======================================================
	//	Activity Life-Cycle Calls
	//======================================================
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.tracking_activity);
        this.setTitle("");
        this.getActivityViews();
        this.getDestinationLocation();
        this.doBindWayPointSvc();        
    }
    @Override
    public void onResume(){
    	super.onResume();
    	this.resetFieldContents();
    	this.mUpdate = true;
    }
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	this.doUnBindWayPointSvc();
    	this.mMapPrefs.edit().putBoolean(SHOW_MAP_PREF, (this.mMap.getVisibility() == View.VISIBLE)).commit();
    	this.mMapPrefs.edit().putBoolean(SATELLITE_PREF, this.mMap.isSatellite()).commit();
    }
    @Override
    public void onPause(){
    	super.onPause();
    	this.mUpdate = false;
    }
	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu m){
		this.getMenuInflater().inflate(R.menu.tracking_menu, m);
		return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu m){
		final MenuItem showMap 		= m.getItem(0);
		final MenuItem setSatellite = m.getItem(1);
		
		if (this.mMap.isSatellite()){ setSatellite.setTitle("Map View"); }
		else {setSatellite.setTitle("Satellite View"); }
		
		if (this.mMap.isShown()){ showMap.setTitle("Hide Map");}
		else { showMap.setTitle("Show Map");}
		
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem mi){
		switch(mi.getItemId()){
		case R.id.tracking_menu_toggleShowMap:
			this.toggleMapViewVisibility();
			if (this.mMap.isShown()){ mi.setTitle("Hide Map"); }
			else { mi.setTitle("Show Map"); }			
			break;
		case R.id.tracking_menu_toggleSatellite:
			if (this.mMap.isSatellite()){ 
				this.mMap.setSatellite(false);
				mi.setTitle("Satellite View");
			} else {
				this.mMap.setSatellite(true);
				mi.setTitle("Map View");
			}
			break;
		default:
			break;
		}
		return false;
	}
	private void toggleMapViewVisibility(){
		this.runOnUiThread(new Runnable(){public void run(){
			TrackingActivity.this.toggleMapViewRunnableAction();
		}});		
	}
	private void toggleMapViewRunnableAction(){
		if (this.mMap.isShown()){
			this.mMap.setVisibility(View.GONE);
			this.mMap.setEnabled(false);
			this.mGpsBarLayout.setOrientation(LinearLayout.VERTICAL);
			LinearLayout ll = (LinearLayout)this.mDist.getParent();
			if (ll != null){ ll.removeView(this.mDist);}
			this.mArrowPaneLayout.addView(this.mDist, 0);
		} else {
			this.mMap.setVisibility(View.VISIBLE);
			this.mMap.setEnabled(true);
			this.mGpsBarLayout.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout ll = (LinearLayout)this.mDist.getParent();
			if (ll != null){ ll.removeView(this.mDist);}			
			this.mRootLayout.addView(this.mDist, this.mGpsBarLayout.getChildCount() - 1);
		}
	}
	
	//======================================================
	//	GPS Service Update Listener
	//======================================================
	
	@Override
	public void onGPSLocationUpdate(final Location location) {
		if (!this.mUpdate){return;}
		if (this.mDest == null){ this.resetFieldContents(); }
		final double distance 	= location.distanceTo(this.mDest);
		final float direction	= location.bearingTo(this.mDest) - location.getBearing();
		this.mArrow.setRotationAngle(direction);
		this.mAcc.setText("~"+DISTANCE_FMT_M.format(location.getAccuracy()));
		this.mLat.setText(MERIDIAN_FORMAT.format(location.getLatitude()));
		this.mLon.setText(MERIDIAN_FORMAT.format(location.getLongitude()));
		if (distance > 10000){ this.mDist.setText(DISTANCE_FMT_KM.format(distance / 1000));}
		else { this.mDist.setText(DISTANCE_FORMAT.format(distance));}
		if (this.mMap.isShown()){this.mOverlay.updateUserData(location);}
	}
	@Override
	public void onGPSStateChange(int gpsState) {
		if (!this.mUpdate){return;}
		if (gpsState == WayPointSvc.GPS_OFFLINE){
			this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.presence_offline);
	    	this.setTitle("GPS Unavailable");
		} else if (gpsState == WayPointSvc.GPS_SEARCHING){
			this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.presence_invisible);
	    	this.setTitle("GPS Searching...");
		} else if (gpsState == WayPointSvc.GPS_READY){
			this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.presence_online);
	    	this.setTitle("GPS Ready");
		}		
	}
}