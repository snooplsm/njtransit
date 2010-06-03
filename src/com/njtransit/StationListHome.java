package com.njtransit;

import com.njtransit.ui.adapter.StationAdapter;

import android.app.SearchManager;
import android.app.TabActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;

public class StationListHome extends TabActivity {
	
	private TabHost tabHost;
	
	private StationListImpl stations;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.station_list_home);
		tabHost =  getTabHost();
		TabContentFactory f = new TabContentFactory() {

			@Override
			public View createTabContent(String arg) {
				if("Alpha".equals(arg)) {
					if(stations==null) {
						stations = (StationListImpl)getLayoutInflater().inflate(R.layout.station_list_xml_2, null); 
					}
					stations.setType(StationAdapter.ALPHA);
					return stations;
				}else
				if("Nearby".equals(arg)) {
					if(stations==null) {
						stations = (StationListImpl)getLayoutInflater().inflate(R.layout.station_list_xml_2, null);
					}
					stations.setType(StationAdapter.ALPHA);					
					return stations;
				}				
				return null;
			}
			
		};
		tabHost.addTab(tabHost.newTabSpec("Alpha").setIndicator("Alpha").setContent(f));
		tabHost.addTab(tabHost.newTabSpec("Nearby").setIndicator("Nearby").setContent(f));		
		tabHost.setCurrentTab(0);		
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onSearchRequested() {
		// TODO Auto-generated method stub
		return super.onSearchRequested();
	}
	
	
}
