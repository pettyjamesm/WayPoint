package com.crobot.waypoint;

import java.text.DateFormat;
import java.util.Date;

import com.crobot.waypoint.R;

import android.app.Dialog;
import android.location.Location;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class CreateWPController implements OnClickListener {
	//	Application Controller
	private AppController			appController;
	//	Dialog
	private CreateWayPointDialog 	cwpDialog;
	
	//	Switch indicating if awaiting a location update
	private boolean		awaitingLocation;
	
	public CreateWPController(AppController ac){
		this.appController	= ac;
		this.awaitingLocation = false;
		this.cwpDialog		= new CreateWayPointDialog(this);
	}
	
	public void show(){
		this.awaitingLocation = true;
		this.cwpDialog.onStart();
		this.cwpDialog.show();
	}
	public void hide(){
		this.awaitingLocation = false;
		this.cwpDialog.hide();
	}
	
	public void onLocationUpdate(Location location){
		if (this.awaitingLocation){
			this.awaitingLocation = false;
			this.cwpDialog.onLocationInsert(location);
		}
	}
	
	@Override
	public void onClick(View v) {
		if (this.cwpDialog.locationValue != null){
			String name = this.cwpDialog.nameInput.getText().toString();
			this.appController.addLocationToDB(name, this.cwpDialog.locationValue);
			this.hide();
		}
	}
	
	private class CreateWayPointDialog extends Dialog {
		private CreateWPController cwpController;
		
		//	UI Views
		public TextView		nameInput;
		public TextView		gpsStatus;
		public Button		addWayPoint;
		
		public Location		locationValue;
		
		public CreateWayPointDialog(CreateWPController wpc){
			super(wpc.appController.homeActivity);
			this.cwpController = wpc;
			this.locationValue = null;
			this.requestWindowFeature(Window.FEATURE_LEFT_ICON);
			this.setTitle("Add New WayPoint");		
			this.setContentView(R.layout.create_wp_dialog);
			this.getLayoutViews();
			this.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_map);
		}
		
		//	Actions when being put to screen
		public void onStart(){
			this.locationValue = null;
			this.addWayPoint.setEnabled(false);
			this.gpsStatus.setText("Awaiting GPS Location");
			this.nameInput.setText(getDefaultName());
			super.onStart();
		}
		
		public void onLocationInsert(Location location){
			this.locationValue = location;
			this.gpsStatus.setText("GPS WayPoint Set");
			this.addWayPoint.setEnabled(true);
		}
		
		/**
		 * getLayoutViews
		 * 	Gets the elements within the view
		 */
		private void getLayoutViews(){
			this.nameInput = (TextView)this.findViewById(R.id.waypointNameTV);
			this.gpsStatus = (TextView)this.findViewById(R.id.createWayPointGPSStatus);
			this.addWayPoint = (Button)this.findViewById(R.id.createWayPointButton);
			this.addWayPoint.setOnClickListener(this.cwpController);
		}	
		//	Static Utilities
		private String getDefaultName(){
			Date date		= new Date();
			DateFormat fmt 	= DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			return "Quick-Save: "+fmt.format(date);
		}

	}
}
