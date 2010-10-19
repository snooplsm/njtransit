package com.njtransit.domain;

public class Preferences {

	public enum Unit {
		ENGLISH, METRIC
	}
	
	private Unit units = Unit.ENGLISH;

	public Unit getUnits() {
		return units;
	}

	public void setUnits(Unit units) {
		this.units = units;
	}
	
	
	
}
