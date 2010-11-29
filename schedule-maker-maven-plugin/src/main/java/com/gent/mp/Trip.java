package com.gent.mp;

public class Trip {

	private int serviceId;
	private int tripId;
	
	public int getServiceId() {
		return serviceId;
	}
	@Override
	public String toString() {
		return "Trip [serviceId=" + serviceId + ", tripId=" + tripId + "]";
	}
	public Trip(int serviceId, int tripId) {
		super();
		this.serviceId = serviceId;
		this.tripId = tripId;
	}
	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}
	public int getTripId() {
		return tripId;
	}
	public void setTripId(int tripId) {
		this.tripId = tripId;
	}
	
}
