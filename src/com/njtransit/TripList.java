package com.njtransit;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.njtransit.domain.Trip;
import com.njtransit.ui.adapter.TripAdapter;

public class TripList extends ListActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_list);
        final NJTransitDBAdapter db = new NJTransitDBAdapter(this).open();
        final List<Trip> trips = db.getTrips(getIntent().getExtras().getInt("station"));
        
        ListView view = getListView();
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.trip_header, view, false);
        view.addHeaderView(header, null, false);
        
        TextView headsign = (TextView)findViewById(R.id.trip_headsign);
        headsign.setText(trips.get(0).getHeadsign());
        
        setListAdapter(new TripAdapter(this, R.layout.trip_row, trips));
        
        view.setTextFilterEnabled(true);
        view.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int pos, long id) {
            Toast.makeText(getApplicationContext(), trips.get(pos).getHeadsign(),
                Toast.LENGTH_SHORT).show();
          }
        });
    }
}
