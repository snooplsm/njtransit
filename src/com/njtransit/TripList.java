package com.njtransit;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.njtransit.domain.Station;
import com.njtransit.domain.Trip;
import com.njtransit.ui.adapter.TripAdapter;

public class TripList extends ListActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        Integer stationId = extras.getInt("station");
        
        
        final NJTransitDBAdapter db = new NJTransitDBAdapter(this).open();
        final List<Trip> trips = db.getTrips(db.getStation(stationId));
        setListAdapter(new TripAdapter(this, R.layout.trip_row, trips));
        
        ListView list = getListView();
        list.setTextFilterEnabled(true);

        list.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int pos, long id) {
            Toast.makeText(getApplicationContext(), trips.get(pos).getHeadsign(),
                Toast.LENGTH_SHORT).show();
          }
        });
    }
}
