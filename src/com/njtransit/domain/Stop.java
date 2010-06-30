package com.njtransit.domain;


public class Stop {
	
	private int tripId,depart,arrive;

	public Stop(int tripId, int depart, int arrive) {
		this.tripId = tripId;
		this.depart = depart;
		this.arrive =arrive;
	}
	
	public int getTripId() {
		return tripId;
	}
	
	public int getDuration() {
		return (arrive-depart)/60000;
	}

}
