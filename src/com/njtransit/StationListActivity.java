package com.njtransit;

import android.app.TabActivity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;

import com.njtransit.domain.Session;
import com.njtransit.ui.adapter.StationAdapter;

/** Holds two tabs for selecting stations (in alphabetical order or by proximity). 
 * This activity provides a means of selecting first a departure station and 
 * then a destination station before forwarding control to the StopListHome 
 * activity.
 */
public class StationListActivity extends TabActivity implements LocationListener {
	
	private StationListView stations;
	
	private Session session = Session.get();
	
	private boolean created = false;
	
	private static String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
	
	private LocationManager getLocations() {
		return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.station_list_home);
		
		if(!created) {
			DatabaseAdapter a = new DatabaseAdapter(this).open();
			session.setAdapter(a);
			session.setServices(a.getServices());
			session.setStations(a.getAllStations());
		}
		
		session.setLastKnownLocation(getLocations().getLastKnownLocation(LOCATION_PROVIDER));
		if(session.getLastKnownLocation() == null) {
			// listen until we find one then
			getLocations().requestLocationUpdates(LOCATION_PROVIDER, 3600000, 0, this);
		}
		
		final String alphaTabTxt = "By Name";
		final String proximityTabTxt = "By Proximity";
		final String favorites = "Favorites";
		
		TabHost tabHost =  getTabHost();
		
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				int mode = session.getDepartureStation() == null ? StationListView.FIRST_STATION_MODE : StationListView.SECOND_STATION_MODE;
				final int type;
				if(alphaTabTxt.equals(tabId)) {
					type = StationAdapter.ALPHA;
				} else if (proximityTabTxt.equals(tabId)) {
					type = StationAdapter.NEARBY;
				} else {
					type = StationAdapter.FAVORITES;
				}
				stations.setType(type).setMode(mode);
			}
			
		});
		TabContentFactory f = new TabContentFactory() {
			@Override
			public View createTabContent(String name) {								
				if(stations == null) {
					stations = (StationListView)getLayoutInflater().inflate(R.layout.station_list_xml_2, null); 
				}					
				return stations;
				
			}
		};
		addTab(alphaTabTxt, f);
		addTab(proximityTabTxt, f);
		addTab(favorites, f);
		tabHost.setCurrentTab(0);		
		created = true;
	}
	
	@Override
	public void onLocationChanged(Location l) {
		session.setLastKnownLocation(l);
		getTabHost().setCurrentTab(getTabHost().getCurrentTab());
		getLocations().removeUpdates(this);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	protected void addTab(String named, TabContentFactory contents) {
		getTabHost().addTab(getTabHost().newTabSpec(named).setIndicator(named).setContent(contents));
	}
}