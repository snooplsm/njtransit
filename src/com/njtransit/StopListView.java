package com.njtransit;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.Context;
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
	
	public StopListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setScrollbarFadingEnabled(true);
		setSmoothScrollbarEnabled(true);
	
		ProgressDialog progress = ProgressDialog.show(getContext(),    
	              "Please wait...", "Loading stop times ...", true);
		
		final Station arrive = session.getArrivalStation();
		final Station departure = session.getDepartureStation();
		
		StopsQueryResult sqr =  Bench.time("get stop time", new Bench.Fn<StopsQueryResult>() {
			public StopsQueryResult apply() {
				return session.getAdapter().getStopTimes(session.getServices(), departure, arrive);
			}
		});
		
		ArrayList<Stop> today = new ArrayList<Stop>();
		ArrayList<Stop> tomorrow = new ArrayList<Stop>();
		Stop closest = null;
		Long closestDiff = Long.MAX_VALUE;
		final Long now = System.currentTimeMillis();
		for(Stop stop : sqr.getStops()) {
			Service service = sqr.getTripToService().get(stop.getTripId());

			if(service.isToday()) {
				Long diff = now - stop.getDepart().getTimeInMillis();
				if(diff > 0 && diff < now && diff < closestDiff) {
					closest = stop;
					closestDiff = diff;
				}
				today.add(stop);
			}
			if(service.isTomorrow()) {
				tomorrow.add(stop);
			}
		}		
		
		today.addAll(tomorrow);
		
		ArrayAdapter<Stop> adapter;
		setAdapter(adapter = new ArrayAdapter<Stop>(context, 1, today) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				return getOrInflateRow(convertView).setStop(getItem(position)).setAway(now);
			}
			
			private StopTimeRow getOrInflateRow(View current) {
				return (StopTimeRow) (current == null ?
					((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.stop_time_row, null)
					: current);
			}
		});
		
		if(closest != null) {
			setSelectionFromTop(adapter.getPosition(closest), 10);
		}
		
		progress.dismiss();
		
		timer = new Timer(false);
		timer.scheduleAtFixedRate(newUpdaterThread(), 7000, 7000);
	}
	
	@Override
	public void foreach(Fn<StopTimeRow> f) {
		int cnt = getAdapter().getCount();
		StopTimeRow r = null;
		for(int i = 0; i < cnt; i++) {
			r = (StopTimeRow) getAdapter().getView(i, r, null);
			f.apply(r);
		}
	}
	
	private TimerTask newUpdaterThread() {
		updaterThread = new TimerTask() {

			@Override
			public void run() {
				Fn<StopTimeRow> updateAway = new Fn<StopTimeRow>() {
					public void apply(StopTimeRow r) {
						r.setAway(System.currentTimeMillis());
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