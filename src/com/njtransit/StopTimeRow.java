package com.njtransit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.njtransit.domain.Stop;

public class StopTimeRow extends LinearLayout {

	private TextView time;
	private TextView duration;
	private TextView away;
	
	private int lastAway=Integer.MAX_VALUE;
	
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
		return this;
	}
	
	private void populateDuration() {
		duration.setText(duration());
	}
	
	public boolean setAway(ListView parent) {
		try {
			long diff = stop.getDepart().getTimeInMillis() - System.currentTimeMillis();
			int mins = (int)diff/60000;
			boolean changed = false;
			if(lastAway!=mins && lastAway > -60) {
				changed = true;
				lastAway = mins;
				if(mins > 0 && mins < 61) {
					if(mins<=61) {
						away.setTextColor(getResources().getColor(R.color.red_light));
						away.postInvalidate();
					}
					away.setText(String.valueOf(mins));
				} else {
					away.setText("");
				}
				populateDuration();
			}
			return changed;
		}catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}
	
	private static final String DURATION_ONLY = "%s mins";
	
	private static final String DURATION_AND_DEPARTURE = "%s mins";
	
	public Long getDepartureInSeconds() {
		return System.currentTimeMillis()-stop.getDepart().getTimeInMillis() / 1000;
	}
	
	public String duration() {
		final long diff = stop.getArrive().getTimeInMillis()-stop.getDepart().getTimeInMillis();	
		long mins = diff / 60000;
		Long departureInSeconds = getDepartureInSeconds() / 60;
		if(departureInSeconds < 0 || departureInSeconds>60) {
			departureInSeconds = null;
		}
		
		if(departureInSeconds==null) {
			return String.format(DURATION_ONLY, mins);
		} else {
			return String.format(DURATION_AND_DEPARTURE, mins);
		}
	}

	private String format(Calendar departing, Calendar arriving) {
		DateFormat f = new SimpleDateFormat("hh:mm aa");
		String depart =  f.format(departing.getTime());
		String arrive = f.format(arriving.getTime());
		return String.format("%s - %s", depart, arrive ).toLowerCase();
	}
}