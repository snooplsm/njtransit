package com.njtransit;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;
import com.njtransit.ui.adapter.StationAdapter;

public class StationListImpl extends ListView {
	
	public static int FIRST_STATION_MODE = 0, SECOND_STATION_MODE=1;
	
	private Integer type;
	
	private int mode;
	
	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	private StationAdapter adapter;
	
	private Session session = Session.get();

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
		NJTransitDBAdapter adapt = new NJTransitDBAdapter(context).open();
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
					session.setDepartureStation(station);
					getContext().startActivity(new Intent(getContext(), StationListHome.class));
				} else {
					// SHOW TIMES!
					session.setArrivalStation(station);
				}
				
			}
			
		});
	}

	public StationListImpl(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StationListImpl(Context context) {
		this(context,null);
	}

}
