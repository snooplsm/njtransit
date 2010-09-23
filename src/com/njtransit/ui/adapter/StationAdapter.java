package com.njtransit.ui.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.njtransit.R;
import com.njtransit.domain.Session;
import com.njtransit.domain.Station;
import com.njtransit.utils.Distance;
import com.njtransit.utils.Locations;

public class StationAdapter extends ArrayAdapter<Station> implements SectionIndexer, Filterable {
	
	private Session session;
	
	private int type;
	
	private HashMap<Integer, Character> posToCharacter;
	
	private HashMap<Character, Integer> characterToPos;
	
	private Character[] sections;
	
	public static final int ALPHA = 0, NEARBY = 1, FAVORITES = 2;
	
	private static final Comparator<Station> ALPHA_SORT = new Comparator<Station>() {
		@Override
		public int compare(Station a, Station b) {
			return a.getName().compareToIgnoreCase(b.getName());
		}
	};
	
	private static final NearbyComparator NEARBY_SORT = new NearbyComparator();
	
	private static class NearbyComparator implements Comparator<Station> {
		
		private Location home;
		
		public NearbyComparator() {
		}
		
		public NearbyComparator setHome(Location l) {
			this.home = l;
			return this;
		}

		@Override
		public int compare(Station a, Station b) {
			if(home == null) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
			Double dist = Distance.greatCircle(home.getLatitude(), home.getLongitude(), a.getLatitude(), a.getLongitude());
			return dist.compareTo(Distance.greatCircle(home.getLatitude(), home.getLongitude(), b.getLatitude(), b.getLongitude()));
		}
	}
	
	public StationAdapter(Context context, int textViewResourceId, int type,
			List<Station> items, Session session) {
		super(context, textViewResourceId, new ArrayList<Station>(items));		
		this.session = session;		
		setType(type);
	}
	
	public void setType(int type) {
		this.type = type;
		if(type == ALPHA) {
			sort(ALPHA_SORT);
		} else {		
			sort(NEARBY_SORT.setHome(session.getLastKnownLocation()));
		}
	}
	
	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		 View v = getOrInflateView(convertView);
         Station s = getItem(pos);
         if (s != null) {
             TextView name = (TextView) v.findViewById(R.id.station_name);
             if (name != null) {
            	 name.setText(s.getName()); 
             }
             TextView distance = (TextView) v.findViewById(R.id.station_distance);
             if(distance != null && session.getLastKnownLocation() != null){
            	 distance.setText(Locations.relativeDistanceFrom(session.getLastKnownLocation()).to(s).inWords());
             }
         }
         return v;
	}
	
	private View getOrInflateView(View v) {
		return v != null ? v : ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.station_row, null);    
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
		if(type == NEARBY) {
			return null;
		}
		calculateSections();
		return sections;
	}
	
	private void calculateSections() {
		if(sections!=null) return;
		HashSet<Character> c = new HashSet<Character>();	
		posToCharacter = new HashMap<Integer, Character>();
		characterToPos = new HashMap<Character, Integer>();
		for(int i = 0; i < getCount(); i++) {
			Station s = getItem(i);
			char ch = s.getName().charAt(0);
			if(c.add(ch)) {
				posToCharacter.put(i, ch);
				characterToPos.put(ch, i);
			}
		}
		sections = new Character[c.size()];
		c.toArray(sections);
		Arrays.sort(sections);
	}
}