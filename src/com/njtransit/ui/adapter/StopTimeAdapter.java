package com.njtransit.ui.adapter;

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
import com.njtransit.domain.StopTime;

/** Renders a list of {@link StopTime}s */
public class StopTimeAdapter extends ArrayAdapter<StopTime> {
private List<StopTime> items;
	
	public StopTimeAdapter(Context context, int textViewResourceId,
			List<StopTime> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		 View v = convertView;
         if (v == null) {
             LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = inflater.inflate(R.layout.stop_time_row, null);
         }
         StopTime s = items.get(pos);
         if (s != null) {
             TextView arr = (TextView) v.findViewById(R.id.arrival);
             if (arr != null) {
            	 arr.setText("arriving @ " +fmt(s.getArrival())); 
             }
             
             TextView dep = (TextView) v.findViewById(R.id.departure);
             if(dep != null){
            	 dep.setText("departing @ " + fmt(s.getDeparture()));
             }
         }
         return v;
	}
	
	private String fmt(Calendar c) {
		SimpleDateFormat f = new SimpleDateFormat("hh:mma");
		return f.format(c.getTime());
	}
}