package com.njtransit.domain;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class AlternateService implements IService {

	private int id;

	private List<Calendar> dates = new ArrayList<Calendar>();
	
	private static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-DD");

	public AlternateService(int id) {
		super();
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public boolean isToday() {
		Calendar now = Calendar.getInstance();
		int today = now.get(Calendar.DAY_OF_YEAR);
		int year = now.get(Calendar.YEAR);
		for (Calendar c : dates) {
			if (today == c.get(Calendar.DAY_OF_YEAR)
					&& year == c.get(Calendar.YEAR)) {
				return true;
			}
		}
		return false;
	}

	public List<Calendar> getDates() {
		return (dates);
	}

	public boolean isTomorrow() {
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		int tomorrowDay = tomorrow.get(Calendar.DAY_OF_YEAR);
		int tomorrowYear = tomorrow.get(Calendar.YEAR);
		for (Calendar c : dates) {
			if (tomorrowDay == c.get(Calendar.DAY_OF_YEAR)
					&& tomorrowYear == c.get(Calendar.YEAR)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("[");
		for(Calendar c : dates) {
			b.append(DF.format(c.getTime()));
		}
		b.append("]");
		return b.toString();
	}

}
