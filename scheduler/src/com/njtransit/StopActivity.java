package com.njtransit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.njtransit.domain.Station;

public class StopActivity extends SchedulerActivity {

	private StopListView stopTimes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stop_list_home);
		setTitle(renderTitle(
				getSchedulerContext().getDepartureStation(), getSchedulerContext().getArrivalStation()));
		(stopTimes = (StopListView) findViewById(R.id.list)).startTimer(getSchedulerContext());
	}

	protected void onPause() {
		super.onPause();
		if (stopTimes != null) {
			stopTimes.onPause();
		}
	}

	protected void onResume() {
		super.onResume();
		if (stopTimes != null) {
			stopTimes.onResume();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem reverse = menu.add(Menu.NONE,1,Menu.FIRST, getString(R.string.reverse));
		reverse.setIcon(R.drawable.signpost);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==1) {
			getSchedulerContext().reverseTrip();
			Intent intent = new Intent(this, StopActivity.class);
			startActivity(intent);
		}		
		return super.onOptionsItemSelected(item);
	}

	private String renderTitle(Station departing, Station arriving) {
		return String.format("%s to %s", departing, arriving);
	}
}