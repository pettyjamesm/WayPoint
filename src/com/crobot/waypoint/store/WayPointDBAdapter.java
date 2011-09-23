package com.crobot.waypoint.store;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.crobot.waypoint.AppController;

public class WayPointDBAdapter {
	//	DATABASE COLUMN NAMES
	public static final String PRIMARY_KEY 	= "rowId";
	public static final String LATITUDE		= "lat";
	public static final String LONGITUDE	= "lon";
	public static final String ACCURACY		= "acc";
	public static final String CREATED_ON	= "create_date";
	public static final String LOCATION_NAME = "loc_name";
	//	DATABASE INFORMATION
	private static final String DB_NAME		= "WayPoint";
	private static final String TABLE_NAME	= "locations";
	private static final int 	DB_VERSION	= 1;
	//	DATABASE QUERY STRINGS
	private static final String SELECT_ALL	= "SELECT * FROM "+TABLE_NAME;
	private static final String SELECT_BY_ID = PRIMARY_KEY+"=?";
	//	DATABASE DATE FORMATTER
	private static final DateFormat DATE_FMT = DateFormat.getDateInstance(DateFormat.MEDIUM);
	
	//	Context of this DB Access
	private Context			parentContext;
	//	SQLiteOpenHelper
	private DBOpenHelper 	openHelper;
	//	AppController
	private AppController	appController;

	public WayPointDBAdapter(AppController ac) {
		this.parentContext 	= ac.homeActivity;	
		this.appController 	= ac;
		this.openHelper		= new DBOpenHelper(this.parentContext);
	}
	
	public boolean insert(float lat, float lon, float acc, String name){
		Date date = new Date(System.currentTimeMillis());
		try {
			SQLiteDatabase db = this.openHelper.getWritableDatabase();
			ContentValues initialValues = new ContentValues();
			initialValues.put(LATITUDE, lat);
			initialValues.put(LONGITUDE, lon);
			initialValues.put(ACCURACY, acc);
			initialValues.put(CREATED_ON, DATE_FMT.format(date));
			initialValues.put(LOCATION_NAME, name);
			db.insert(TABLE_NAME, null, initialValues);
		} catch (SQLException e){
			Log.v("ERROR", "Error Inserting Data: "+e.getMessage());
			return false;
		}
		this.appController.updateWayPointList();
		return true;
	}	
	public void deleteByKey(int primaryKey){
		SQLiteDatabase db = this.openHelper.getWritableDatabase();
		db.delete(TABLE_NAME, SELECT_BY_ID, new String[]{ Integer.toString(primaryKey)});
		this.appController.updateWayPointList();
	}
	public void updateRow(StoredWayPoint toUpdate){
		int key = toUpdate.databaseKey;
		ContentValues update = toUpdate.getContentValues();
		SQLiteDatabase db = this.openHelper.getWritableDatabase();		
		db.update(TABLE_NAME, update, SELECT_BY_ID, new String[]{Integer.toString(key)});
		this.appController.updateWayPointList();
	}
	
	public ArrayList<StoredWayPoint> getLocations(){
		ArrayList<StoredWayPoint> output = new ArrayList<StoredWayPoint>();
		SQLiteDatabase db = this.openHelper.getWritableDatabase();
		Cursor	crsr = db.rawQuery(SELECT_ALL, null);
		crsr.moveToFirst();
		for (int i = 0; i < crsr.getCount(); i++){
			float[] tmp	 = new float[3];
			int		key  = crsr.getInt(0);
			tmp[0]	 	 = crsr.getFloat(1);
			tmp[1]	 	 = crsr.getFloat(2);
			tmp[2]	 	 = crsr.getFloat(3);
			String date	 = crsr.getString(4);
			String name	 = crsr.getString(5);
			output.add(new StoredWayPoint(this, key, tmp, date, name));
			crsr.moveToNext();
		}
		crsr.deactivate();
		return output;
	}
	
	
	public class DBOpenHelper extends SQLiteOpenHelper {
		//	String for Database Creation
		private static final String DB_CREATE	= "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("+PRIMARY_KEY+
		" INTEGER PRIMARY KEY AUTOINCREMENT, "+LATITUDE+" real not null, "+LONGITUDE+" real not null, "+
		ACCURACY+" real not null, "+CREATED_ON+" text not null, "+LOCATION_NAME+" text not null);";

		public DBOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);			
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DB_CREATE);
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
			this.onCreate(db);
		}		
	}
}
