package com.njtransit.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.location.Location;

import com.njtransit.utils.Distance;

public class Session {

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
	private Station currentArrivalStation;
	private Station currentDestintionStation;

	private List<Station> stations;
	
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

	public Station getCurrentArrivalStation() {
		return currentArrivalStation;
	}

	public void setCurrentArrivalStation(Station currentArrivalStation) {
		this.currentArrivalStation = currentArrivalStation;
	}

	public Station getCurrentDestintionStation() {
		return currentDestintionStation;
	}

	public void setCurrentDestintionStation(Station currentDestintionStation) {
		this.currentDestintionStation = currentDestintionStation;
	}
	
	public Station getStation(Integer id) {
		for(Station s : stations) {
			if(s.getId()==id) {
				return s;
			}
		}
		return null;
	}

}
