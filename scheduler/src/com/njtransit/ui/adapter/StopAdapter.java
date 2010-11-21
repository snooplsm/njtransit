package com.njtransit.ui.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.njtransit.R;
import com.njtransit.domain.Stop;

public class StopAdapter extends ArrayAdapter<Stop> {

	DateFormat f = new SimpleDateFormat("hh:mm aa");
	
	public StopAdapter(Context context, List<Stop> objects) {
		super(context, 1, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View str = null;
		str = getOrInflateRow(convertView);
		TextView time = (TextView) str.findViewById(R.id.time);
		TextView duration = (TextView) str.findViewById(R.id.duration);
		TextView minutesAway = (TextView) str.findViewById(R.id.away);
		Stop stop = getItem(position);
		time.setText(format(stop));
		duration.setText(duration(stop));
		int awayTimeInMinutes = awayTimeInMinutes(stop);
		int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if(awayTimeInMinutes>=0 && (awayTimeInMinutes<=100 || ((hourOfDay<4 || hourOfDay>18) && awayTimeInMinutes<=200))) {
			minutesAway.setVisibility(View.VISIBLE);
			minutesAway.setText(String.format("departs in %s minutes",awayTimeInMinutes));
		} else {
			minutesAway.setVisibility(View.GONE);
		}
		return str;
	}

	public int awayTimeInMinutes(Stop stop) {
		long diff = stop.getDepart().getTimeInMillis()
				- System.currentTimeMillis();
		int mins = (int) diff / 60000;
		return mins;
	}

	public String duration(Stop stop) {
		final long diff = stop.getArrive().getTimeInMillis()
				- stop.getDepart().getTimeInMillis();
		long mins = diff / 60000;
		Long departureInSeconds = System.currentTimeMillis()
				- (stop.getDepart().getTimeInMillis() / 1000) / 60;
		if (departureInSeconds < 0 || departureInSeconds > 60) {
			departureInSeconds = null;
		}

		return String.format("%s minutes", mins);
	}

	private View getOrInflateRow(View current) {
		return (View) (current == null ? ((LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.stop_time_row, null) : current);
	}

	private String format(Stop stop) {
		DateFormat f = new SimpleDateFormat("hh:mm aa");
		String depart = f.format(stop.getDepart().getTime());
		String arrive = f.format(stop.getArrive().getTime());
		return String.format("%s - %s", depart, arrive)
				.toLowerCase();
	}

}
