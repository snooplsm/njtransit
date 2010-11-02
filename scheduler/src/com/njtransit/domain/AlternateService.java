package com.njtransit.domain;

import java.util.Calendar;
import java.util.TimeZone;

public class AlternateService implements IService {

	private int id;
	
	public int getId() {
		return id;
	}

	public Calendar getDate() {
		return date;
	}

	public int getException() {
		return exception;
	}

	public AlternateService(int id, Calendar date, int exception) {
		this.id = id;
		this.date = date;
		this.exception = exception;
	}
	
	public boolean isToday() {
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int today =now.get(Calendar.DAY_OF_YEAR);
		int year = now.get(Calendar.YEAR);
		return today==date.get(Calendar.DAY_OF_YEAR) && year == date.get(Calendar.YEAR); 
	}
	
	public boolean isTomorrow() {
		Calendar tomorrow = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		int tomorrowDay = tomorrow.get(Calendar.DAY_OF_YEAR);
		int tomorrowYear = tomorrow.get(Calendar.YEAR);
		return tomorrowDay == date.get(Calendar.DAY_OF_YEAR) && tomorrowYear == tomorrow.get(Calendar.YEAR);
	}
	

	private Calendar date;
	
	private int exception;
	
}
