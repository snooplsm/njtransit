package com.njtransit;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.admob.android.ads.AdView;
import com.admob.android.ads.SimpleAdListener;
import com.njtransit.domain.IService;
import com.njtransit.domain.Station;
import com.njtransit.domain.Stop;
import com.njtransit.model.StopsQueryResult;
import com.njtransit.rail.R;
import com.njtransit.ui.adapter.StopAdapter;


public class StopActivity extends SchedulerActivity {

	private StopListView stopTimes;
	
	private TimerTask updaterThread = null;

	private boolean needsReschedule = false;
	
	private TextView departure;
	private TextView arrival;

	private TextView errors;
	
	private Timer timer;
	
	ProgressDialog progress = null;
	
	private boolean needProgress;
	
	private int refreshCount = 0;
	
	private List<Stop> stops = new ArrayList<Stop>();

	public static final String DEPARTURE_ID = "departure-id";
	public static final String ARRIVAL_ID = "arrival-id";
	
	private Map<TextView,Integer> minutesAway = new HashMap<TextView,Integer>();
	
	private AdView ad;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.stop_list_home);

		Station departure = getSchedulerContext().getDepartureStation();
		Station arrival = getSchedulerContext().getArrivalStation();
		this.departure = (TextView)findViewById(R.id.departureText);
		this.arrival = (TextView)findViewById(R.id.arrivalText);				
		stopTimes = (StopListView) findViewById(R.id.list);
		errors = (TextView) findViewById(R.id.errors);
		findAndShowStops(departure,arrival);		
	    ad = (AdView) findViewById(R.id.ad);
	    ad.setAdListener(new SimpleAdListener() {

			@Override
			public void onReceiveAd(AdView arg0) {			
				super.onReceiveAd(arg0);
				ad.setVisibility(View.VISIBLE);
			}

			@Override
			public void onFailedToReceiveAd(AdView arg0) {
				// TODO Auto-generated method stub
				super.onFailedToReceiveAd(arg0);
			}
	    	
	    });		
	}
	
	Comparator<Stop> comparator = new Comparator<Stop>() {

		@Override
		public int compare(Stop o1, Stop o2) {
			return o1.getDepart().compareTo(o2.getDepart());
		}
		
	};
	
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

			@Override
			protected StopResult doInBackground(Void... params) {

				final StopsQueryResult sqr ;
				if(getSchedulerContext().getDepartureDate()!=null) {
					sqr = getSchedulerContext().getAdapter().getStopTimesAlternate(departure, arrival,useMockData,getSchedulerContext().getDepartureDate());
				} else {
					sqr = getSchedulerContext().getAdapter().getStopTimesAlternate(departure, arrival,useMockData);
				}
				
				final ArrayList<Stop> today = new ArrayList<Stop>();
				final ArrayList<Stop> tomorrow = new ArrayList<Stop>();
				final Long now = System.currentTimeMillis();
				final Calendar day = Calendar.getInstance();
				
				Stop closest = null;
				Long closestDiff = Long.MAX_VALUE;
				if(!(day.get(Calendar.YEAR)==sqr.getDepartureDate().get(Calendar.YEAR) && day.get(Calendar.DAY_OF_YEAR)==sqr.getDepartureDate().get(Calendar.DAY_OF_YEAR))) {
					for(Stop stop :sqr.getStops()) {
						IService service = sqr.getTripToService()
								.get(stop.getTripId());
						if(service.isDate(sqr.getDepartureDate())) {
							Calendar newDepart = Calendar.getInstance();
							newDepart.setTimeInMillis(stop.getDepart().getTimeInMillis());
							Calendar newArrive = Calendar.getInstance();
							newArrive.setTimeInMillis(stop.getArrive().getTimeInMillis());
							newDepart.set(Calendar.YEAR,sqr.getDepartureDate().get(Calendar.YEAR));
							newDepart.set(Calendar.DAY_OF_YEAR, sqr.getDepartureDate().get(Calendar.DAY_OF_YEAR));
							newArrive.set(Calendar.YEAR,sqr.getDepartureDate().get(Calendar.YEAR));
							newArrive.set(Calendar.DAY_OF_YEAR, sqr.getDepartureDate().get(Calendar.DAY_OF_YEAR));
							if(stop.getDepart().get(Calendar.DAY_OF_YEAR) < stop.getArrive().get(Calendar.DAY_OF_YEAR)) {
								newArrive.add(Calendar.DAY_OF_YEAR, 1);
							}
							stops.add(stop);
						}						
					}
					Collections.sort(stops,comparator);
				} else {
					Calendar relativeTime = Calendar.getInstance();
					Calendar tomorrowDate = Calendar.getInstance();
					tomorrowDate.add(Calendar.DAY_OF_YEAR, 1);
					for (Stop stop : sqr.getStops()) {
						IService service = sqr.getTripToService()
								.get(stop.getTripId());
						//relativeTime.set(Calendar.HOUR_OF_DAY,stop.getDepart().get(Calendar.HOUR_OF_DAY));
						//relativeTime.set(Calendar.MINUTE, stop.getDepart().get(Calendar.MINUTE));			
						if (service.isToday()) {	
							Calendar newDepart = Calendar.getInstance();
							newDepart.setTimeInMillis(stop.getDepart().getTimeInMillis());
							Calendar newArrive = Calendar.getInstance();
							newArrive.setTimeInMillis(stop.getArrive().getTimeInMillis());
							newDepart.set(Calendar.YEAR,relativeTime.get(Calendar.YEAR));
							newDepart.set(Calendar.DAY_OF_YEAR, relativeTime.get(Calendar.DAY_OF_YEAR));
							newArrive.set(Calendar.YEAR,relativeTime.get(Calendar.YEAR));
							newArrive.set(Calendar.DAY_OF_YEAR, relativeTime.get(Calendar.DAY_OF_YEAR));
							if(stop.getDepart().get(Calendar.DAY_OF_YEAR) < stop.getArrive().get(Calendar.DAY_OF_YEAR)) {
								newArrive.add(Calendar.DAY_OF_YEAR, 1);
							}
							Long diff =  newDepart.getTimeInMillis() - relativeTime.getTimeInMillis();
							
							if(diff>-5400001) {
								Stop newStop = new Stop(stop.getTripId(),newDepart,newArrive); 
								if (diff > 0
										&& diff < closestDiff) {
									closest = newStop;
									closestDiff = diff;
								}
								today.add(newStop);
							}
						} 
						if (service.isTomorrow()) {							
							if(stop.getDepart().get(Calendar.HOUR_OF_DAY)<6) {
								Calendar tom = Calendar.getInstance();
								tom.setTimeInMillis(tomorrowDate.getTimeInMillis());
								Calendar newDepart = Calendar.getInstance();
								newDepart.setTimeInMillis(stop.getDepart().getTimeInMillis());
								Calendar newArrive = Calendar.getInstance();
								newArrive.setTimeInMillis(stop.getArrive().getTimeInMillis());
								newDepart.set(Calendar.YEAR,tom.get(Calendar.YEAR));
								newDepart.set(Calendar.DAY_OF_YEAR, tom.get(Calendar.DAY_OF_YEAR));
								newArrive.set(Calendar.YEAR,tom.get(Calendar.YEAR));
								newArrive.set(Calendar.DAY_OF_YEAR, tom.get(Calendar.DAY_OF_YEAR));
								if(stop.getDepart().get(Calendar.DAY_OF_YEAR) < stop.getArrive().get(Calendar.DAY_OF_YEAR)) {
									newArrive.add(Calendar.DAY_OF_YEAR, 1);
								}
								Stop newStop = new Stop(stop.getTripId(), newDepart, newArrive);
								Long diff =  newDepart.getTimeInMillis() - relativeTime.getTimeInMillis();
								if (diff > 0 
										&& diff < closestDiff) {
									closest = stop;
									closestDiff = diff;
								}
								tomorrow.add(newStop);
							}
						}
						
					}					
					Collections.sort(today,comparator);
					Collections.sort(tomorrow,comparator);
					today.addAll(tomorrow);

					
					stops.addAll(today);
					stops.isEmpty();
				}


				return new StopResult(sqr,closest);
			}

			@Override
			protected void onPreExecute() {
				trackPageView(getClass().getSimpleName()+"/"+departure.getName()+"_to_"+arrival.getName());
				populateStationsHeader(departure,arrival);
				progress = ProgressDialog.show(StopActivity.this, "Please wait",
						"Loading schedule ...", true);
				needProgress = true;
			}

			@Override
			protected void onCancelled() {
				progress.cancel();
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void onPostExecute(StopResult result) {
				if(progress.isShowing()) {
					needProgress = false;
					progress.dismiss();
				}
				Stop closest = result.getClosest();				
				if(!stops.isEmpty()) {
					StopAdapter stopAdapter = new StopAdapter(StopActivity.this,result.getStopQueryResult().getDepartureDate(),result.getStopQueryResult().getTripToService(), stops);
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
					String address = String.format("feedback_%s_%s_%s",getSchedulerContext().getDepartureStation().getId(),getSchedulerContext().getArrivalStation().getId(), getString(R.string.email_address));
					String appName = getString(R.string.app_name_full);
					String question = String.format("We are unable to find results for your search criteria. Please note that %s does not support connections.  To provide additional feedback, please email %s.",appName, address);
					stopTimes.setVisibility(View.GONE);
					errors.setVisibility(View.VISIBLE);
					errors.setText(question);
				}	
				trackEvent("stop_times", "query", stops.size() +" stops for " + departure.getName() + " to " + arrival.getName() + " in " + result.getStopQueryResult().getQueryDuration(), 0);
			}
			
		}.execute();
	}
	
	private TimerTask newUpdaterThread() {
		updaterThread = new TimerTask() {
			
			@Override
			public void run() {
				minutesAway.clear();
				
				for(int i = 0; i < stopTimes.getChildCount(); i++) {
					final Integer away;
					LinearLayout row = (LinearLayout)stopTimes.getChildAt(i);
					TextView minutesAway = (TextView) row.findViewById(R.id.away);
					Stop stop = (Stop)stopTimes.getItemAtPosition(row.getId());
					long awayTimeInMinutes = StopAdapter.awayTimeInMinutes(stop);
					Calendar tomorrow = Calendar.getInstance();
					int hourOfDay = tomorrow.get(Calendar.HOUR_OF_DAY);
					if(awayTimeInMinutes>=0 && (awayTimeInMinutes<=100 || ((hourOfDay<4 || hourOfDay>18) && awayTimeInMinutes<=200))) {
						//minutesAway.setVisibility(View.VISIBLE);
						//minutesAway.setText(String.format("departs in %s minutes",awayTimeInMinutes));
						away = (int)awayTimeInMinutes;
					} else {
						away = null;
						//minutesAway.setVisibility(View.GONE);
					}
					StopActivity.this.minutesAway.put(minutesAway, away);
				}
				if(minutesAway.isEmpty()) {
					return;
				}
				trackEvent("stops", "refresh", new Date().toString() , ++refreshCount);
				StopActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						for(Map.Entry<TextView, Integer> e : minutesAway.entrySet()) {
							e.getKey().setVisibility(e.getValue()==null ? View.GONE : View.VISIBLE);
							if(e.getValue()!=null) {
								e.getKey().setText(String.format("departs in %s minutes",e.getValue()));
							}
						}
						stopTimes.invalidate();
					}
				});				
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
		if(progress!=null) {
			if(progress.isShowing()) {
				progress.dismiss();
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
					c.set(Calendar.SECOND, 0);
					c.set(Calendar.MILLISECOND, 0);
					c.add(Calendar.MINUTE, 1);
					timer.schedule(newUpdaterThread(), c.getTime());
				}
			}catch (Exception e) {
				Log.e(getClass().getSimpleName(), "onResume",e);
			}
		}
		if(needProgress) {
			progress.show();
		}
	}
	
	@Override
	public void onBackPressed() {
		
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		startActivity(intent);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if(stops.size()>0) {
			MenuItem reverse = menu.add(Menu.NONE,1,Menu.FIRST, getString(R.string.reverse));
			reverse.setIcon(R.drawable.signpost);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==1) {
			trackEvent("menu-click", "MenuButton", item.getTitle().toString(), item.getItemId());
			getSchedulerContext().reverseTrip();
			Intent intent = new Intent(this, StopActivity.class);
			startActivity(intent);
		}		
		return super.onOptionsItemSelected(item);
	}
}
