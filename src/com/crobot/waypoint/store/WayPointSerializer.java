package com.crobot.waypoint.store;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.util.Xml;

public final class WayPointSerializer {
	private static final String NAMESPACE		= "";
	private static final String WAYPOINT_TAG	= "waypoint";
	private static final String LATITUDE_TAG	= "lat";
	private static final String LONGITUDE_TAG 	= "lon";
	private static final String ACCURACY_TAG	= "acc";
	
	public static String createXmlString(final StoredWayPoint swp){
		return createXmlString(swp.getLocation());
	}
	
	public static String createXmlString(final Location location){
		final double lat = location.getLatitude();
		final double lon = location.getLongitude();
		final float acc  = location.getAccuracy();
		return createXmlString(lat, lon, acc);
	}
	
	public static String createXmlString(double lat, double lon, float acc){
		final XmlSerializer xml = Xml.newSerializer();
		final StringWriter writer = new StringWriter();
		try {
			xml.setOutput(writer);
			xml.startDocument("UTF-8", true);
			xml.startTag(NAMESPACE, WAYPOINT_TAG);
			xml.startTag(NAMESPACE, LATITUDE_TAG);
			xml.text(String.valueOf(lat));
			xml.endTag(NAMESPACE, LATITUDE_TAG);
			xml.startTag(NAMESPACE, LONGITUDE_TAG);
			xml.text(String.valueOf(lon));
			xml.endTag(NAMESPACE, LONGITUDE_TAG);
			xml.startTag(NAMESPACE, ACCURACY_TAG);
			xml.text(String.valueOf(acc));
			xml.endTag(NAMESPACE, ACCURACY_TAG);
			xml.endTag(NAMESPACE, WAYPOINT_TAG);
			xml.endDocument();
		} catch (Exception e){return null;}				
		return writer.toString();
	}
	
	public static Location getLocationFromXmlReader(final BufferedReader reader){
		final StringBuffer fileBuffer 	= new StringBuffer(512);		
		try {
			String lastRead = reader.readLine();
			while(lastRead != null){
				fileBuffer.append(lastRead);			
				lastRead = reader.readLine();
			}
		} catch (IOException e) {
			Log.e("Read Error", e.getMessage());
			return null;
		}
		return getLocationFromXmlString(fileBuffer.toString());
	}
	
	public static Location getLocationFromXmlString(final String xmlInput){
		double latitude		= -400.0;
		double longitude	= -400.0;
		float accuracy		= -400.0f;
		try {
			final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			final XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(new StringReader(xmlInput));			
			int eventType = xpp.getEventType();
			
			String tagName = "";
			
			while (eventType != XmlPullParser.END_DOCUMENT){
				if (eventType == XmlPullParser.START_TAG){
					tagName = xpp.getName();					
				} else if (eventType == XmlPullParser.TEXT){
					if (tagName.equals(LATITUDE_TAG)){ latitude = Double.valueOf(xpp.getText()).doubleValue();}
					else if (tagName.equals(LONGITUDE_TAG)){ longitude = Double.valueOf(xpp.getText()).doubleValue(); }
					else if (tagName.equals(ACCURACY_TAG)){ accuracy = Float.valueOf(xpp.getText()).floatValue(); }
				}
				eventType = xpp.next();
			}			
		} catch (XmlPullParserException e) {
			Log.e("XmlParser Error", e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e("XmlParser IO Error", e.getMessage());
			return null;
		}
		
		if (latitude == -400.0 || longitude == -400.0 || accuracy == -400.0f){
			return null;
		}
		final Location output = new Location(LocationManager.GPS_PROVIDER);
		output.setLatitude(latitude);
		output.setLongitude(longitude);
		output.setAccuracy(accuracy);		
		return output;
	}
}
