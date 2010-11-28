package com.njtransit.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.njtransit.domain.Trip;
import com.scheduler.njtransit.R;

public class TripAdapter extends ArrayAdapter<Trip> {
	private List<Trip> items;
	
	public TripAdapter(Context context, int textViewResourceId,
			List<Trip> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		 View v = convertView;
         if (v == null) {
             LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = inflater.inflate(R.layout.trip_row, null);
         }
         Trip s = items.get(pos);
         if (s != null) {
             TextView headsign = (TextView) v.findViewById(R.id.trip_headsign);
             TextView direction = (TextView) v.findViewById(R.id.trip_direction);
             if (headsign != null) {
            	 headsign.setText(s.getHeadsign()); 
             }
             if(direction != null){
            	 direction.setText(s.getDirection().equals(0) ? "South" : "North");
             }
         }
         return v;
	}
}
