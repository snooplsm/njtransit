package com.njtransit;

import java.text.DecimalFormat;
import java.util.Calendar;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.admob.android.ads.AdManager;
import com.njtransit.DatabaseAdapter.InstallDatabaseMeter;

public class SplashScreenActivity extends SchedulerActivity {

	private static class Pair {
		
		public Pair(TextView textView, String value) {
			super();
			this.textView = textView;
			this.value = value;
		}
		private TextView textView;
		private String value;
		public TextView getTextView() {
			return textView;
		}
		public String getValue() {
			return value;
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		final TextView progressText = (TextView) findViewById(R.id.progressText);
		final TextView progressPercent = (TextView) findViewById(R.id.progressPercent);
		
		new AsyncTask<Object, Pair, DatabaseAdapter>() {

			@Override
			protected DatabaseAdapter doInBackground(Object... params) {
				
				final InstallDatabaseMeter meter = new InstallDatabaseMeter() {

					private DecimalFormat df = new DecimalFormat("##%");

					@Override
					public void onBeforeCopy() {
						publishProgress(new Pair(progressText,"Copying Database"));
					}

					@Override
					public void onFinishedCopying() {
						publishProgress(new Pair(progressText,""), new Pair(progressPercent,""));

					}

					@Override
					public void onPercentCopied(long copySize, final float percent,
							long totalBytesCopied) {
						publishProgress(new Pair(progressPercent,df.format(percent)));

					}

					@Override
					public void onSizeToBeCopiedCalculated(long copySize) {

					}

				};

				DatabaseAdapter adapter = new DatabaseAdapter(
						getApplicationContext(), meter).open();
				return adapter;
			}

			@Override
			protected void onProgressUpdate(Pair... values) {
				for(Pair p : values) {
					p.textView.setText(p.value);
				}
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
