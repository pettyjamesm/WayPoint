package com.crobot.waypoint;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.crobot.waypoint.R;
import com.crobot.waypoint.store.StoredWayPoint;

public class WPListController {
	//	Application Controller
	public AppController	appController;
	//	Dialog
	private WPListDialog	listDialog;
	private WPListAdapter	listAdapter;
	
	
	public WPListController(AppController ac){
		this.appController = ac;
		this.listAdapter = new WPListAdapter(this);
		this.listDialog = new WPListDialog(this);
		this.refreshListItems();
	}
	
	public void refreshListItems(){
		ArrayList<StoredWayPoint> locations;
		locations = this.appController.sqliteDB.getLocations();
		this.listAdapter.setElementArray(locations);
	}
	
	public void openListItem(int itemIndex){
		StoredWayPoint swp = (StoredWayPoint) this.listAdapter.getItem(itemIndex);
		this.appController.showOpenWPDialog(swp);
	}
	
	public void show(){
		this.listDialog.listView.setAdapter(this.listAdapter);
		this.listDialog.show();
	}
	public void hide(){
		this.listDialog.hide();
	}
	
	public class WPListAdapter extends BaseAdapter {
		
		//	List Controller
		private WPListController			listController;		
		//	ArrayList of Elements
		private ArrayList<StoredWayPoint> 	elements;

		public WPListAdapter(WPListController wplc) {
			this.listController = wplc;
		}
		
		public void setElementArray(ArrayList<StoredWayPoint> items){
			this.elements = items;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return this.elements.size();
		}

		@Override
		public Object getItem(int position) {
			return this.elements.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			WPListItem wpli;
			StoredWayPoint sw = (StoredWayPoint)this.getItem(position);
			if (convertView == null){
				wpli = new WPListItem(parent.getContext(), position,
										sw.locationName, sw.createdDate, this.listController);
			} else {
				wpli = (WPListItem)convertView;
				wpli.itemIndex = position;
				wpli.itemName = sw.locationName;
				wpli.dateAdded = sw.createdDate;
			}
			wpli.refreshViews();
			return wpli;
		}
	}
	
	public class WPListItem extends LinearLayout implements OnClickListener {
		int itemIndex;
		String itemName;
		String dateAdded;
		
		private TextView 			titleText;
		private TextView			dateText;
		
		private WPListController 	wpListController;
		
		public WPListItem(Context c, int indx, String name, String dateAdded, WPListController lc){
			super(c);
			this.setOrientation(LinearLayout.VERTICAL);
			this.setOnClickListener(this);
			this.itemIndex = indx;
			this.itemName = name;
			this.dateAdded = dateAdded;
			this.wpListController = lc;
			LayoutInflater inflator = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout layout = (LinearLayout)inflator.inflate(R.layout.wp_list_item, null);
			this.addView(layout);
			//	Style Text
			this.titleText = (TextView)layout.findViewById(R.id.wp_item_title);
			this.dateText = (TextView)layout.findViewById(R.id.wp_item_date);
			this.setFocusable(true);
			this.setClickable(true);
		}
		
		public void refreshViews(){
			this.titleText.setText(this.itemName);
			this.dateText.setText("Created: "+this.dateAdded);
		}

		@Override
		public void onClick(View v) {
			this.wpListController.openListItem(this.itemIndex);
		}
	}
	
	
	public class WPListDialog extends Dialog {
		//	Layout Views
		public ListView				listView;
		public LinearLayout			listLayout;
		
		public WPListDialog(WPListController wplc){
			super(wplc.appController.homeActivity);
			this.requestWindowFeature(Window.FEATURE_LEFT_ICON);
			this.setTitle("My WayPoints");
			this.setContentView(R.layout.wp_list_dialog);
			this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_map);			
			this.getLayoutElements();
			this.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}
		
		private void getLayoutElements(){
			this.listView = (ListView)this.findViewById(R.id.wp_listView);
			this.listLayout = (LinearLayout)this.findViewById(R.id.wp_list_linearLayout);
		}
	}
}
