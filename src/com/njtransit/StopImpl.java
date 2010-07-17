package com.njtransit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.njtransit.domain.Service;
import com.njtransit.domain.Session;
import com.njtransit.domain.Station;
import com.njtransit.domain.Stop;
import com.njtransit.model.StopsQueryResult;

public class StopImpl extends ListView {
	
	private Session session = Session.get();
	
	public static SimpleDateFormat DF = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
	
	private Map<Integer, StopTimeRow> stopRows = new HashMap<Integer,StopTimeRow>();
	
	private TimerTask updaterThread = null;
	
	private boolean needsReschedule = false;
	
	private Timer timer;
	
	public StopImpl(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Station arrive = session.getArrivalStation();
		Station departure = session.getDepartureStation();
		
		StopsQueryResult sqr = session.getAdapter().getStopTimes(session.getServices(), departure, arrive);
		
		ArrayList<Stop> today = new ArrayList<Stop>();
		ArrayList<Stop> tomorrow = new ArrayList<Stop>();
		Stop closest = null;
		Long closestDiff = Long.MAX_VALUE;
		final Long now = System.currentTimeMillis();
		for(Stop stop : sqr.getStops()) {
			Service service = sqr.getTripToService().get(stop.getTripId());

			if(service.isToday()) {
				Long diff = now - stop.getDepart().getTimeInMillis();
				if(diff > 0 && diff < now && diff < closestDiff) {
					closest = stop;
					closestDiff = diff;
				}
				today.add(stop);
			}
			if(service.isTomorrow()) {
				tomorrow.add(stop);
			}
		}		
		
		today.addAll(tomorrow);
		ArrayAdapter<Stop> adapter;
		setAdapter(adapter = new ArrayAdapter<Stop>(context, 1,today) {
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				//TODO: figure out a way to recycle to reduce latency
				if(stopRows.containsKey(position)) {
					return stopRows.get(position);
				}
				LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				Stop stop = getItem(position);
				try {
					StopTimeRow row = ((StopTimeRow)inflater.inflate(R.layout.stop_time_row, null)).setStop(stop).setAway(now);
					stopRows.put(position,row);
					return row;
				}catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		if(closest!=null) {
			setSelectionFromTop(adapter.getPosition(closest),10);
		}
		
		timer = new Timer(false);
		timer.scheduleAtFixedRate(newUpdaterThread(), 7000, 7000);
	}
	
	private TimerTask newUpdaterThread() {
		updaterThread = new TimerTask() {

			@Override
			public void run() {
				Long now = System.currentTimeMillis();
				for(StopTimeRow row : stopRows.values()) {
					row.setAway(now);
				}
			}			
		};
		return updaterThread;
	}
	
	public void onResume() {
		if(timer!=null && needsReschedule) {
			timer = new Timer(false);			
			timer.scheduleAtFixedRate(newUpdaterThread(), 100, 7000);
		}
	}
	
	public void onPause() {
		
		try {
			if(timer!=null) {
				needsReschedule = true;
				timer.cancel();
			}
		}catch (Exception e) {
			Log.w("error", "onResume");
		}
	}

}