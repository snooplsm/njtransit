package com.njtransit;

import android.os.Bundle;

public class SettingsActivity extends SchedulerActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		trackPageView(SettingsActivity.class.getSimpleName());
	}

}
