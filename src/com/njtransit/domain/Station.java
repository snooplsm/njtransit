package com.njtransit.domain;

public class Station {

	private Integer id;
	
	private String name;
	
	private Double latitude;
	
	private Double longitude;
	
	private Integer zoneId;

	public Station() {
		
	}
	
	public Station(Integer id, String name, Double latitude, Double longitude,
			Integer zoneId) {
		this.id = id;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.zoneId = zoneId;
	}

	public Integer getId() {
		return id;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public String getName() {
		return name;
	}

	public Integer getZoneId() {
		return zoneId;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setZoneId(Integer zoneId) {
		this.zoneId = zoneId;
	}
	
	@Override
	public String toString() {
	  return getName();
	}
}
