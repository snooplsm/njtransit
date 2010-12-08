package com.njtransit.ui.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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

public class StationAdapter extends ArrayAdapter<Station> implements
		SectionIndexer, Filterable {

	private SchedulerApplication app;

	private HashMap<Integer, Character> posToCharacter;

	private HashMap<Character, Integer> characterToPos;

	private Character[] sections;

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
			if (distance != null && app.getLastKnownLocation() != null) {
				distance.setText(Locations
						.relativeDistanceFrom(app.getLastKnownLocation(),
								app.getPreferences().getUnits()).to(s)
						.inWords());
			} else {
				distance.setVisibility(View.GONE);
			}
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
		char c = sections[section];
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
		HashSet<Character> c = new HashSet<Character>();
		posToCharacter = new HashMap<Integer, Character>();
		characterToPos = new HashMap<Character, Integer>();
		for (int i = 0; i < getCount(); i++) {
			Station s = getItem(i);
			char ch = s.getName().charAt(0);
			if (c.add(ch)) {
				posToCharacter.put(i, ch);
				characterToPos.put(ch, i);
			}
		}
		sections = new Character[c.size()];
		c.toArray(sections);
		Arrays.sort(sections);
	}
}
