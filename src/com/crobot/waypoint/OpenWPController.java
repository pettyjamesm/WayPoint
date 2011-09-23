package com.crobot.waypoint;

import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.crobot.waypoint.R;
import com.crobot.waypoint.store.StoredWayPoint;

public class OpenWPController {
	//	Application Controller
	private AppController	appController;
	
	//	Dialog View
	private OpenWPDialog	openDialog;
	
	public OpenWPController(AppController ac){
		this.appController = ac;
		this.openDialog = new OpenWPDialog(this);
	}
	
	public void show(StoredWayPoint swp){
		this.openDialog.setStoredWayPoint(swp);
		this.openDialog.show();
	}
	public void hide(){
		this.openDialog.hide();
		this.appController.showListDialog();
	}
	
	public void onNavigateToWayPoint(StoredWayPoint swp){
		this.appController.showTrackingDialog(swp.getLocation());
	}
	
	public void onSendWayPoint(){
		this.appController.sendWayPoint(this.openDialog.storedWayPoint);
	}
	
	
	public class OpenWPDialog extends Dialog implements OnClickListener {
		//	OpenWPController
		private OpenWPController	openController;
		
		//	Current SavedWayPoint
		private StoredWayPoint		storedWayPoint;
		
		//	UI Elements of view
		private EditText			nameInput;
		private TextView			dateAdded;
		private ImageButton			navigateButton;
		private ImageButton			sendButton;
		private Button				deleteButton;
		
		public OpenWPDialog(OpenWPController owpc){
			super(owpc.appController.homeActivity);
			this.openController = owpc;
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.setContentView(R.layout.open_wp_dialog);
			this.getLayoutElements();
			this.storedWayPoint = null;
			this.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		}
		
		public void onStop(){
			this.storedWayPoint.locationName = nameInput.getText().toString();
        	this.storedWayPoint.update();
        	super.onStop();
        	this.openController.hide();
		}
		
		public void setStoredWayPoint(StoredWayPoint swp){
			this.storedWayPoint = swp;
			this.nameInput.setText(swp.locationName);
			this.dateAdded.setText(swp.createdDate);
		}
		
		private void getLayoutElements(){
			this.nameInput = (EditText)this.findViewById(R.id.open_wp_wpName);
			this.dateAdded = (TextView)this.findViewById(R.id.open_wp_wpDateAdded);
			this.deleteButton = (Button)this.findViewById(R.id.open_wp_deleteButton);
			this.deleteButton.setOnClickListener(this);
			this.navigateButton = (ImageButton)this.findViewById(R.id.open_wp_navigateToWPButton);
			this.navigateButton.setOnClickListener(this);	
			this.sendButton = (ImageButton)this.findViewById(R.id.open_wp_sendWPButton);
			this.sendButton.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if (v == this.deleteButton){
				Builder adb = new Builder(this.getContext());
				adb.setMessage("Delete WayPoint?");
			    adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   onDelete();		                
		           }
			    });
			    adb.setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
			     });
				adb.create().show();
			} else if (v == this.navigateButton){
				this.openController.onNavigateToWayPoint(this.storedWayPoint);
			} else if (v == this.sendButton){
				this.openController.onSendWayPoint();
			}
		}
		
		private void onDelete(){
			this.hide();
			this.storedWayPoint.deleteWayPoint();
		}
	}
}
