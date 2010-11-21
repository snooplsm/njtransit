package com.njtransit;

import java.util.HashSet;
import java.util.Set;

import android.content.Intent;
import android.os.Bundle;

import com.njtransit.JumpDialog.OnJumpListener;
import com.njtransit.StationListView.OnStationListener;
import com.njtransit.domain.Station;
import com.njtransit.ui.adapter.StationAdapter;

/**
 * List for display all the stations in a particular order.
 * 
 * @author rgravener
 *
 */
public class StationListActivity extends SchedulerActivity implements OnJumpListener {

	private final Set<Character> stationLetters = new HashSet<Character>();

	private StationListView stations;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.station_list_home);
		final SchedulerApplication app = this.getSchedulerContext();
		stations = (StationListView) findViewById(R.id.station_view);
		stations.setAdapter(new StationAdapter(this, R.layout.station_row,
				StationAdapter.ALPHA, app.getStations(), app));
		stations.setOnStationListener(new OnStationListener() {
			@Override
			public void onStationSelected(Station station) {
				Intent i = new Intent();
				i.putExtra("stationId", station.getId());
				setResult(RESULT_OK, i);
				finish();
			}
		});
		
		new JumpDialog(this, this).only(getStationLetters())
				.inRowsOf(5).show();
	}

	@Override
	public void onJump(Character c) {
		Object[] sections = stations.getStationAdapter().getSections();
		for(int i = 0; i < sections.length; i++) {
			if(sections[i].equals(c)) {
				int pos = stations.getStationAdapter().getPositionForSection(i);
				stations.setSelectionFromTop(pos,0);
				break;
			}
		}
	}
	
	private Set<Character> getStationLetters() {
		synchronized (this) {
			if (stationLetters.isEmpty()) {
				for (Station s : getSchedulerContext().getStations()) {
					stationLetters.add(s.getName().charAt(0));
				}
			}
		}
		return stationLetters;
	}
}