package com.njtransit.domain;

public class Service {

	private int id;
	private boolean monday,tuesday,wednesday,thursday,friday,saturday,sunday;
	
	public Service(int id, boolean... startWithMonday) {
		this.id = id;
		monday = startWithMonday[0];
		tuesday = startWithMonday[1];
		wednesday = startWithMonday[2];
		thursday = startWithMonday[3];
		friday = startWithMonday[4];
		saturday = startWithMonday[5];
		sunday = startWithMonday[6];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		Service other = (Service) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public int getId() {
		return id;
	}

	public boolean isFriday() {
		return friday;
	}

	public boolean isMonday() {
		return monday;
	}

	public boolean isSaturday() {
		return saturday;
	}

	public boolean isSunday() {
		return sunday;
	}

	public boolean isThursday() {
		return thursday;
	}

	public boolean isTuesday() {
		return tuesday;
	}

	public boolean isWednesday() {
		return wednesday;
	}
	
}
