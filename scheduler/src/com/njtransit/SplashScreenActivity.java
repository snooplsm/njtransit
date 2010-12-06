package com.njtransit;

import java.util.Calendar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.admob.android.ads.AdManager;

public class SplashScreenActivity extends SchedulerActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		new AsyncTask<Object, Void, DatabaseAdapter>() {

			@Override
			protected DatabaseAdapter doInBackground(Object... params) {
				DatabaseAdapter adapter = new DatabaseAdapter(
						getApplicationContext()).open();
				return adapter;
			}

			@Override
			protected void onPostExecute(DatabaseAdapter result) {
				SchedulerApplication app = getSchedulerContext();
				app.setAdapter(result);
				app.setStations(result.getStations());

				if (Root.getScheduleEndDate(getApplicationContext()) < 0) {
					long max = result.getMaxCalendarDate();
					Root.saveScheduleEndDate(getApplicationContext(), max);
				}
				if (Root.getScheduleStartDate(getApplicationContext()) < 0) {
					long min = result.getMinCalendarDate();
					Root.saveScheduleStartDate(getApplicationContext(), min);
				}
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				if (cal.getTimeInMillis() > Root
						.getScheduleEndDate(getApplicationContext())) {
				}
				AdManager.setAllowUseOfLocation(true);
				AdManager.setTestDevices(new String[] { "0A3A55190402402C",
						AdManager.TEST_EMULATOR });
				startActivity(new Intent(SplashScreenActivity.this,
						MainActivity.class));
				finish();
			}

		}.execute();
	}

}
