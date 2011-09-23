package com.crobot.waypoint.tracking;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class WPMapOverlay extends Overlay {	
	//	The map to draw to
	private MapController 	mMapController;
	private MapView			mMapView;
	//	The drawables to position on the map
	private Bitmap	mWPMarker;
	private UserMarker	mArrow;
	
	//	Locations on globe and in pixels
	private GeoPoint mWPLocation	= null;
	private Point	 mWPPoint		= new Point();
	private GeoPoint mUserLocation	= null;
	private Point	 mUserPoint		= new Point();
	
	public WPMapOverlay(MapView mv){
		this.mMapView  	= mv;
		this.mMapController = this.mMapView.getController();
		Resources res	= this.mMapView.getContext().getResources();
		this.mWPMarker 	= BitmapFactory.decodeResource(res, com.crobot.waypoint.R.drawable.red_ring); 
		this.mArrow		= new UserMarker(this.mWPMarker.getWidth() / 3);
	}
	
	public void setWayPointLocation(final Location location){
		this.setWayPointLocation(location.getLatitude(), location.getLongitude());
	}
	
	public void setWayPointLocation(double lat, double lon){
		final int latE6 = (int)(lat * 1E6);
		final int lonE6 = (int)(lon * 1E6);
		this.mWPLocation = new GeoPoint(latE6, lonE6);
		this.mMapView.postInvalidate();
		this.resetMapFitting();
	}
	
	public void updateUserData(final Location location){
		this.setUserCoordinates(location.getLatitude(), location.getLongitude());
	}
	
	private void setUserCoordinates(double lat, double lon){
		final int latE6 = (int)(lat * 1E6);
		final int lonE6 = (int)(lon * 1E6);
		this.mUserLocation = new GeoPoint(latE6, lonE6);
		this.resetMapFitting();
	}
	
	private void resetMapFitting(){
		if (this.mUserLocation == null || this.mWPLocation == null){ return; }
		final int usrLat = this.mUserLocation.getLatitudeE6();
		final int usrLon = this.mUserLocation.getLongitudeE6();
		final int wpLat	 = this.mWPLocation.getLatitudeE6();
		final int wpLon	 = this.mWPLocation.getLongitudeE6();
		final int[] span = getSpanSize(usrLat, usrLon, wpLat, wpLon);
		
		this.mMapController.zoomToSpan(span[0], span[1]);
		this.mMapController.setCenter(getMidPoint(usrLat, usrLon, wpLat, wpLon));
		this.mMapView.preLoad();
		this.mMapView.postInvalidate();
	}

	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when){
		super.draw(canvas, mapView, shadow);
		float drawX = 0;
		float drawY = 0;
		if (this.mWPLocation != null){
			// Draw WayPoint Marker
			mapView.getProjection().toPixels(this.mWPLocation, this.mWPPoint);
			drawX = this.mWPPoint.x - (this.mWPMarker.getWidth() / 2);
			drawY = this.mWPPoint.y - (this.mWPMarker.getHeight() / 2);
			canvas.drawBitmap(this.mWPMarker, drawX, drawY, null);
		}
		if (this.mUserLocation != null){
			// Draw User Marker
			mapView.getProjection().toPixels(this.mUserLocation, this.mUserPoint);
			this.mArrow.drawToCanvas(canvas, this.mUserPoint.x, this.mUserPoint.y);
		}
		return false;
	}

	private static GeoPoint getMidPoint(int lat1E6, int lon1E6, int lat2E6, int lon2E6){
		
		double lat1 = ((double)lat1E6) / 1E6;
		double lat2 = ((double)lat2E6) / 1E6;
		double lon1 = ((double)lon1E6) / 1E6;
		double lon2 = ((double)lon2E6) / 1E6;
		
		double dLon = Math.toRadians(lon2 - lon1);
	    lat1 = Math.toRadians(lat1);
	    lat2 = Math.toRadians(lat2);
	    lon1 = Math.toRadians(lon1);
		
	    double Bx = Math.cos(lat2) * Math.cos(dLon);
	    double By = Math.cos(lat2) * Math.sin(dLon);
	    double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
	    double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);
	    
	    return new GeoPoint((int)(Math.toDegrees(lat3) * 1E6),(int)(Math.toDegrees(lon3) * 1E6));
	}
	
	private static int[] getSpanSize(int lat1E6, int lon1E6, int lat2E6, int lon2E6){
		int[] output = new int[2];
		if (lat1E6 < 0){ lat1E6 += (360 * 1E6);}
		if (lat2E6 < 0){ lat2E6 += (360 * 1E6);}
		if (lon1E6 < 0){ lon1E6 += (360 * 1E6);}
		if (lon2E6 < 0){ lon2E6 += (360 * 1E6);}
		final int minLat = Math.abs((lat1E6 < lat2E6)? lat1E6 : lat2E6);
		final int maxLat = Math.abs((lat1E6 < lat2E6)? lat2E6 : lat1E6);
		final int minLon = Math.abs((lon1E6 < lon2E6)? lon1E6 : lon2E6);
		final int maxLon = Math.abs((lon1E6 < lon2E6)? lon2E6 : lon1E6);
		output[0] = maxLat - minLat;
		output[1] = maxLon - minLon;
		return output;
	}
}
