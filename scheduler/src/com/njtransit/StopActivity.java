package com.njtransit;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.njtransit.domain.IService;
import com.njtransit.domain.Station;
import com.njtransit.domain.Stop;
import com.njtransit.model.StopsQueryResult;
import com.njtransit.ui.adapter.StopAdapter;


public class StopActivity extends SchedulerActivity implements Traversable<StopTimeRow> {

	private StopListView stopTimes;
	
	private TimerTask updaterThread = null;

	private boolean needsReschedule = false;
	
	private TextView departure;
	private TextView arrival;

	private TextView errors;
	
	private Timer timer;
	
	private List<Stop> stops = new ArrayList<Stop>();

	public static final String DEPARTURE_ID = "departure-id";
	public static final String ARRIVAL_ID = "arrival-id";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stop_list_home);
//		int departureId = savedInstanceState.getInt(DEPARTURE_ID);
//		int arrivalId = savedInstanceState.getInt(ARRIVAL_ID);
		Station departure = getSchedulerContext().getDepartureStation();
		Station arrival = getSchedulerContext().getArrivalStation();
		this.departure = (TextView)findViewById(R.id.departureText);
		this.arrival = (TextView)findViewById(R.id.arrivalText);
		populateStationsHeader(departure,arrival);
		stopTimes = (StopListView) findViewById(R.id.list);
		errors = (TextView) findViewById(R.id.errors);
		findAndShowStops(departure,arrival);
		tracker.trackPageView("/"+getClass().getSimpleName()+"/"+departure.getName()+"_to_"+arrival.getName());
	}
	
	private void populateStationsHeader(Station departure, Station arrival) {
		this.departure.setText(departure.getName());
		this.arrival.setText(arrival.getName());
	}

	private void findAndShowStops(Station depart, Station arrive) {
		final Station departure;
		final Station arrival;
		// if depart is null or arrive is null then use testing values.
		final boolean useMockData;
		if(depart!=null) {
			departure = depart;
			useMockData = false;
		}else {
			departure = new Station(148,"Trenton Transit Center",72.5,72.5);
			useMockData = true;
			getSchedulerContext().setDepartureStation(departure);
		}
		if(arrive!=null) {
			arrival = arrive;
		} else {
			arrival = new Station(105, "New York Penn Station", 72.5,72.5);
			getSchedulerContext().setArrivalStation(arrival);
		}
		if(useMockData) {
			populateStationsHeader(departure, arrival);
			//stations.setText(renderTitle(departure,arrival));
		}
		new AsyncTask<Void, Void, StopResult>() {

			ProgressDialog progress = null;

			@Override
			protected StopResult doInBackground(Void... params) {

				final StopsQueryResult sqr = getSchedulerContext().getAdapter().getStopTimesAlternate(departure, arrival,useMockData);

				final ArrayList<Stop> today = new ArrayList<Stop>();
				final ArrayList<Stop> tomorrow = new ArrayList<Stop>();
				final Long now = System.currentTimeMillis();
				
				Stop closest = null;
				Long closestDiff = Long.MAX_VALUE;
				for (Stop stop : sqr.getStops()) {
					IService service = sqr.getTripToService()
							.get(stop.getTripId());
					Calendar relativeTime = Calendar.getInstance();
					relativeTime.set(Calendar.HOUR_OF_DAY,stop.getDepart().get(Calendar.HOUR_OF_DAY));
					relativeTime.set(Calendar.MINUTE, stop.getDepart().get(Calendar.MINUTE));
					if (service.isToday()) {	
						Long diff =  now - relativeTime.getTimeInMillis();
						
						if (diff > 0 && diff < now
								&& diff < closestDiff) {
							closest = stop;
							closestDiff = diff;
						}
						today.add(stop);
					} 
					if (service.isTomorrow()) {
						relativeTime.add(Calendar.DAY_OF_YEAR, 1);
						Long diff =  now - relativeTime.getTimeInMillis();
						
						if (diff > 0 && diff < now
								&& diff < closestDiff) {
							closest = stop;
							closestDiff = diff;
						}
						tomorrow.add(stop);
					}
					
				}
				Comparator<Stop> comparator = new Comparator<Stop>() {

					@Override
					public int compare(Stop o1, Stop o2) {
						return o1.getDepart().compareTo(o2.getDepart());
					}
					
				};
				Collections.sort(today,comparator);
				Collections.sort(tomorrow,comparator);
				today.addAll(tomorrow);

				
				stops.addAll(today);

				return new StopResult(sqr,closest);
			}

			@Override
			protected void onPreExecute() {
				progress = ProgressDialog.show(StopActivity.this, "Please wait",
						"Loading schedule ...", true);
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void onPostExecute(StopResult result) {
				progress.dismiss();
				Stop closest = result.getClosest();				
				if(!stops.isEmpty()) {
					StopAdapter stopAdapter = new StopAdapter(StopActivity.this,stops);
					stopTimes.setAdapter(stopAdapter);
					if (closest != null) {
						stopTimes.setSelectionFromTop(
								((ArrayAdapter<Stop>) stopTimes.getAdapter())
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
							try {
								if(getSchedulerContext().getDepartureStation()!=null && getSchedulerContext().getArrivalStation()!=null) {
									getSchedulerContext().getAdapter().saveHistory(getSchedulerContext().getDepartureStation().getId(), getSchedulerContext().getArrivalStation().getId(), System.currentTimeMillis());
								}
							} catch (Exception e) {
								Log.e(getClass().getSimpleName(), "could not log saveHistory",e);
							}
						}
					}.start();
				} else {
					String address = String.format("feedback_%s_%s@wmwm.us",getSchedulerContext().getDepartureStation().getId(),getSchedulerContext().getArrivalStation().getId());
					String question = String.format("We were unable to find any results for your search criteria.  Keep in mind that this not a trip planning application and does not support connections.  Still, if you believe this is a bug please email %s and we will look into it.",address);
					stopTimes.setVisibility(View.GONE);
					errors.setVisibility(View.VISIBLE);
					errors.setText(question);
				}	
				tracker.trackEvent("stop_times", "query", stops.size() +" stops for " + departure.getName() + " to " + arrival.getName() + " in " + result.getStopQueryResult().getQueryDuration(), 0);
			}
			
		}.execute();
	}
	

	/**
	 * Refactored this so that it uses StopAdapter's cache.  ListViews only need to display 10 items at a time, so on first go around we only need to iterate over whats been displayed.
	 */
	@Override
	public void foreach(Fn<StopTimeRow> f) {
		for(int i = 0;i<stopTimes.getChildCount(); i++) {
		//for(StopTimeRow str : stopTimes.getChildCount()) {
			try {
				Object o = stopTimes.getChildAt(i);
				if(o instanceof StopTimeRow) {
					StopTimeRow row = (StopTimeRow)o;
				}
			} catch (Exception e) {
				//don't care
			}
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
							if((away = r.getAway(stopTimes))!=null) {
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
					stopTimes.postInvalidate();
				}
			}			
		};
		return updaterThread;
	}

	protected void onPause() {
		super.onPause();
		if (stopTimes != null) {
			try {
				if (timer != null) {
					needsReschedule = true;
					timer.cancel();
				}
			} catch (Exception e) {
				Log.w("error", "onPause",e);
			}
		}
	}

	protected void onResume() {
		super.onResume();
		if (stopTimes != null) {
			try {
				if (timer != null && needsReschedule) {
			
					timer = new Timer(false);
					timer.schedule(newUpdaterThread(), 300);
					Calendar c = Calendar.getInstance();
					c.add(Calendar.MINUTE, c.get(Calendar.MINUTE)+1);
					timer.scheduleAtFixedRate(newUpdaterThread(), c.getTime(), 60000);
				}
			}catch (Exception e) {
				Log.e(getClass().getSimpleName(), "onResume",e);
			}
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
}