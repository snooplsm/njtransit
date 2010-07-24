package com.njtransit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;

public class StopActivity extends Activity {

	private Session session = Session.get();

	private StopListView stopTimes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stop_list_home);
		setTitle(renderTitle(
				session.getDepartureStation(), session.getArrivalStation()));
		stopTimes = (StopListView) findViewById(R.id.list);
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
		menu.add(Menu.NONE,1,Menu.FIRST,"Reverse");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==1) {
			session.reverseTrip();
			Intent intent = new Intent(this, StopActivity.class);
			startActivity(intent);
		}		
		return super.onOptionsItemSelected(item);
	}

	private String renderTitle(Station departing, Station arriving) {
		return String.format("%s to %s", departing, arriving);
	}
}