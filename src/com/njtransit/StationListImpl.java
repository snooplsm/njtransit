package com.njtransit;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;
import com.njtransit.ui.adapter.StationAdapter;

public class StationListImpl extends ListView {
	
	public static int FIRST_STATION_MODE = 0, SECOND_STATION_MODE = 1;
	
	private StationAdapter adapter;
	
	private Session session = Session.get();
	
	/** either {@value StationAdapter#ALPHA} or {@value StationAdapter#NEARBY} */
	private Integer type;
	
	/** either {@value #FIRST_STATION_MODE} of {@value #SECOND_STATION_MODE} */
	private int mode;
	
	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
		if(mode == SECOND_STATION_MODE) {
			adapter.remove(session.getDepartureStation());
		}
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
		adapter.setType(type);
		invalidateViews();
	}

	public StationListImpl(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);		
		NJTransitDBAdapter adapt = session.getAdapter();
		session.setStations(adapt.getAllStations());
		adapter = new StationAdapter(context,R.layout.station_row,StationAdapter.ALPHA, session.getStations(), session);
		setAdapter(adapter);
		setType(attrs.getAttributeIntValue(null, "type", StationAdapter.ALPHA));
		setFastScrollEnabled(true);
		setTextFilterEnabled(true);
		setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				
				Station station = adapter.getItem(position);
				
				
				if(mode == FIRST_STATION_MODE) {					
					Toast.makeText(getContext(), station.getName().toLowerCase() + " departure selected", Toast.LENGTH_SHORT).show();
					session.setDepartureStation(station);
					getContext().startActivity(new Intent(getContext(), StationListHome.class));
				} else {
					// SHOW TIMES!
					Toast.makeText(getContext(), station.getName().toLowerCase() + " arrival selected", Toast.LENGTH_SHORT).show();
					session.setArrivalStation(station);
					getContext().startActivity(new Intent(getContext(), StopListHome.class));
				}	
			}
		});
	}
	
	/** called by linflater#inflate */
	public StationListImpl(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
}