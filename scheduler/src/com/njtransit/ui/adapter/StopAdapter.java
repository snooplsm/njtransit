package com.njtransit.ui.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.njtransit.domain.IService;
import com.njtransit.domain.Stop;
import com.scheduler.njtransit.R;

public class StopAdapter extends ArrayAdapter<Stop> {

	static DateFormat f = new SimpleDateFormat("hh:mm aa");
	
	static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
	
	private Map<Integer,IService> services;
	private Calendar departDate;
	private Calendar today;
	
	public StopAdapter(Context context, Calendar departDate, Map<Integer,IService> services, List<Stop> objects) {
		super(context, 1, objects);
		this.services = services;
		this.departDate = departDate;
		today = Calendar.getInstance();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View str = null;
		str = getOrInflateRow(convertView);
		str.setId(position);
		TextView time = (TextView) str.findViewById(R.id.time);
		TextView duration = (TextView) str.findViewById(R.id.duration);
		TextView minutesAway = (TextView) str.findViewById(R.id.away);
		TextView timeDesc = (TextView) str.findViewById(R.id.time_descriptor);
		Stop stop = getItem(position);
		time.setText(format(stop));
		duration.setText(duration(stop));
		int awayTimeInMinutes = awayTimeInMinutes(stop);
		Calendar _departDate = Calendar.getInstance();
		_departDate.setTimeInMillis(departDate.getTimeInMillis());
		int hourOfDay = _departDate.get(Calendar.HOUR_OF_DAY);
		int scheduleDepartDay = _departDate.get(Calendar.DAY_OF_YEAR);
		int scheduleDepartYear = _departDate.get(Calendar.YEAR);
		Calendar nextDay = Calendar.getInstance();
		nextDay.setTimeInMillis(_departDate.getTimeInMillis());
		nextDay.add(Calendar.DAY_OF_YEAR, 1);
		Calendar depart = stop.getDepart();
		int departYear = depart.get(Calendar.YEAR);
		int departDay = depart.get(Calendar.DAY_OF_YEAR);
		int nextDayYear = nextDay.get(Calendar.YEAR);
		int nextDayDay = nextDay.get(Calendar.DAY_OF_YEAR);
		int todayDayDay = today.get(Calendar.DAY_OF_YEAR);
		int todayDayYear = today.get(Calendar.YEAR);
		
		if(scheduleDepartDay!=todayDayDay || scheduleDepartYear!=todayDayYear) {
			timeDesc.setText(dateFormat.format(depart.getTime()));
			timeDesc.setVisibility(View.VISIBLE);
			minutesAway.setVisibility(View.GONE);
		} else {
			if(departYear==nextDayYear && departDay == nextDayDay) {
				timeDesc.setText("next day");
				timeDesc.setVisibility(View.VISIBLE);
				minutesAway.setVisibility(View.GONE);
			} else {
				timeDesc.setVisibility(View.GONE);
			}
			if(awayTimeInMinutes>=0 && (awayTimeInMinutes<=100 || ((hourOfDay<4 || hourOfDay>18) && awayTimeInMinutes<=200))) {
				minutesAway.setVisibility(View.VISIBLE);
				minutesAway.setText(String.format("departs in %s minutes",awayTimeInMinutes));
			} else {
				minutesAway.setVisibility(View.GONE);
			}
		}
		return str;
	}

	public static int awayTimeInMinutes(Stop stop) {
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
