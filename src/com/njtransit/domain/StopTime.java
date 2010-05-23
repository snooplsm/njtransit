package com.njtransit.domain;

import java.util.Calendar;

public class StopTime {

	private Integer stationId;
	private Integer tripId;
	private Calendar arrival;
	private Calendar departure;
	private Integer sequence;
	private Integer pickupType;
	private Integer dropOffType;

	public StopTime() {
		
	}
	
	public StopTime(Integer stationId, Integer tripId, Calendar arrival,
			Calendar departure, Integer sequence, Integer pickupType,
			Integer dropOffType) {
		this.stationId = stationId;
		this.tripId = tripId;
		this.arrival = arrival;
		this.departure = departure;
		this.sequence = sequence;
		this.pickupType = pickupType;
		this.dropOffType = dropOffType;
	}


	public Integer getStationId() {
		return stationId;
	}
	
	public void setStationId(Integer stationId) {
		this.stationId = stationId;
	}
	
	public Integer getTripId() {
		return tripId;
	}
	
	public void setTripId(Integer tripId) {
		this.tripId = tripId;
	}
	public Integer getSequence() {
		return sequence;
	}
	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}
	public Integer getPickupType() {
		return pickupType;
	}
	public void setPickupType(Integer pickupType) {
		this.pickupType = pickupType;
	}
	public Integer getDropOffType() {
		return dropOffType;
	}
	public void setDropOffType(Integer dropOffType) {
		this.dropOffType = dropOffType;
	}
	public Calendar getArrival() {
		return arrival;
	}
	public void setArrival(Calendar arrival) {
		this.arrival = arrival;
	}
	public Calendar getDeparture() {
		return departure;
	}
	public void setDeparture(Calendar departure) {
		this.departure = departure;
	}
}
