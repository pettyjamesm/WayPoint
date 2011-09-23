package com.crobot.waypoint.tracking;

import java.util.Arrays;

import android.location.Location;

public class GPSAccuracyEvaluator {
	private static final int AUTO_ACCEPT_THRESHOLD 	= 20; 	//	Threshold to accept measurement
	private static final int MAX_NO_ACC_COUNT		= 5;	//	Max number of times to reject locations w/o accuracy
	private static final int HISTORY_LENGTH			= 3;	//	Trailing Number of Accuracy readings to keep
	
	//	Math Components
	private float[] 	history;
	private int[]		indexes;
	
	//	Boolean Switch Indicating Completions
	private boolean		accuracyMet;
	
	//	The number of location updates with no accuracy provided
	private int			noAccCount;
	
	/**
	 * Default Constructor
	 */
	public GPSAccuracyEvaluator(){
		this.accuracyMet = false;
		this.noAccCount	= 0;
		this.history	= new float[HISTORY_LENGTH];
		this.indexes	= new int[HISTORY_LENGTH];
		for (int i = 0; i < HISTORY_LENGTH; i++){
			this.indexes[i] = i;
		}
	}
	//
	//	Public Methods
	//
	
	/**
	 * startCalculatingPosition
	 * 	Resets the object to start calculating the new position
	 */
	public void startCalculatingPosition(){
		this.accuracyMet 	= false;
		this.noAccCount		= 0;
		this.history		= new float[HISTORY_LENGTH];
		this.indexes		= new int[HISTORY_LENGTH];
		for (int i = 0; i < HISTORY_LENGTH; i++){
			this.indexes[i] = i;
		}
	}
	/**
	 * addLocationUpdate
	 * 	Provides a new location reading to this object to add to the history and calculate from
	 * @param location The new location to reading
	 */
	public void addLocationUpdate(Location location){
		if (!location.hasAccuracy() && this.noAccCount <= MAX_NO_ACC_COUNT){
			this.noAccCount++;
			return;
		}
		float accuracy = location.getAccuracy();
		if (accuracy <= AUTO_ACCEPT_THRESHOLD){
			this.accuracyMet = true;
		} else {
			this.advanceIndexes();
			this.history[this.indexes[0]] = accuracy;
			if (this.approveAccuracyByPattern()){
				this.accuracyMet = true;
			}
		}
	}
	/**
	 * isAccuracyMet-
	 * 	Returns true if the location data received is good enough for a single location fix
	 */
	public boolean isAccuracyMet(){
		return this.accuracyMet;
	}
	
	//
	//	Private methods below here
	//
	/**
	 * advanceIndexes -
	 * 	Advances the index array so that the index of the most recent accuracy measurement
	 * is the value of indexes[0]
	 */
	private void advanceIndexes(){
		for (int i = 0; i < HISTORY_LENGTH; i++){
			this.indexes[i] = (this.indexes[i] + 1) % HISTORY_LENGTH;
		}
	}
	/**
	 * approveAccuracyByPattern -
	 * 	Attempts to determine if location accuracy has stopped improving
	 * @return True if the accuracy has stopped improving and should be accepted, false otherwise
	 */
	private boolean approveAccuracyByPattern(){
		float[] tmp = this.history.clone();
		Arrays.sort(tmp);
		if (tmp[HISTORY_LENGTH -1] - tmp[0] <= AUTO_ACCEPT_THRESHOLD){
			return true;
		}
		return false;		
	}
}
