package com.njtransit.domain;

public class Station {

	private Integer id;
	
	private String name;
	
	private Double latitude;
	
	private Double longitude;
	
	private Integer zoneId;
	
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
	
	@Override
	public String toString() {
	  return getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Station other = (Station) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}