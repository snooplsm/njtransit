package com.gent.mp;

public class Station {

	@Override
	public String toString() {
		return "Station [id=" + id + ", lat=" + lat + ", lng=" + lng
				+ ", name=" + name + "]";
	}
	private String name;
	private String id;
	private float lat,lng;
	
	public Station(String name, String id, float lat, float lng) {
		super();
		this.name = name;
		this.id = id;
		this.lat = lat;
		this.lng = lng;
	}
	
	public float getLat() {
		return lat;
	}
	public void setLat(float lat) {
		this.lat = lat;
	}
	public float getLng() {
		return lng;
	}
	public void setLng(float lng) {
		this.lng = lng;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
}
