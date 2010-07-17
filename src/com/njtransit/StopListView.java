package com.njtransit;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.njtransit.domain.Service;
import com.njtransit.domain.Session;
import com.njtransit.domain.Station;
import com.njtransit.domain.Stop;
import com.njtransit.model.StopsQueryResult;
import com.njtransit.utils.Bench;

/** List of StopTimeRows */
public class StopListView extends ListView implements Traversable<StopTimeRow> {
	
	private Session session = Session.get();
	
	private TimerTask updaterThread = null;
	
	private boolean needsReschedule = false;
	
	private Timer timer;
	
	private List<Stop> stops = new ArrayList<Stop>();
	
	public StopListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setScrollbarFadingEnabled(true);
		setSmoothScrollbarEnabled(true);
		
		setAdapter(new ArrayAdapter<Stop>(getContext(), 1, stops) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {				
				return getOrInflateRow(convertView).setStop(getItem(position)).setAway(StopListView.this,System.currentTimeMillis());
			}
			
			private StopTimeRow getOrInflateRow(View current) {
				return (StopTimeRow) (current == null ?
					((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.stop_time_row, null)
					: current);
			}
		});
		
		new AsyncTask<Void, Void, Stop>() {

			ProgressDialog progress = null;
			
			@SuppressWarnings("unchecked")
			@Override
			protected Stop doInBackground(Void... params) {
				final Station arrive = session.getArrivalStation();
				final Station departure = session.getDepartureStation();
				
				final StopsQueryResult sqr =  Bench.time("get stop times", new Bench.Fn<StopsQueryResult>() {
					public StopsQueryResult apply() {
						return session.getAdapter().getStopTimes(session.getServices(), departure, arrive);
					}
				});
				
				final ArrayList<Stop> today = new ArrayList<Stop>();
				final ArrayList<Stop> tomorrow = new ArrayList<Stop>();
				final Long now = System.currentTimeMillis();
				final Stop closest = Bench.time("find closest", new Bench.Fn<Stop>() {
					@Override
					public Stop apply() {
						Stop closest = null;
						Long closestDiff = Long.MAX_VALUE;
						for(Stop stop : sqr.getStops()) {
							Service service = sqr.getTripToService().get(stop.getTripId());
							if(service.isToday()) {
								Long diff = now - stop.getDepart().getTimeInMillis();
								if(diff > 0 && diff < now && diff < closestDiff) {
									closest = stop;
									closestDiff = diff;
								}
								today.add(stop);
							} else {
								tomorrow.add(stop);
							}
						}		
						today.addAll(tomorrow);
						return closest;
					}
				});
				
				stops.addAll(today);
				
				return closest;
			}
			
			@Override
			protected void onPreExecute() {
				progress = ProgressDialog.show(getContext(),    
			              "Please wait...", "Loading stop times ...", true);
			}
			
			@SuppressWarnings("unchecked")
			@Override
			protected void onPostExecute(Stop closest) {
				progress.dismiss();
				if(closest != null) {
					setSelectionFromTop(((ArrayAdapter<Stop>)getAdapter()).getPosition(closest), 10);
				}
				timer = new Timer(false);
				timer.scheduleAtFixedRate(newUpdaterThread(), 1000, 1000);
		    }
			
		}.execute();
	}
	
	@Override
	public void foreach(Fn<StopTimeRow> f) {
		int cnt = getAdapter().getCount();
		StopTimeRow r = null;
		for(int i = 0; i < cnt; i++) {
			r = (StopTimeRow) getAdapter().getView(i, r, this);
			f.apply(r);
		}
	}
	
	private TimerTask newUpdaterThread() {
		updaterThread = new TimerTask() {

			@Override
			public void run() {
				final Long now = System.currentTimeMillis();
				Fn<StopTimeRow> updateAway = new Fn<StopTimeRow>() {					
					public void apply(StopTimeRow r) {
						r.setAway(StopListView.this,now);
					}
				};
				foreach(updateAway);
			}			
		};
		return updaterThread;
	}
	
	public void onResume() {
		if(timer != null && needsReschedule) {
			timer = new Timer(false);			
			timer.scheduleAtFixedRate(newUpdaterThread(), 100, 7000);
		}
	}
	
	public void onPause() {
		try {
			if(timer != null) {
				needsReschedule = true;
				timer.cancel();
			}
		} catch (Exception e) {
			Log.w("error", "onResume");
		}
	}
}