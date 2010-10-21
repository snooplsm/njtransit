package com.njtransit;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;
import com.njtransit.ui.adapter.StationAdapter;

public class StationListView extends ListView {

	public static interface OnStationListener {
		void onStationSelected(Station station);
	}

	public static final int FIRST_STATION_MODE = 0, SECOND_STATION_MODE = 1;

	private Session session = Session.get();

	/** either {@value StationAdapter#ALPHA} or {@value StationAdapter#NEARBY} */
	private int type = StationAdapter.ALPHA;

	/** either {@value #FIRST_STATION_MODE} of {@value #SECOND_STATION_MODE} */
	private int mode;
	
	private OnStationListener listener;
	
	public void setOnStationListener(OnStationListener listener) {
		this.listener = listener;
	}

	/** filter the current list down to stations that match name */
	public StationListView filter(String name) {
		getStationAdapter().clear();
		ArrayList<Station> stations = session.getAdapter().getAllStationsLike(
				name);
		for (Station s : stations) {
			getStationAdapter().add(s);
		}
		return this;
	}

	public int getMode() {
		return mode;
	}

	public StationListView setMode(int mode) {
		this.mode = mode;
		if (mode == SECOND_STATION_MODE) {
			getStationAdapter().remove(session.getDepartureStation());
		}
		return this;
	}

	public Integer getType() {
		return type;
	}

	public StationListView setType(int type) {
		if (this.type == StationAdapter.FAVORITES
				&& type != StationAdapter.FAVORITES) {
			getStationAdapter().clear();
			for (Station s : session.getStations()) {
				getStationAdapter().add(s);
			}
		}
		if (type == StationAdapter.FAVORITES) {
			new AsyncTask<Void, Void, ArrayList<Station>>() {
				@Override
				protected ArrayList<Station> doInBackground(Void... params) {
					ArrayList<Station> stations = session.getAdapter()
							.getMostVisitedStations(session,
									System.currentTimeMillis());
					return stations;
				}

				@Override
				protected void onPostExecute(ArrayList<Station> result) {
					getStationAdapter().clear();
					for (Station s : result) {
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
		setAdapter(new StationAdapter(context, R.layout.station_row,
				StationAdapter.ALPHA, session.getStations(), session));
		setType(attrs.getAttributeIntValue(null, "type", StationAdapter.ALPHA));
		setScrollbarFadingEnabled(true);
		setFastScrollEnabled(true);
		setTextFilterEnabled(true);
		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				if(listener!=null) {
					Station station = getStationAdapter().getItem(position);
					listener.onStationSelected(station);
				}
			}
		});
	}

//	private void onDepartureSelected(Station station) {
//		Toast.makeText(getContext(),
//				"depart from " + station.getName().toLowerCase(),
//				Toast.LENGTH_SHORT).show();
//		session.setDepartureStation(station);
//		getContext().startActivity(
//				new Intent(getContext(), StationListActivity.class));
//	}
//
//	private void onArrivalSelected(Station station) {
//		Toast.makeText(getContext(),
//				"arrive at " + station.getName().toLowerCase(),
//				Toast.LENGTH_SHORT).show();
//		session.setArrivalStation(station);
//		getContext()
//				.startActivity(new Intent(getContext(), StopActivity.class));
//	}

	/** called by inflater#inflate */
	public StationListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public StationAdapter getStationAdapter() {
		return (StationAdapter) getAdapter();
	}
}