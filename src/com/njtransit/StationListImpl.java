package com.njtransit;

import java.util.List;

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
	private Session session = Session.get();
	
	/** either {@value StationAdapter#ALPHA} or {@value StationAdapter#NEARBY} */
	private Integer type = StationAdapter.ALPHA;
	
	/** either {@value #FIRST_STATION_MODE} of {@value #SECOND_STATION_MODE} */
	private int mode = FIRST_STATION_MODE;
	
	/** optional user generated query provided by searchable input */
	private String query;
	
	
	public StationListImpl setQuery(String q) {
		this.query = q;
		if(q != null && q.length() > 0) {
			getStationAdapter().clear();
			List<Station> filtered = session.getAdapter().getAllStationsLike(query);
			for(Station s:filtered) {
				getStationAdapter().add(s);
			}
			getStationAdapter().setType(getType());
			invalidateViews();
		}
		return this;
	}
	
	
	
	public int getMode() {
		return mode;
	}
	
	public StationListImpl setMode(int mode) {
		this.mode = mode;
		if(mode == SECOND_STATION_MODE) {
			getStationAdapter().remove(session.getDepartureStation());
		}
		return this;
	}

	public Integer getType() {
		return type;
	}
	
	public StationListImpl setType(Integer type) {
		this.type = type;
		getStationAdapter().setType(type);
		invalidateViews();
		return this;
	}	
	
	public StationListImpl(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);		
		DatabaseAdapter db = session.getAdapter();
		session.setStations(db.getAllStations());
		
		// TODO base default type on preference
		setAdapter(new StationAdapter(context, R.layout.station_row, StationAdapter.ALPHA, session.getStations(), session));
		setType(attrs.getAttributeIntValue(null, "type", StationAdapter.ALPHA));
		
		setScrollbarFadingEnabled(true);
		setFastScrollEnabled(true);
		setTextFilterEnabled(true);
		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				Station station = getStationAdapter().getItem(position);
				if(mode == FIRST_STATION_MODE) {	
					onDepartureSelected(station);
				} else {
					onArrivalSelected(station);
				}	
			}
		});
	}
	
	private void onDepartureSelected(Station station){
		Toast.makeText(getContext(), "depart from " + station.getName().toLowerCase(), Toast.LENGTH_SHORT).show();
		session.setDepartureStation(station);
		getContext().startActivity(new Intent(getContext(), StationListHome.class));
	}
	
	private void onArrivalSelected(Station station) {
		Toast.makeText(getContext(), "arrive at "+station.getName().toLowerCase(), Toast.LENGTH_SHORT).show();
		session.setArrivalStation(station);
		getContext().startActivity(new Intent(getContext(), StopListHome.class));
	}
	
	/** called by linflater#inflate */
	public StationListImpl(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	private StationAdapter getStationAdapter() {
		return (StationAdapter) getAdapter();
	}
}