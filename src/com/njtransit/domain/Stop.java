package com.njtransit.domain;


public class Stop {
	
	private int tripId,depart,arrive;
	
	private transient double duration = Double.NaN;
	
//	public double getDuration() {
//		if(Double.isNaN(duration)) {
//			String arrive = arrival.getTime().toGMTString();
//			String depart = departure.getTime().toGMTString();
//			long diff = arrival.getTimeInMillis() - departure.getTimeInMillis();
//			duration = diff / 1000.0;
//		}
//		return duration;
//	}

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
