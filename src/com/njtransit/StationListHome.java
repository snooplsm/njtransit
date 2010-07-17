package com.njtransit;

import android.app.TabActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;

import com.njtransit.domain.Session;
import com.njtransit.ui.adapter.StationAdapter;

/** Holds two tabs for selecting stations (in alphabetical order or by proximity). 
 * This activity provides a means of selecting first a departure station and 
 * then a destination station before forwarding control to the StopListHome 
 * activity.
 */
public class StationListHome extends TabActivity {
	
	private StationListImpl stations;
	
	private Session session = Session.get();
	
	/** static??? */
	private static boolean created = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.station_list_home);
		if(!created) {
			NJTransitDBAdapter a = new NJTransitDBAdapter(this).open();
			session.setAdapter(a);
			session.setServices(a.getServices());
		}
//		Station trenton = a.getStation(148);
//		Station newark = a.getStation(107);
//		StopsQueryResult sqr = a.getStopTimes(session.getServices(), trenton, newark);
//		if(sqr.getQueryDuration()>1) {
//			Log.w("query-length", "Query length exceeds 1 second : " + sqr.getQueryDuration());
//		}
//		for(Stop s : sqr.getStops()) {
//			s.getArrive();
//		}
		final String alphaTabTxt = "Alpha";
		final String proximityTabTxt = "Nearby";
		
		TabHost tabHost =  getTabHost();
		TabContentFactory f = new TabContentFactory() {

			@Override
			public View createTabContent(String arg) {
				int mode = session.getDepartureStation() == null ? StationListImpl.FIRST_STATION_MODE : StationListImpl.SECOND_STATION_MODE;
				if(alphaTabTxt.equals(arg)) {
					if(stations == null) {
						stations = (StationListImpl)getLayoutInflater().inflate(R.layout.station_list_xml_2, null); 
					}
					stations.setType(StationAdapter.ALPHA);
					stations.setMode(mode);
					return stations;
				}else
				if(proximityTabTxt.equals(arg)) {
					if(stations == null) {
						stations = (StationListImpl)getLayoutInflater().inflate(R.layout.station_list_xml_2, null);
					}
					stations.setMode(mode);
					stations.setType(StationAdapter.ALPHA);
					return stations;
				}				
				return null;
			}
			
		};
		tabHost.addTab(tabHost.newTabSpec(alphaTabTxt).setIndicator(alphaTabTxt).setContent(f));
		tabHost.addTab(tabHost.newTabSpec(proximityTabTxt).setIndicator(proximityTabTxt).setContent(f));		
		tabHost.setCurrentTab(0);		
		created = true;
	}
}