package com.njtransit;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.njtransit.domain.Station;
import com.njtransit.domain.Trip;
import com.njtransit.ui.adapter.StationAdapter;

public class TripList extends ListActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_list);
        ListView view = getListView();
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.trip_header, view, false);
        view.addHeaderView(header, null, false);
        
        
        final NJTransitDBAdapter db = new NJTransitDBAdapter(this).open();
        final Integer station = getIntent().getExtras().getInt("station");
        final List<Trip> trips = db.getTrips(station);
        
        // this will only be set within the context of the second trip
        final Integer trip = getIntent().getExtras().getInt("flip");
        
        Button flip = (Button) findViewById(R.id.reverse_direction);
    	
        if(trip.intValue() == 0) {
        	if(trips.size() > 1) {
	        	flip.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent next = new Intent(TripList.this, TripList.class);
						next.putExtra("station", station);
						next.putExtra("flip", trips.get(1).getId());
						startActivity(next);
					}
	        	});
        	} else {
        		flip.setVisibility(View.INVISIBLE);
        	}
        } else {
        	flip.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					TripList.this.finish();
		       }
        	});
        }
        TextView headsign = (TextView)findViewById(R.id.trip_headsign);
        headsign.setText(trip == 0 ? trips.get(0).getHeadsign() : db.getTrip(trip).getHeadsign());
        
        // TODO how do we get all stations for a given trip?
        final List<Station> stations = db.getAllStations();
        
        setListAdapter(new StationAdapter(this, R.layout.station_list, stations));
        
        view.setTextFilterEnabled(true);
        view.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int pos, long id) {
            Toast.makeText(getApplicationContext(), stations.get(pos-1).getName(),
                Toast.LENGTH_SHORT).show();
            Intent next = new Intent(TripList.this, StopTimeList.class);
            next.putExtra("sa", station);
            next.putExtra("sb", stations.get(pos-1).getId());
            next.putExtra("trip", trip == 0 ? trips.get(0).getId() : trip);
            startActivity(next);
          }
        });
    }
}
