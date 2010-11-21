package com.njtransit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.njtransit.domain.IService;
import com.njtransit.domain.Preferences;
import com.njtransit.domain.Station;
import com.njtransit.utils.Distance;

public class Session  {

	private Context context;

	private Location lastKnownLocation;
	
	private Station arrivalStation;

	private Station departureStation;

	private int stationOrderType = 1;
	private Preferences preferences;
	private List<Station> stations;
	private Map<Integer, IService> services;
	
	@SuppressWarnings("unused")
	private LocationManager locationManager;

	private DatabaseAdapter adapter;

	Session(DatabaseAdapter adapter) {
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

	public DatabaseAdapter getAdapter() {
		if(adapter==null) {
			adapter = new DatabaseAdapter(context);
			adapter.open();
		}
		return adapter;
	}
	
	public Station getArrivalStation() {
		return arrivalStation;
	}
	
	public Context getContext() {
		return context;
	}

	public Station getDepartureStation() {
		return departureStation;
	}

	public Location getLastKnownLocation() {
		return lastKnownLocation;
	}
	
	public Preferences getPreferences() {
		if(preferences==null) {
			preferences = new Preferences();
		}
		return preferences;
	}
	
	public Map<Integer, IService> getServices() {
		return services;
	}

	public Station getStation(Integer id) {
		for(Station s : stations) {
			if(s.getId().equals(id)) {
				return s;
			}
		}
		return null;
	}

	public int getStationOrderType() {
		return stationOrderType;
	}
	
	public List<Station> getStations() {
		return stations;
	}

	public void reverseTrip() {
		Station tmp = departureStation;
		departureStation = arrivalStation;
		arrivalStation = tmp;
	}

	public void setArrivalStation(Station arrivalStation) {
		this.arrivalStation = arrivalStation;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public void setDepartureStation(Station departureStation) {
		this.departureStation = departureStation;
	}

	public void setLastKnownLocation(Location lastKnownLocation) {
		this.lastKnownLocation = lastKnownLocation;
	}

	public void setPreferences(Preferences preferences) {
		this.preferences = preferences;
	}

	public void setServices(List<IService> services) {
		this.services = new HashMap<Integer,IService>();
		for(IService s : services) {
			this.services.put(s.getId(), s);
		}
	}

	public void setStationOrderType(int stationOrderType) {
		this.stationOrderType = stationOrderType;
	}


	public void setStations(ArrayList<Station> stations) {
		this.stations = stations;
	}

}
