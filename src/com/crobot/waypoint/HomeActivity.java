package com.crobot.waypoint;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.crobot.waypoint.R;
import com.crobot.waypoint.store.WayPointSerializer;
import com.crobot.waypoint.tracking.WayPointSvc;

public class HomeActivity extends Activity implements WayPointSvc.IListener {
	
	//	Application Core Operators
	public AppController			appController;
	public HomeActivityController 	homeController;

	//	Pointers to the UI Elements on this page
	public Button	addWPButton;
	public Button	listWPButton;
	
	//	GPS State Change Methods
	private WayPointSvc mWPSvc		= null;
	private int		mCurrentState	= WayPointSvc.GPS_OFFLINE;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent called = this.getIntent();
        this.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.main);
        this.setTitle("");
        /**	Create Program Operators **/
        this.appController	= new AppController(this);
        this.homeController = new HomeActivityController(this);
        /**	Get UI Element Pointers **/
        this.getLayoutElements();
        this.doBindWayPointSvc();
        if (this.isImportOperation(called)){
        	try {
				Location embedded = this.getEmbeddedLocation(called);
				if (embedded != null){ this.appController.addLocationToDB("Imported WayPoint", embedded);}
				else { Toast.makeText(this.getApplicationContext(), "Corrupt WayPoint File!", Toast.LENGTH_LONG).show(); }
			} catch (FileNotFoundException e) {
				Log.v("ERROR", e.getMessage());
				Toast.makeText(this.getApplicationContext(), "Invalid WayPoint File Location", Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				Log.v("ERROR", e.getMessage());
				Toast.makeText(this.getApplicationContext(), "Corrupt WayPoint File!", Toast.LENGTH_LONG).show();
			}
        }
        /**	Tell the Main Controller to Initialize **/
    }
    
    private void doBindWayPointSvc(){
    	this.bindService(new Intent(this, WayPointSvc.class), this.mConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void doUnBindWayPointSvc(){
    	if (this.mWPSvc != null){ try {this.mWPSvc.unbindService(this.mConnection);} catch (Exception e){}}
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	this.mUpdate = false;
    }    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	this.doUnBindWayPointSvc();
    }
    @Override
    public void onResume(){
    	super.onResume();
    	this.mUpdate = true;
    	this.refreshGPSStateIcon();
    }
    
    
    //
    //	WayPoint File Import Operations
    //
    private boolean isImportOperation(Intent called){
    	final String action = called.getAction();
    	if (action.equals(Intent.ACTION_VIEW) || action.equals(Intent.ACTION_EDIT)){
    		return true;
    	}
    	return false;
    }
    private Location getEmbeddedLocation(Intent called)throws FileNotFoundException, IOException{
   		InputStream is = this.getContentResolver().openInputStream(called.getData());
    	InputStreamReader isr = new InputStreamReader(is);
    	BufferedReader reader = new BufferedReader(isr);
    	final Location imported = WayPointSerializer.getLocationFromXmlReader(reader);
    	reader.close();
    	return imported;
	}
    
    
    /**
     * getLayoutElements
     * 	Retrieves the Views from the Layout after the content has been set
     */
    private void getLayoutElements(){
    	this.addWPButton = (Button)this.findViewById(R.id.ha_createWayPointButton);
    	this.addWPButton.setOnClickListener(this.homeController);
    	this.listWPButton = (Button)this.findViewById(R.id.ha_listWayPointsButton);
    	this.listWPButton.setOnClickListener(this.homeController);
    }
    
    private boolean				mUpdate		= false;
	private ServiceConnection 	mConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName className, IBinder service){
			mWPSvc = ((WayPointSvc.LocalBinder)service).getService();
			mWPSvc.addServiceListener(HomeActivity.this);
		}
		@Override
		public void onServiceDisconnected(ComponentName className){ 
			if (mWPSvc != null){ mWPSvc.removeServiceListener(HomeActivity.this);}
			mWPSvc = null; 
		}
	};

	@Override
	public void onGPSLocationUpdate(Location location) {
		this.appController.cwpController.onLocationUpdate(location);
	}

	@Override
	public void onGPSStateChange(int gpsState) {
		if (gpsState != this.mCurrentState){
			this.mCurrentState 	= gpsState;
			if (this.mUpdate){
				this.refreshGPSStateIcon();
			}			
		}
	} 
	
	private void refreshGPSStateIcon(){
		if (this.mCurrentState == WayPointSvc.GPS_OFFLINE){
			this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.presence_offline);
	    	this.setTitle("GPS Unavailable");
		} else if (this.mCurrentState == WayPointSvc.GPS_SEARCHING){
			this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.presence_invisible);
	    	this.setTitle("GPS Searching...");
		} else if (this.mCurrentState == WayPointSvc.GPS_READY){
			this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.presence_online);
	    	this.setTitle("GPS Ready");
		}	
	}
}