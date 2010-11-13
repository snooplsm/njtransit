package com.njtransit;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import com.njtransit.JumpDialog.OnJumpListener;
import com.njtransit.StationListView.OnStationListener;
import com.njtransit.domain.Session;
import com.njtransit.domain.Station;

/**
 * Holds two tabs for selecting stations (in alphabetical order or by
 * proximity). This activity provides a means of selecting first a departure
 * station and then a destination station before forwarding control to the
 * StopListHome activity.
 */
public class StationListActivity extends Activity implements OnJumpListener {

	private StationListView stations;

	private Set<Character> stationLetters = new HashSet<Character>();
	
	private Session session = Session.get();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.station_list_home);
		stations = (StationListView) findViewById(R.id.station_view);
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
				for (Station s : session.getStations()) {
					stationLetters.add(s.getName().charAt(0));
				}
			}
		}
		return stationLetters;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// if(stations.getType()==StationAdapter.FAVORITES) {
		// MenuItem clear =
		// menu.add(Menu.NONE,1,Menu.FIRST,getString(R.string.clear_favorites));
		// clear.setIcon(R.drawable.clear_favorites);
		// }
		return true;
	}

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// if(item.getItemId()==1) {
	// new AsyncTask<Void, Void, Void>() {
	//
	// @Override
	// protected void onPostExecute(Void result) {
	// //getTabHost().setCurrentTab(0);
	// }
	//
	// @Override
	// protected void onPreExecute() {
	// Toast.makeText(getApplicationContext(), "Clearing Favorites",
	// Toast.LENGTH_SHORT).show();
	// }
	//
	// @Override
	// protected Void doInBackground(Void... params) {
	// session.getAdapter().deleteFavorites();
	// return null;
	// }
	//
	// }.execute();
	// }
	// return super.onOptionsItemSelected(item);
	// }

}