package com.njtransit;

import android.content.Context;
import android.util.AttributeSet;
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

	private Session session = Session.get();
	
	private OnStationListener listener;
	
	public void setOnStationListener(OnStationListener listener) {
		this.listener = listener;
	}

//	/** filter the current list down to stations that match name */
//	public StationListView filter(String name) {
//		getStationAdapter().clear();
//		ArrayList<Station> stations = session.getAdapter().getAllStationsLike(
//				name);
//		for (Station s : stations) {
//			getStationAdapter().add(s);
//		}
//		return this;
//	}

	public StationListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setAdapter(new StationAdapter(context, R.layout.station_row,
				StationAdapter.ALPHA, session.getStations(), session));
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

	/** called by inflater#inflate */
	public StationListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public StationAdapter getStationAdapter() {
		return (StationAdapter) getAdapter();
	}
}