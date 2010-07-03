package com.njtransit;

import android.app.TabActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabContentFactory;

import com.njtransit.domain.Session;

public class StopListHome extends TabActivity {

	private Session session = Session.get();
	
	private TabHost tabHost;
	
	private TextView title;
	
	private StopImpl stopTimes;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stop_list_home);
		//setTitle
		
		title = (TextView) findViewById(R.id.title);
		title.setText(session.getDepartureStation().getName() +" > " + session.getArrivalStation().getName());
		
		tabHost =  getTabHost();
		
		TabContentFactory f = new TabContentFactory() {

			@Override
			public View createTabContent(String arg) {
				if("Default".equals(arg)) {
					if(stopTimes==null) {
						stopTimes = (StopImpl)getLayoutInflater().inflate(R.layout.stop_impl, null);
					}
					return stopTimes;
				}
				//int mode = session.getDepartureStation()==null ? StationListImpl.FIRST_STATION_MODE : StationListImpl.SECOND_STATION_MODE;
//				if("Alpha".equals(arg)) {
//					if(stations==null) {
//						stations = (StationListImpl)getLayoutInflater().inflate(R.layout.station_list_xml_2, null); 
//					}
//					stations.setType(StationAdapter.ALPHA);
//					stations.setMode(mode);
//					return stations;
//				}else
//				if("Nearby".equals(arg)) {
//					if(stations==null) {
//						stations = (StationListImpl)getLayoutInflater().inflate(R.layout.station_list_xml_2, null);
//					}
//					stations.setMode(mode);
//					stations.setType(StationAdapter.ALPHA);					
//					return stations;
//				}				
				return null;
			}
			
		};
		tabHost.addTab(tabHost.newTabSpec("Default").setIndicator("Default").setContent(f));
//		tabHost.addTab(tabHost.newTabSpec("Nearby").setIndicator("Nearby").setContent(f));		
		tabHost.setCurrentTab(0);	
	}
}
