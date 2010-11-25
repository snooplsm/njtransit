package com.njtransit;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Base activity with Typed access to {@link SchedulerApplication}
 * @author dtangren
 */
public class SchedulerActivity extends Activity {
	
	GoogleAnalyticsTracker tracker;
	
	public SchedulerApplication getSchedulerContext() {
	  return (SchedulerApplication) getApplicationContext();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracker = GoogleAnalyticsTracker.getInstance();		
		tracker.start(getString(R.string.google_ua), this);
		tracker.setDispatchPeriod(60);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tracker.stop();
	}
}