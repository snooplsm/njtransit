package com.njtransit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.admob.android.ads.AdManager;
import com.njtransit.domain.IService;
import com.njtransit.domain.Preferences;
import com.njtransit.domain.Station;
import com.njtransit.utils.Distance;

/** Shared state management */
public class SchedulerApplication extends Application implements LocationListener  {

	private Location lastKnownLocation;
	private Station arrivalStation, departureStation;
	private int stationOrderType = 1;
	private Preferences preferences;
	private DeviceInformation deviceInformation;
	public DeviceInformation getDeviceInformation() {
		return deviceInformation;
	}

	private static String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;

	@Override
	public void onCreate() {
		super.onCreate();

		deviceInformation = DeviceInformation.getDeviceInformatino(this);

		adapter = new DatabaseAdapter(getApplicationContext()).open();
		Toast.makeText(getApplicationContext(), getString(R.string.disclaimer),
				Toast.LENGTH_SHORT).show();
		this.stations = adapter.getStations();
		
		setLastKnownLocation(getLocations().getLastKnownLocation(
				LOCATION_PROVIDER));
		if (getLastKnownLocation() == null) {
			getLocations().requestLocationUpdates(LOCATION_PROVIDER, 3600000,
					0, this);
		}
		AdManager.setAllowUseOfLocation(true);
	}

	public Preferences getPreferences() {
		if(preferences==null) {
			preferences = new Preferences();
		}
		return preferences;
	}

	public void setPreferences(Preferences preferences) {
		this.preferences = preferences;
	}

	public int getStationOrderType() {
		return stationOrderType;
	}

	public void setStationOrderType(int stationOrderType) {
		this.stationOrderType = stationOrderType;
	}

	private List<Station> stations;
	
	private Map<Integer, IService> services;
	
	public Map<Integer, IService> getServices() {
		return services;
	}

	public void setServices(List<IService> services) {
		this.services = new HashMap<Integer,IService>();
		for(IService s : services) {
			this.services.put(s.getId(), s);
		}
	}

	@SuppressWarnings("unused")
	private LocationManager locationManager;
	
	private DatabaseAdapter adapter;
	
	public DatabaseAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(DatabaseAdapter adapter) {
		this.adapter = adapter;
	}

	/**
	 * 
	 * @param location can be null, if so defaults to lastKnownLocation
	 * @return
	 */
	public Map<Station, Double> findClosestStation(Location location) {
		Map<Station, Double> s = findClosestStations(location, 1);
		if(s==null || s.isEmpty()) {
			return Collections.<Station, Double>emptyMap();
		}
		return s;
	}
	
	/**
	 * This is a weak algorithm, if performance is a concern we should address it.
	 * 
	 * @param location
	 * @param max
	 * @return Key-Value collection of station to relative metered distances
	 */
	public Map<Station, Double> findClosestStations(Location location, int max) {
		if(location == null) location = lastKnownLocation;
		if(location == null || stations == null || stations.isEmpty()) return new HashMap<Station,Double>(2){
			private static final long serialVersionUID = 1L;
			{
				put(getStations().get(0), 100.0);
				put(getStations().get(1), 200.0);
			}
		};
		TreeMap<Double,Station> closest = new TreeMap<Double,Station>();
		for(Station s : stations) {
			double dist = Distance.greatCircle(location.getLatitude(), location.getLongitude(), s.getLatitude(), s.getLongitude());
			if(closest.size() < max) {
				closest.put(dist, s);
			} else {
				for(Double oldDist : closest.keySet()) {
					if(dist < oldDist) {
						closest.remove(oldDist);
						closest.put(dist, s);
						break;
					}
				}
			}
		}
		Map<Station, Double> inverted = new HashMap<Station, Double>(closest.size());
		for(Map.Entry<Double, Station> e: closest.entrySet()) {
			inverted.put(e.getValue(), e.getKey());
		}
		return inverted;
	}

	public List<Station> getStations() {
		return stations;
	}

	public void setStations(ArrayList<Station> stations) {
		this.stations = stations;
	}

	public Location getLastKnownLocation() {
		return lastKnownLocation;
	}

	public void setLastKnownLocation(Location lastKnownLocation) {
		this.lastKnownLocation = lastKnownLocation;
	}
	
	public Station getArrivalStation() {
		return arrivalStation;
	}

	public void setArrivalStation(Station arrivalStation) {
		this.arrivalStation = arrivalStation;
	}

	public Station getDepartureStation() {
		return departureStation;
	}

	public void setDepartureStation(Station departureStation) {
		this.departureStation = departureStation;
	}

	public Station getStation(Integer id) {
		for(Station s : stations) {
			if(s.getId().equals(id)) {
				return s;
			}
		}
		return null;
	}

	public void reverseTrip() {
		Station tmp = departureStation;
		departureStation = arrivalStation;
		arrivalStation = tmp;
	}
	
	private LocationManager getLocations() {
		return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public void onLocationChanged(Location l) {
		setLastKnownLocation(l);
		// getTabHost().setCurrentTab(getTabHost().getCurrentTab());
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
}
