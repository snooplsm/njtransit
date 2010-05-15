package com.njtransit.domain;

public class Station {

	private Integer id;
	
	private String name;
	
	private Float latitude;
	
	private Float longitude;
	
	private Integer zoneId;

	public Integer getId() {
		return id;
	}

	public Float getLatitude() {
		return latitude;
	}

	public Float getLongitude() {
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

	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setZoneId(Integer zoneId) {
		this.zoneId = zoneId;
	}
	
}
