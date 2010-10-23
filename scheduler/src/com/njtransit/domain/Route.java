package com.njtransit.domain;

public class Route {

	private Integer id;
	
	private Integer agencyId;
	
	private String shortName;
	
	private String longName;
	
	private Integer routeType;
	
	public Route(Integer id, Integer agencyId, String shortName,
			String longName, Integer routeType) {
		this.id = id;
		this.agencyId = agencyId;
		this.shortName = shortName;
		this.longName = longName;
		this.routeType = routeType;
	}

	public Integer getId() {
		return id;
	}

	public Integer getAgencyId() {
		return agencyId;
	}

	public String getShortName() {
		return shortName;
	}

	public String getLongName() {
		return longName;
	}

	public Integer getRouteType() {
		return routeType;
	}
}