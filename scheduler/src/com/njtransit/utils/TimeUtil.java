package com.njtransit.utils;

import android.content.Context;
import android.content.res.Resources;

import com.scheduler.R;

public class TimeUtil {

	public static String getUnitTime(Long millis, Context context) {
		Long minutes = millis / 60000;
		Resources r = context.getResources();
		if(minutes<=99) {
			if(minutes<0) {
				int seconds = (int) (millis / 1000);
				
				return seconds + r.getString(R.string.second_short);
			}
			return minutes + r.getString(R.string.minute_short);
		}
		Long hour = millis / 3600000;
		if(hour <= 24) {
			if(hour < 2) {
				return hour + "+" + r.getString(R.string.hour_short);
			}
			return hour + r.getString(R.string.hour_short);
		}
		return null;
	}
	
}
