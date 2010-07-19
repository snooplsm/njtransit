package com.njtransit;

import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabContentFactory;

import com.njtransit.domain.Session;
import com.njtransit.ui.adapter.StationAdapter;

/** Holds two tabs for selecting stations (in alphabetical order or by proximity). 
 * This activity provides a means of selecting first a departure station and 
 * then a destination station before forwarding control to the StopListHome 
 * activity.
 */
public class StationListHome extends TabActivity implements LocationListener {
	
	private StationListImpl stations;
	
	private Session session = Session.get();
	
	/** static??? */
	private static boolean created = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.station_list_home);
		
		if(!created) {
			DatabaseAdapter a = new DatabaseAdapter(this).open();
			session.setAdapter(a);
			session.setServices(a.getServices());
		}
		
		session.setLocationManager(getLocations());
		session.setLastKnownLocation(getLocations().getLastKnownLocation(LocationManager.GPS_PROVIDER));
		
		final String alphaTabTxt = "By Name";
		final String proximityTabTxt = "By Proximity";
		
		TabHost tabHost =  getTabHost();
		TabContentFactory f = new TabContentFactory() {
			@Override
			public View createTabContent(String name) {
				int mode = session.getDepartureStation() == null ? StationListImpl.FIRST_STATION_MODE : StationListImpl.SECOND_STATION_MODE;
				int type = alphaTabTxt.equals(name) ? StationAdapter.ALPHA : StationAdapter.NEARBY;
				if(stations == null) {
					stations = (StationListImpl)getLayoutInflater().inflate(R.layout.station_list_xml_2, null); 
				}
				if(type == StationAdapter.NEARBY) {
					getLocations().removeUpdates(StationListHome.this);
					getLocations().requestLocationUpdates(LocationManager.GPS_PROVIDER, 3600000, 0, StationListHome.this);
				} else {
					getLocations().removeUpdates(StationListHome.this);
				}

				String query = null;
				Intent intent = getIntent();
			    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			      query = intent.getStringExtra(SearchManager.QUERY);
			    }
				
				return stations.setType(type).setMode(mode).setQuery(query);
			}
		};
		tabHost.addTab(tabHost.newTabSpec(alphaTabTxt).setIndicator(alphaTabTxt).setContent(f));
		tabHost.addTab(tabHost.newTabSpec(proximityTabTxt).setIndicator(proximityTabTxt).setContent(f));		
		tabHost.setCurrentTab(0);		
		created = true;
	}
	
	@Override
	public boolean onSearchRequested() {
		// pause, search just got invoked
	    return super.onSearchRequested();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    setIntent(intent);
	    handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      getTabHost().setCurrentTab(getTabHost().getCurrentTab());
	    }
	}

	@Override
	public void onLocationChanged(Location l) {
		Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();
		session.setLastKnownLocation(l);
		getTabHost().setCurrentTab(getTabHost().getCurrentTab());
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
	
	private LocationManager getLocations() {
		return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}
}