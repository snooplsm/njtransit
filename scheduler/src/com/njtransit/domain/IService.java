package com.njtransit.domain;

import java.util.Calendar;

public interface IService {

	boolean isToday();
	boolean isTomorrow();
	
	boolean isDate(Calendar cal);
	
	int getId();
	
}
