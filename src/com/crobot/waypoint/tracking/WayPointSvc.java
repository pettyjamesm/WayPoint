package com.crobot.waypoint.tracking;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public class WayPointSvc extends Service implements GpsStatus.Listener, LocationListener {
	//	GPS States
	public static final int GPS_OFFLINE		= 0;
	public static final int GPS_SEARCHING	= 1;
	public static final int GPS_READY		= 2;
	
	//	Binder Class
	public class LocalBinder extends Binder {
		public WayPointSvc getService(){ return WayPointSvc.this; }
	}
	
	// 	Binder Instance
	private LocalBinder	mBinder = new LocalBinder();
	//	Location Manager
	private LocationManager			mLocMan;
	private GPSAccuracyEvaluator 	mAccCheck;
	//	GPS Status Containers
	private int mGpsState;
	private int	mLastState;
	
	//	Update State Switches
	private boolean mRegistered;	
	//	Listeners to notify on updates
	private ArrayList<WayPointSvc.IListener> mListeners;
	
	//==========================================
	// Notification Dispatch Methods
	//==========================================
	private void dispatchGpsStateUpdate(){
		for (int i = 0; i < this.mListeners.size(); i++){
			this.mListeners.get(i).onGPSStateChange(this.mGpsState);
		}
	}
	private void dispatchLocationUpdate(final Location location){
		for (int i = 0; i < this.mListeners.size(); i++){
			this.mListeners.get(i).onGPSLocationUpdate(location);
		}
	}
	public void addServiceListener(final WayPointSvc.IListener l){
		this.mListeners.add(l);
		if (!this.mRegistered){this.registerForUpdates();}
		this.dispatchGpsStateUpdate();
	}
	public void removeServiceListener(final WayPointSvc.IListener l){
		this.mListeners.remove(l);
		if (this.mListeners.size() == 0){ this.unregisterForUpdates(); }
	}
	
	//==========================================
	//	Location Interface Methods
	//==========================================
	@Override
	public void onGpsStatusChanged(int event) {
		this.mLastState = this.mGpsState;
		switch(event){
		case GpsStatus.GPS_EVENT_FIRST_FIX:
		case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
			if (this.mAccCheck.isAccuracyMet()){ this.mGpsState = GPS_READY; }
			else { this.mGpsState = GPS_SEARCHING; }
			break;		
		case GpsStatus.GPS_EVENT_STARTED:
			this.mGpsState = GPS_SEARCHING;
			this.mAccCheck.startCalculatingPosition();
			break;
		case GpsStatus.GPS_EVENT_STOPPED:
			this.mGpsState = GPS_OFFLINE;
			this.mAccCheck.startCalculatingPosition();
			break;
		default:
			break;
		}
		if (this.mLastState != this.mGpsState){
			this.dispatchGpsStateUpdate();
		}		
	}
	@Override
	public void onLocationChanged(Location location) {
		this.mAccCheck.addLocationUpdate(location);
		this.dispatchLocationUpdate(location);
		if (this.mGpsState != GPS_READY && this.mAccCheck.isAccuracyMet()){
			this.mLastState = this.mGpsState;
			this.mGpsState  = GPS_READY;
			this.dispatchGpsStateUpdate();
		}
	}
	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "GPS Disabled", Toast.LENGTH_SHORT).show();
		this.mGpsState = GPS_OFFLINE;
		this.dispatchGpsStateUpdate();
	}
	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "GPS Enabled", Toast.LENGTH_SHORT).show();		
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	//===========================================
	//	Behavior modification calls
	//===========================================
	private void registerForUpdates(){
		this.mRegistered = true;
		this.mAccCheck.startCalculatingPosition();
		this.mLocMan.addGpsStatusListener(this);
		this.mLocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}
	private void unregisterForUpdates(){
		this.mLocMan.removeGpsStatusListener(this);
		this.mLocMan.removeUpdates(this);
		this.mRegistered = false;
		this.mAccCheck.startCalculatingPosition();
	}
	
	//===========================================
	// Service Life-Cycle
	//===========================================
	@Override
	public void onCreate(){
		super.onCreate();
		this.mListeners = new ArrayList<WayPointSvc.IListener>();
		this.mAccCheck	= new GPSAccuracyEvaluator();
		this.mLocMan	= (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		this.mGpsState	= GPS_OFFLINE;
		this.mLastState = GPS_OFFLINE;
		this.mRegistered = false;
	}
	@Override
	public void onDestroy(){
		this.unregisterForUpdates();
		super.onDestroy();
	}
	@Override
	public void onStart(Intent intent, int startId){
		super.onStart(intent, startId);
		this.registerForUpdates();
	}
	@Override
	public IBinder onBind(Intent intent) {
		return this.mBinder;
	}
	
	//===========================================
	// Listener Interface Definition
	//===========================================
	public interface IListener {
		public void onGPSStateChange(final int gpsState);
		public void onGPSLocationUpdate(final Location location);
	}
}
