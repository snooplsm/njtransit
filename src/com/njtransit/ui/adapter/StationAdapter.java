package com.njtransit.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.njtransit.R;
import com.njtransit.domain.Station;

public class StationAdapter extends ArrayAdapter<Station> {

	private List<Station> items;
	
	public StationAdapter(Context context, int textViewResourceId,
			List<Station> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		 View v = convertView;
         if (v == null) {
             LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = inflater.inflate(R.layout.station_row, null);
         }
         Station s = items.get(pos);
         if (s != null) {
             TextView name = (TextView) v.findViewById(R.id.station_name);
             TextView distance = (TextView) v.findViewById(R.id.station_distance);
             if (name != null) {
            	 name.setText(s.getName()); 
             }
             if(distance != null){
            	 distance.setText("about {{x}} away");
             }
         }
         return v;
	}
}