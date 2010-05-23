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

import com.njtransit.domain.Session;
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
        final Integer stationId = getIntent().getExtras().getInt("station");
        final List<Trip> trips = db.getTrips(stationId);
        
        // this will only be set within the context of the second trip
        final Integer tripId = getIntent().getExtras().getInt("flip");
        final Integer tripPos = getIntent().getExtras().getInt("tripPos");
        
        final Button flip = (Button) findViewById(R.id.reverse_direction);
    	
        if(tripId.intValue() == 0) {
        	if(trips.size() == 2) {
	        	flip.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent next = new Intent(TripList.this, TripList.class);
						next.putExtra("station", stationId);
						Trip trip = null;
						Integer newTripPos = null;
						if(tripPos.intValue()==0) {
							newTripPos = 1;
							
						} else {
							newTripPos = 0;
						}
						trip = trips.get(newTripPos);
						next.putExtra("flip", trip.getId());
						next.putExtra("tripPos", newTripPos);
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
        
        Trip trip = trips.get(tripPos);
        
        headsign.setText(trip.getHeadsign());
        
        Station station = Session.get().getStation(stationId);
        final List<Station> stations = db.stationsWithin(station, trips.get(tripId));
        
        setListAdapter(new StationAdapter(this, R.layout.station_list, stations));
        
        view.setTextFilterEnabled(true);
        view.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int pos, long id) {
            Toast.makeText(getApplicationContext(), "Selected destination " +stations.get(pos-1).getName(),
                Toast.LENGTH_SHORT).show();
            Intent next = new Intent(TripList.this, StopTimeList.class);
            next.putExtra("sa", stationId);
            next.putExtra("sb", stations.get(pos-1).getId());
            next.putExtra("trip", tripId.intValue() == 0 ? trips.get(0).getId() : tripId);
            startActivity(next);
          }
        });
    }
}
