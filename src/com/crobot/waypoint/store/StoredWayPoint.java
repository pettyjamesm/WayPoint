package com.crobot.waypoint.store;

import android.content.ContentValues;
import android.location.Location;
import android.location.LocationManager;

public class StoredWayPoint {
	
	private WayPointDBAdapter dbAdapter;
	
	public 	int			databaseKey;
	
	private float		latitude;
	private float		longitude;
	private float		accuracy;
	
	public String		createdDate;
	public String		locationName;

	public StoredWayPoint(WayPointDBAdapter wpdba, int dbKey, float[] vals, String date, String name){
		this.dbAdapter		= wpdba;
		this.databaseKey 	= dbKey;
		this.latitude 		= vals[0];
		this.longitude 		= vals[1];
		this.accuracy 		= vals[2];
		this.createdDate 	= date;
		this.locationName 	= name;
	}	
	public Location getLocation(){
		Location output = new Location(LocationManager.GPS_PROVIDER);
		output.setLatitude(this.latitude);
		output.setLongitude(this.longitude);
		output.setAccuracy(this.accuracy);
		return output;
	}	
	public void deleteWayPoint(){
		this.dbAdapter.deleteByKey(this.databaseKey);
	}	
	public ContentValues getContentValues(){
		ContentValues output = new ContentValues();
		output.put(WayPointDBAdapter.LATITUDE, this.latitude);
		output.put(WayPointDBAdapter.LONGITUDE, this.longitude);
		output.put(WayPointDBAdapter.ACCURACY, this.accuracy);
		output.put(WayPointDBAdapter.CREATED_ON, this.createdDate);
		output.put(WayPointDBAdapter.LOCATION_NAME, this.locationName);
		return output;
	}	
	public void update(){
		this.dbAdapter.updateRow(this);
	}
	
	public String toString(){
		return this.locationName;
	}
}
