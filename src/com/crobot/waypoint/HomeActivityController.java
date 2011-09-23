package com.crobot.waypoint;

import android.view.View;
import android.view.View.OnClickListener;

public class HomeActivityController implements OnClickListener {
	
	private HomeActivity 	homeActivity;
	public	AppController	appController;
	
	public HomeActivityController(HomeActivity ha){
		this.homeActivity = ha;
		this.appController = this.homeActivity.appController;
	}

	@Override
	public void onClick(View v) {
		if (v == this.homeActivity.addWPButton){
			this.appController.showCreateWPDialog();
		} else if (v == this.homeActivity.listWPButton){
			this.appController.showListDialog();
		}
	}

}
