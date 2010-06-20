package com.njtransit.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.njtransit.NJTransitDBAdapter;
import com.njtransit.NJTransitDBHelper;
import com.njtransit.utils.Distance;

public class Session implements LocationListener {

	private Session() {
	}

	private static Session instance;

	public static Session get() {
		if (instance == null) {
			instance = new Session();
		}
		return instance;
	}

	private Location lastKnownLocation;
	private Station arrivalStation;
	private Station departureStation;

	private List<Station> stations;
	
	private LocationManager locationManager;
	private NJTransitDBAdapter adapter;
	
	public NJTransitDBAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(NJTransitDBAdapter adapter) {
		this.adapter = adapter;
	}

	public void setLocationManager(LocationManager locationManager) {
		this.locationManager = locationManager;
		if(locationManager!=null) {
			lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(lastKnownLocation==null) {
		        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3600000, 0,
		                this);
			}

		}
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
		if(stations!=null) {
			ArrayList<Station> station = new ArrayList<Station>();
			for(Station s : stations) {
				if(s.getId().equals(148) || s.getId().equals(107)) {
					station.add(s);
				}
			}
			return station;
		}
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

	@Override
	public void onLocationChanged(Location location) {

		this.lastKnownLocation = location;
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
