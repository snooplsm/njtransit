package com.gent.mp;

import java.util.List;

public class StopTimesResult {

	private List<Trip> trips;
	private List<Stop> stops;
	private CalendarDate calendarDate;
	public StopTimesResult(List<Trip> trips, List<Stop> stops,
			CalendarDate calendarDate) {
		super();
		this.trips = trips;
		this.stops = stops;
		this.calendarDate = calendarDate;
	}
	public List<Trip> getTrips() {
		return trips;
	}
	public void setTrips(List<Trip> trips) {
		this.trips = trips;
	}
	public List<Stop> getStops() {
		return stops;
	}
	public void setStops(List<Stop> stops) {
		this.stops = stops;
	}
	public CalendarDate getCalendarDate() {
		return calendarDate;
	}
	public void setCalendarDate(CalendarDate calendarDate) {
		this.calendarDate = calendarDate;
	}
	
}
