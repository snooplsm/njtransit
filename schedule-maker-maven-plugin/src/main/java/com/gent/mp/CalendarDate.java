package com.gent.mp;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CalendarDate {

	private Map<Integer,Date> serviceIdToDate = new HashMap<Integer,Date>();

	@Override
	public String toString() {
		return "CalendarDate [serviceIdToDate=" + serviceIdToDate + "]";
	}

	public Map<Integer, Date> getServiceIdToDate() {
		return serviceIdToDate;
	}

	public void setServiceIdToDate(Map<Integer, Date> serviceIdToDate) {
		this.serviceIdToDate = serviceIdToDate;
	}

	public CalendarDate(int serviceId, Date date) {
		super();
		serviceIdToDate.put(serviceId, date);
	}

}
