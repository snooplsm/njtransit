package com.njtransit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
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
	
	public StopImpl(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		Station arrive = session.getArrivalStation();
		Station departure = session.getDepartureStation();
		
		StopsQueryResult sqr = session.getAdapter().getStopTimes(session.getServices(), departure, arrive);
		List<Stop> stops = sqr.getStops();
		
		ArrayList<Stop> today = new ArrayList<Stop>();
		ArrayList<Stop> tomorrow = new ArrayList<Stop>();
		for(Stop stop : sqr.getStops()) {
			Service service = sqr.getTripToService().get(stop.getTripId());
			if(service.isToday()) {
				today.add(stop);
			}
			if(service.isTomorrow()) {
				tomorrow.add(stop);
			}
		}
		
		setAdapter(new ArrayAdapter<Stop>(context, 1, stops) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				Stop stop = getItem(position);
				return ((StopTimeRow)inflater.inflate(R.layout.stop_time_row, null)).setStop(stop);
			}
		});	
	}
}