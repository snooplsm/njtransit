package com.njtransit;

import android.app.Activity;

/**
 * Base activity with Typed access to {@link SchedulerApplication}
 * @author dtangren
 */
public class SchedulerActivity extends Activity {	
	public SchedulerApplication getSchedulerContext() {
	  return (SchedulerApplication) getApplicationContext();
	}
}