package com.crobot.waypoint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.crobot.waypoint.store.StoredWayPoint;
import com.crobot.waypoint.store.WayPointDBAdapter;
import com.crobot.waypoint.store.WayPointSerializer;

public class AppController {
	public static final int GPS_UNAVAILABLE 	= 0;
	public static final int GPS_SEARCHING		= 1;
	public static final int GPS_READY			= 2;
	
	//	Main Application Activity
	public 	HomeActivity		homeActivity;
	//	GPS Location Storage
	public  WayPointDBAdapter	sqliteDB;
	//	Current GPS Status
	public int currentStatus;
	
	//	Dialog Controllers
	private WPListController			listController;
	public CreateWPController			cwpController;
	private OpenWPController			openWPController;
	
	//	File Properties
	private static final String TMP_FILENAME = "SentWayPoint.waypoint";
	private static final File	EXT_STORAGE	 = Environment.getExternalStorageDirectory();
	private static final File	EXT_DATA_DIR = new File(EXT_STORAGE, "/Android/data/com.crobot.waypoint.premium/files/");
	private static final File	TMP_FILE 	 = new File(EXT_DATA_DIR, TMP_FILENAME);
	private static final Uri	FILE_URI 	 = Uri.fromFile(TMP_FILE);
	
	public AppController(HomeActivity ha){
		this.homeActivity = ha;
		this.sqliteDB = new WayPointDBAdapter(this);
		this.listController = new WPListController(this);
		this.cwpController = new CreateWPController(this);
		this.openWPController = new OpenWPController(this);
		this.currentStatus = GPS_UNAVAILABLE;
		if (!TMP_FILE.exists()){TMP_FILE.mkdirs();}
	}
	//
	//	Send WayPoint
	//
	public void sendWayPoint(StoredWayPoint swp){
		try {
			this.writeWayPointFile(swp);
			Intent send = new Intent(Intent.ACTION_SEND);
			send.putExtra(Intent.EXTRA_SUBJECT, swp.locationName+" WayPoint File");
			send.putExtra(Intent.EXTRA_STREAM, FILE_URI);
			send.setType("application/vnd.crobot.waypoint");
			this.hideAllDialogs();
			this.homeActivity.startActivity(Intent.createChooser(send, "Send Using:"));
		} catch (IOException ioe){
			Log.v("IO ERROR", ioe.getMessage());
			Toast.makeText(this.homeActivity.getApplicationContext(), "WayPoint Save Error!", Toast.LENGTH_LONG).show();
		}		
	}	
	private void writeWayPointFile(StoredWayPoint swp) throws IOException {
		if (TMP_FILE.exists()){TMP_FILE.delete(); }
		TMP_FILE.createNewFile();
		FileWriter writer = new FileWriter(TMP_FILE, false);
		final String xmlString = WayPointSerializer.createXmlString(swp);
		writer.append(xmlString);
		writer.flush();
		writer.close();
	}
	
	//
	//	Show Dialog Views
	//
	public void showCreateWPDialog(){
		this.cwpController.show();
	}
	public void showListDialog(){
		this.listController.show();
	}
	public void showTrackingDialog(Location location){
		this.openWPController.hide();
		this.listController.hide();
		this.cwpController.hide();
		Intent tracking = new Intent(this.homeActivity, TrackingActivity.class);
		tracking.putExtra(TrackingActivity.LOCATION_KEY, new double[]{location.getLatitude(), location.getLongitude()});
		this.homeActivity.startActivity(tracking);
	}
	public void showOpenWPDialog(StoredWayPoint swp){
		this.listController.hide();
		this.openWPController.show(swp);
	}
	public void hideAllDialogs(){
		this.openWPController.hide();
		this.listController.hide();
		this.cwpController.hide();
	}
	
	//
	//	SQLite Location Operations
	//
	public void addLocationToDB(String name, Location location){
		float	lat = (float)location.getLatitude();
		float	lon = (float)location.getLongitude();
		float	acc = location.getAccuracy();
		this.sqliteDB.insert(lat, lon, acc, name);
		Toast.makeText(this.homeActivity.getApplicationContext(), "WayPoint Saved", Toast.LENGTH_SHORT).show();
	}
	public void updateWayPointList(){
		this.listController.refreshListItems();
	}
}
