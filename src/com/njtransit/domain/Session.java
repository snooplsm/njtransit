package com.njtransit.domain;

import java.util.ArrayList;
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

	private ArrayList<Station> stations;
	
	/**
	 * 
	 * @param location can be null, if so defaults to lastKnownLocation
	 * @return
	 */
	public Station findClosestStation(Location location) {
		ArrayList<Station> s = findClosestStations(location,1);
		if(s==null || s.isEmpty()) {
			return null;
		}
		return s.get(0);
	}
	
	/**
	 * This is a week algorithm, if performance is a concern we should address it.
	 * 
	 * @param location
	 * @param max
	 * @return
	 */
	public ArrayList<Station> findClosestStations(Location location, int max) {
		if(location==null) location = lastKnownLocation;
		if(location==null || stations==null || stations.isEmpty()) return null;
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
		return new ArrayList<Station>(closest.values());
	}

	public ArrayList<Station> getStations() {
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

}
