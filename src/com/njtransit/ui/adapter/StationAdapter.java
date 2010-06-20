package com.njtransit.ui.adapter;

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

public class StationAdapter extends ArrayAdapter<Station> implements SectionIndexer, Filterable {
	
	private Session session;
	
	private int type;
	
	public static final int ALPHA=1,NEARBY=2;
	
	private static Comparator<Station> ALPHA_SORT = new Comparator<Station>() {

		@Override
		public int compare(Station object1, Station object2) {
			return object1.getName().compareToIgnoreCase(object2.getName());
		}
		
	};
	
	private static NearbyComparator NEARBY_SORT = new NearbyComparator();
	
	private static class NearbyComparator implements Comparator<Station> {
		
		private Location home;
		
		public NearbyComparator() {
		}
		
		public void setHome(Location location) {
			this.home = location;
		}

		@Override
		public int compare(Station object1, Station object2) {
			if(home==null) {
				return object1.getName().compareToIgnoreCase(object2.getName());
			}
			Double dist = Distance.greatCircle(home.getLatitude(), home.getLongitude(), object1.getLatitude(), object1.getLongitude());
			return dist.compareTo(Distance.greatCircle(home.getLatitude(), home.getLongitude(), object2.getLatitude(), object2.getLongitude()));
		}
		
	}
	
	public StationAdapter(Context context, int textViewResourceId, int type,
			List<Station> items, Session session) {
		super(context, textViewResourceId, items);		
		this.session = session;		
		setType(type);
	}
	
	public void setType(int type) {
		this.type = type;
		if(type==ALPHA) {
			sort(ALPHA_SORT);
		} else {
			NEARBY_SORT.setHome(session.getLastKnownLocation());
			sort(NEARBY_SORT);
		}
		
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		 View v = convertView;
         if (v == null) {
             LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = inflater.inflate(R.layout.station_row, null);
         }
         Station s = getItem(pos);
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

	@Override
	public int getPositionForSection(int arg) {
		char c = sections[arg];
		return characterToPos.get(c);
	}

	@Override
	public int getSectionForPosition(int arg) {
		Station s = getItem(arg);
		
		char c = s.getName().charAt(0);
		
		return characterToPos.get(c);
	}

	@Override
	public Object[] getSections() {
		if(type==NEARBY) {
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

	private HashMap<Integer, Character> posToCharacter;
	private HashMap<Character, Integer> characterToPos;
	
	private Character[] sections;
}