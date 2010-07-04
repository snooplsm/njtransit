package com.njtransit.domain;

import java.util.Calendar;


public class Stop {
	
	private int tripId;
	
	private Calendar depart,arrive;

	public Stop(int tripId, Calendar arrive, Calendar depart) {
		this.tripId = tripId;
		this.depart = depart;
		this.arrive =arrive;
	}
	
	public int getTripId() {
		return tripId;
	}

	public Calendar getDepart() {
		return depart;
	}

	public Calendar getArrive() {
		return arrive;
	}	

}
