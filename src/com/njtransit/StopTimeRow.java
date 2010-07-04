package com.njtransit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.njtransit.domain.Stop;

public class StopTimeRow extends LinearLayout {

	private TextView arrival, departure;
	private TextView duration;
	private Stop stop;
	
	private static SimpleDateFormat DF = new SimpleDateFormat("hh:mm");
	private static SimpleDateFormat DFAM = new SimpleDateFormat("a");
	
	public StopTimeRow(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		 arrival = (TextView)findViewById(R.id.arrival);
		 departure = (TextView)findViewById(R.id.departure);
		 duration = (TextView)findViewById(R.id.duration);
	}
	
	public void setStop(Stop stop) {
		this.stop = stop;
		arrival.setText(format(stop.getArrive())+" - ");
		departure.setText(format(stop.getDepart()));
		duration.setText(duration(this.stop));
	}
	
	public static String duration(Stop s) {
		final long diff = Math.abs(s.getArrive().getTimeInMillis()-s.getDepart().getTimeInMillis());
		
		long mins = diff / 60000;
		return ""+mins +" min";
	}

	public static String format(Calendar c) {
		Date date = c.getTime();
		return DF.format(date) + " " + Character.toLowerCase(DFAM.format(date).charAt(0));
	}
}
