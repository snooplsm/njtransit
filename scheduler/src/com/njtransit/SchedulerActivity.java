package com.njtransit;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.njtransit.rail.R;

/**
 * Base activity with Typed access to {@link SchedulerApplication}
 * 
 * @author dtangren
 */
public class SchedulerActivity extends Activity {

	private GoogleAnalyticsTracker tracker;

	public SchedulerApplication getSchedulerContext() {
		return (SchedulerApplication) getApplicationContext();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracker = GoogleAnalyticsTracker.getInstance();
		try {
			tracker.setProductVersion(getPackageName(), getPackageManager()
					.getPackageInfo(getPackageName(), 0).versionName);
		} catch (Exception e) {

		}
		tracker.start(getString(R.string.google_ua), this);
		tracker.setDispatchPeriod(60);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tracker.stop();
	}

	protected void trackPageView(String url) {
		try {
			tracker.trackPageView(url);
			SchedulerApplication app = getSchedulerContext();
			DeviceInformation info = app.getDeviceInformation();
			tracker.trackPageView(info.getUuid()+"/"+url);
		} catch (Exception e) {

		}
	}

	protected void trackEvent(String category, String action, String label,
			int value) {

		try {
			tracker.trackEvent(category, action, label, value);
			SchedulerApplication app = getSchedulerContext();
			DeviceInformation info = app.getDeviceInformation();
			tracker.trackEvent(info.getUuid() + ":"+ category, action, label, value);
		} catch (Exception e) {

		}
	}
}
