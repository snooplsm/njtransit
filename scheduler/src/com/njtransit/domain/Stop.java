package com.njtransit.domain;

import java.util.Calendar;


public class Stop {
	
	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}

	private int tripId;
	
	private String blockId;
	
	private Calendar depart,arrive;

	public Stop(int tripId, Calendar depart, Calendar arrive, String blockId) {
		this.tripId = tripId;
		this.depart = depart;
		this.arrive =arrive;
		this.blockId = blockId;
	}
	
	
	public String getBlockId() {
		return blockId;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arrive == null) ? 0 : arrive.hashCode());
		result = prime * result + ((depart == null) ? 0 : depart.hashCode());
		result = prime * result + tripId;
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
		Stop other = (Stop) obj;
		if (arrive == null) {
			if (other.arrive != null)
				return false;
		} else if (!arrive.equals(other.arrive))
			return false;
		if (depart == null) {
			if (other.depart != null)
				return false;
		} else if (!depart.equals(other.depart))
			return false;
		if (tripId != other.tripId)
			return false;
		return true;
	}

	public int getTripId() {
		return tripId;
	}

	public Calendar getDepart() {
		return depart;
	}

	public Calendar getArrive() {
		return arrive;
	}	

}
