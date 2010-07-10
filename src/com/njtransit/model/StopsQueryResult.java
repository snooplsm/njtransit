package com.njtransit.model;

import java.util.List;
import java.util.Map;

import com.njtransit.domain.Service;
import com.njtransit.domain.Station;
import com.njtransit.domain.Stop;

public class StopsQueryResult {

	private Station depart,arrive;
	
	private Long queryStart,queryEnd;
	
	private Map<Integer,Service> tripToService;
	
	private List<Stop> stops;
	
	public StopsQueryResult(Station depart, Station arrive, Long queryStart, Long queryEnd, Map<Integer,Service> tripToService, List<Stop> stops) {
		this.depart = depart;
		this.arrive = arrive;
		this.queryEnd = queryEnd;
		this.queryStart = queryStart;
		this.tripToService = tripToService;
		this.stops = stops;
	}
	
	public Double getQueryDuration() {
		return (queryEnd - queryStart) / 1000.0;
	}

	public Station getDepart() {
		return depart;
	}

	public Station getArrive() {
		return arrive;
	}

	public Long getQueryStart() {
		return queryStart;
	}

	public Long getQueryEnd() {
		return queryEnd;
	}

	public Map<Integer, Service> getTripToService() {
		return tripToService;
	}

	public List<Stop> getStops() {
		return stops;
	}
	
}
