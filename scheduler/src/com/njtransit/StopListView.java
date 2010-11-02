package com.njtransit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.njtransit.domain.IService;
import com.njtransit.domain.Session;
import com.njtransit.domain.Station;
import com.njtransit.domain.Stop;
import com.njtransit.model.StopsQueryResult;
import com.njtransit.ui.adapter.StopAdapter;

/** List of StopTimeRows */
public class StopListView extends ListView implements Traversable<StopTimeRow> {

	private Session session = Session.get();

	private TimerTask updaterThread = null;

	private boolean needsReschedule = false;

	private Timer timer;

	private List<Stop> stops = new ArrayList<Stop>();
	
	private Long started = System.currentTimeMillis();

	public StopListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setScrollbarFadingEnabled(true);
		setSmoothScrollbarEnabled(true);

		setAdapter(new StopAdapter(this, getContext(), stops));

		new AsyncTask<Void, Void, Stop>() {

			ProgressDialog progress = null;

			@Override
			protected Stop doInBackground(Void... params) {
				final Station arrive = session.getArrivalStation();
				final Station departure = session.getDepartureStation();

				final StopsQueryResult sqr = session.getAdapter().getStopTimes(session.getServices(), departure, arrive);

				final ArrayList<Stop> today = new ArrayList<Stop>();
				final ArrayList<Stop> tomorrow = new ArrayList<Stop>();
				final Long now = System.currentTimeMillis();
				
				Stop closest = null;
				Long closestDiff = Long.MAX_VALUE;
				for (Stop stop : sqr.getStops()) {
					IService service = sqr.getTripToService()
							.get(stop.getTripId());
					if (service.isToday()) {
						Long diff = now
								- stop.getDepart()
										.getTimeInMillis();
						if (diff > 0 && diff < now
								&& diff < closestDiff) {
							closest = stop;
							closestDiff = diff;
						}
						today.add(stop);
					} else {
						tomorrow.add(stop);
					}
				}
				today.addAll(tomorrow);

				stops.addAll(today);

				return closest;
			}

			@Override
			protected void onPreExecute() {
				progress = ProgressDialog.show(getContext(), "Please wait...",
						"Loading stop times ...", true);
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void onPostExecute(Stop closest) {
				progress.dismiss();
				if (closest != null) {
					setSelectionFromTop(
							((ArrayAdapter<Stop>) getAdapter())
									.getPosition(closest),
							10);
				}
				timer = new Timer(false);
				Calendar c = Calendar.getInstance();
				c.clear(Calendar.MILLISECOND);
				c.clear(Calendar.SECOND);
				c.set(Calendar.MINUTE, c.get(Calendar.MINUTE)+1);
				timer.scheduleAtFixedRate(newUpdaterThread(), c.getTime(), 60000);
				new Thread() {
					@Override
					public void run() {
						session.getAdapter().saveHistory(session.getDepartureStation().getId(), session.getArrivalStation().getId(), started);
					}
				}.start();
			}

		}.execute();
	}

	/**
	 * Refactored this so that it uses StopAdapter's cache.  ListViews only need to display 10 items at a time, so on first go around we only need to iterate over whats been displayed.
	 */
	@Override
	public void foreach(Fn<StopTimeRow> f) {
		for(StopTimeRow str : ((StopAdapter)getAdapter()).getCache()) {
			f.apply(str);
		}
	}

	private TimerTask newUpdaterThread() {
		updaterThread = new TimerTask() {

			boolean changed;
			
			@Override
			public void run() {
				changed = false;
				Fn<StopTimeRow> updateAway = new Fn<StopTimeRow>() {					
					public void apply(final StopTimeRow r) {
						String away = null;
							if((away = r.getAway(StopListView.this))!=null) {
								changed = true;
								final String awy = away;
								r.post(new Runnable() {

									@Override
									public void run() {
										r.setAway(awy);
									}
									
								});
							}
						}
					};
				foreach(updateAway);
				if(changed) {
					postInvalidate();
				}
			}			
		};
		return updaterThread;
	}

	public void onResume() {
		if (timer != null && needsReschedule) {
			timer = new Timer(false);
			timer.schedule(newUpdaterThread(), 300);
			Calendar c = Calendar.getInstance();
			c.clear(Calendar.MILLISECOND);
			c.clear(Calendar.SECOND);
			c.set(Calendar.MINUTE, c.get(Calendar.MINUTE)+1);
			timer.scheduleAtFixedRate(newUpdaterThread(), c.getTime(), 60000);
		}
	}

	public void onPause() {
		try {
			if (timer != null) {
				needsReschedule = true;
				timer.cancel();
			}
		} catch (Exception e) {
			Log.w("error", "onResume");
		}
	}
}