package com.njtransit.domain;

import java.sql.Time;

public class StopTime {

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	private Integer id;
	private Integer tripId;
	private Time arrival;
	private Time departure;
	private Integer sequence;
	private Integer pickupType;
	private Integer dropOffType;
	
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
	public Time getArrival() {
		return arrival;
	}
	public void setArrival(Time arrival) {
		this.arrival = arrival;
	}
	public Time getDeparture() {
		return departure;
	}
	public void setDeparture(Time departure) {
		this.departure = departure;
	}
}
