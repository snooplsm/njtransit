package com.njtransit.domain;

public class Preferences {

	public enum Unit {
		ENGLISH, METRIC
	}
	
	private Unit units = Unit.ENGLISH;
	
	private Boolean findScheduleOnLaunch = true;

	public Boolean getFindScheduleOnLaunch() {
		return findScheduleOnLaunch;
	}

	public void setFindScheduleOnLaunch(Boolean findScheduleOnLaunch) {
		this.findScheduleOnLaunch = findScheduleOnLaunch;
	}

	public Unit getUnits() {
		return units;
	}

	public void setUnits(Unit units) {
		this.units = units;
	}
	
	
	
}
