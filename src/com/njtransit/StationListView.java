package com.njtransit;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;
import com.njtransit.ui.adapter.StationAdapter;

public class StationListView extends ListView {
	
	public static int FIRST_STATION_MODE = 0, SECOND_STATION_MODE = 1;
	private Session session = Session.get();
	
	/** either {@value StationAdapter#ALPHA} or {@value StationAdapter#NEARBY} */
	private int type = StationAdapter.ALPHA;
	
	/** either {@value #FIRST_STATION_MODE} of {@value #SECOND_STATION_MODE} */
	private int mode;
	
	public int getMode() {
		return mode;
	}
	
	public StationListView setMode(int mode) {
		this.mode = mode;
		if(mode == SECOND_STATION_MODE) {
			getStationAdapter().remove(session.getDepartureStation());
		}
		return this;
	}

	public Integer getType() {
		return type;
	}
	
	public StationListView setType(int type) {
		if(this.type==StationAdapter.FAVORITES && type!=StationAdapter.FAVORITES) {
			getStationAdapter().clear();
			for(Station s : session.getStations()) {
				getStationAdapter().add(s);
			}
		}
		if(type==StationAdapter.FAVORITES) {
			new AsyncTask<Void, Void, ArrayList<Station>>() {

				@Override
				protected ArrayList<Station> doInBackground(Void... params) {
					ArrayList<Station> stations = session.getAdapter().getMostVisitedStations(session, System.currentTimeMillis());
					return stations;
				}

				@Override
				protected void onPostExecute(ArrayList<Station> result) {
					getStationAdapter().clear();
					for(Station s : result) {
						getStationAdapter().add(s);
					}
				}

				
			}.execute();
		}
		this.type = type;
		getStationAdapter().setType(type);
		invalidateViews();
		return this;
	}	
	
	public StationListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);				
		
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
		getContext().startActivity(new Intent(getContext(), StationListActivity.class));
	}
	
	private void onArrivalSelected(Station station) {
		Toast.makeText(getContext(), "arrive at "+station.getName().toLowerCase(), Toast.LENGTH_SHORT).show();
		session.setArrivalStation(station);
		getContext().startActivity(new Intent(getContext(), StopTabActivity.class));
	}
	
	/** called by linflater#inflate */
	public StationListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	private StationAdapter getStationAdapter() {
		return (StationAdapter) getAdapter();
	}
}