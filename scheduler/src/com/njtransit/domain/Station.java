package com.njtransit.domain;

public class Station {

	private Integer id;
	
	private String name;
	
	private String descriptiveName;
	
	private Double latitude;
	
	private Double longitude;

	public Station(Integer id, String name, Double latitude, Double longitude) {
		this.id = id;
		setName(name);
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public String getDescriptiveName() {
		return descriptiveName;
	}
	
	private void setName(String str) {
		this.name = makePretty(str);
	}
	
	public void setDescriptiveName(String str) {
		this.descriptiveName = makePretty(str);
	}
	
	private String makePretty(String str) {
		char lastChar=' ';
		StringBuilder sb = new StringBuilder(str);
		for(int i = 0; i < sb.length();i++) {
			char nowChar = sb.charAt(i);
			if(lastChar==' ' || lastChar=='/') {
				sb.setCharAt(i, Character.toUpperCase(nowChar));
			} else {
				sb.setCharAt(i, Character.toLowerCase(nowChar));
			}
			lastChar = nowChar;
		}
		return sb.toString();
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