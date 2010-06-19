package com.njtransit.domain;

import java.util.Calendar;

public class Stop {
	
	private int id;
	
	private Calendar departure;
	
	private int sequence;
	
	public Stop(int id, Calendar departure, int sequence) {
		this.departure = departure;
		this.id = id;
		this.sequence = sequence;
	}

	public int getSequence() {
		return sequence;
	}

	public Calendar getDeparture() {
		return departure;
	}

	public int getId() {
		return id;
	}

}
