package com.njtransit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.njtransit.domain.Stop;

public class StopTimeRow extends LinearLayout {

	private TextView time;
	private TextView duration;
	
	public StopTimeRow(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		 time = (TextView)findViewById(R.id.time);
		 duration = (TextView)findViewById(R.id.duration);
	}
	
	public StopTimeRow setStop(Stop stop) {
		time.setText(format(stop.getArrive(), stop.getDepart()));
		duration.setText(duration(stop));
		return this;
	}
	
	public static String duration(Stop s) {
		final long diff = Math.abs(s.getArrive().getTimeInMillis() - s.getDepart().getTimeInMillis());	
		long mins = diff / 60000;
		return String.format("%s min", mins);
	}

	private String format(Calendar arriving, Calendar departing) {
		DateFormat f = new SimpleDateFormat("hh:mm aa");
		return String.format("%s - %s", f.format(arriving.getTime()), f.format(departing.getTime())).toLowerCase();
	}
}