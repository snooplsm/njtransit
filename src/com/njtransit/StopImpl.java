package com.njtransit;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;
import com.njtransit.domain.Stop;

public class StopImpl extends ListView {

	private Station arrive,departure;
	
	private Session session = Session.get();
	
	private List<Stop> stops;
	
	public StopImpl(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		arrive = session.getArrivalStation();
		departure = session.getDepartureStation();
		
		stops = session.getAdapter().getStopTimes(departure, arrive);
		
		setAdapter(new ArrayAdapter<Stop>(context, 1, stops) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				Stop stop = getItem(position);
				StopTimeRow str = (StopTimeRow)inflater.inflate(R.layout.stop_time_row, null);
				str.setStop(stop);
				return str;
			}
			
		});
		
	}

	
}
