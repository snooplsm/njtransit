package com.njtransit.domain;

import java.util.ArrayList;

import com.njtransit.utils.Distance;

import android.location.Location;

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
		if(location==null) location = lastKnownLocation;
		if(location==null || stations==null || stations.isEmpty()) return null;
		Station closest = null;
		double minDist = Double.MAX_VALUE;
		for(Station s : stations) {
			double dist = Distance.greatCircle(location.getLatitude(), location.getLongitude(), s.getLatitude(), s.getLongitude());
			if(dist < minDist) {
				minDist = dist;
				closest = s;
			}
		}
		return closest;
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
