package com.njtransit.ui.adapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.njtransit.R;
import com.njtransit.StopListView;
import com.njtransit.StopTimeRow;
import com.njtransit.domain.Stop;

public class StopAdapter extends ArrayAdapter<Stop> {

	private StopListView parent;
	
	private HashSet<StopTimeRow> cache = new HashSet<StopTimeRow>();
	
	public HashSet<StopTimeRow> getCache() {
		return cache;
	}

	public StopAdapter(StopListView parent, Context context,
			List<Stop> objects) {
		super(context, 1, objects);
		this.parent = parent;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		StopTimeRow str = null;
		try {
			str = getOrInflateRow(convertView).setStop(
					getItem(position));
			cache.add(str);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		String away = str.getAway(this.parent);
		str.setAway(away);
		return str;
	}

	private StopTimeRow getOrInflateRow(View current) {
		return (StopTimeRow) (current == null ? ((LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				.inflate(R.layout.stop_time_row, null) : current);
	}
	
}
