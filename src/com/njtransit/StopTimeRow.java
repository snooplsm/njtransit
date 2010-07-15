package com.njtransit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.njtransit.domain.Stop;

public class StopTimeRow extends LinearLayout {

	private TextView time;
	private TextView duration;
	private TextView away;
	private Stop stop;
	
	public StopTimeRow(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		 time = (TextView)findViewById(R.id.time);
		 duration = (TextView)findViewById(R.id.duration);
		 away = (TextView)findViewById(R.id.away);
	}
	
	public StopTimeRow setStop(Stop stop) {
		this.stop = stop;
		time.setText(format(stop.getDepart(), stop.getArrive()));
		duration.setText(duration(stop));
		return this;
	}
	
	public StopTimeRow setAway(Long from) {
		long diff = stop.getArrive().getTimeInMillis() - from;
		int mins = (int)Math.floor(diff/60000.0D);
		if(mins<0 || mins > 21) {
			
		} else {
			System.out.println("yea");
			away.setText(""+mins +"");
		}
		
		return this;
	}
	
	public static String duration(Stop s) {
		final long diff = s.getArrive().getTimeInMillis()-s.getDepart().getTimeInMillis();	
		long mins = diff / 60000;
		return String.format("%s min", mins);
	}

	private String format(Calendar departing, Calendar arriving) {
		DateFormat f = new SimpleDateFormat("hh:mm aa");
		return String.format("%s - %s", f.format(departing.getTime()), f.format(arriving.getTime()) ).toLowerCase();
	}
}