package com.njtransit;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.njtransit.domain.Stop;

public class StopAdapter extends ArrayAdapter<Stop> {

	public StopAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}
}
