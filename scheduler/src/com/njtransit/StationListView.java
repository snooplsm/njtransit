package com.njtransit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.njtransit.domain.Station;
import com.njtransit.ui.adapter.StationAdapter;

/**
 * Be sure to assign the adapter externally, this classes adapter requires external dependencies on 
 * the {@link SchedulerApplication}
 * @author dtangren
 *
 */
public class StationListView extends ListView {

	public static interface OnStationListener {
		void onStationSelected(Station station);
	}

	private OnStationListener listener;

	public void setOnStationListener(OnStationListener listener) {
		this.listener = listener;
	}

	public StationListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

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