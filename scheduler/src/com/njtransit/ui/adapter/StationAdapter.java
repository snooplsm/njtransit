package com.njtransit.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.njtransit.SchedulerApplication;
import com.njtransit.domain.Station;
import com.njtransit.rail.R;
import com.njtransit.utils.Locations;

import java.util.*;

public class StationAdapter extends ArrayAdapter<Station> implements
		SectionIndexer, Filterable {

	private SchedulerApplication app;

	private HashMap<Integer, String> posToCharacter;

	private HashMap<String, Integer> characterToPos;

	private String[] sections;

	public StationAdapter(Context context, int textViewResourceId,
			List<Station> items, SchedulerApplication app) {
		super(context, textViewResourceId, new ArrayList<Station>(items));
		this.app = app;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		View v = getOrInflateView(convertView);
		Station s = getItem(pos);
		if (s != null) {
			TextView name = (TextView) v.findViewById(R.id.station_name);
			TextView desc = (TextView) v.findViewById(R.id.station_desc);
			if (name != null) {
				name.setText(s.getName());
				if(s.getDescriptiveName()==null) {
					desc.setVisibility(View.GONE);
				} else {
					desc.setText(s.getDescriptiveName());
				}
			}
			TextView distance = (TextView) v
					.findViewById(R.id.station_distance);
			distance.setVisibility(View.GONE);
		}
		return v;
	}

	private View getOrInflateView(View v) {
		return v != null ? v : ((LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.station_row,
				null);
	}

	@Override
	public int getPositionForSection(int section) {
		String c = (String)sections[section];
		return characterToPos.get(c);
	}

	@Override
	public int getSectionForPosition(int pos) {
		Station s = getItem(pos);
		char c = s.getName().charAt(0);
		return characterToPos.get(c);
	}

	@Override
	public Object[] getSections() {
		calculateSections();
		return sections;
	}

	private void calculateSections() {
		if (sections != null)
			return;
		HashSet<String> c = new HashSet<String>();
		posToCharacter = new HashMap<Integer, String>();
		characterToPos = new HashMap<String, Integer>();
		for (int i = 0; i < getCount(); i++) {
			Station s = getItem(i);
			String ch = s.getName().substring(0,1);
			if (c.add(ch)) {
				posToCharacter.put(i, ch);
				characterToPos.put(ch, i);
			}
		}
		sections = new String[c.size()];
		c.toArray(sections);
		Arrays.sort(sections);
	}
}
