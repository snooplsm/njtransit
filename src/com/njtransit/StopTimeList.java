package com.njtransit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

import com.njtransit.domain.StopTime;
import com.njtransit.ui.adapter.StopTimeAdapter;

public class StopTimeList extends ListActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stop_time_list);
        ListView view = getListView();
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.stop_time_header, view, false);
        view.addHeaderView(header, null, false);
        
        final NJTransitDBAdapter db = new NJTransitDBAdapter(this).open();
        final Integer stationA = getIntent().getExtras().getInt("sa");
        final Integer stationB = getIntent().getExtras().getInt("sb");
        final Integer trip = getIntent().getExtras().getInt("trip");
        
        TextView headsign = (TextView)findViewById(R.id.trip_headsign);
        if(headsign != null) {
        	headsign.setText(db.getTrip(trip).getHeadsign());
        }
        
        TextView aName = (TextView) findViewById(R.id.station_a);
        if(aName != null) {
        	aName.setText(db.getStation(stationA).getName());
        }
        
        TextView bName = (TextView) findViewById(R.id.station_b);
        if(bName != null) {
        	bName.setText(db.getStation(stationB).getName());
        }
        
        // TODO how do we get all stations for a given trip?
        final List<StopTime> times = db.getAllStopTimes(stationA, trip);
        
        setListAdapter(new StopTimeAdapter(this, R.layout.stop_time_list, times));
        
        view.setTextFilterEnabled(true);
        view.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int pos, long id) {
            Toast.makeText(getApplicationContext(), "Train arrives @ " + fmt(times.get(pos-1).getArrival()),
                Toast.LENGTH_SHORT).show();
          }
        });
    }
	private String fmt(Calendar c) {
		SimpleDateFormat f = new SimpleDateFormat("hh:mma");
		return f.format(c.getTime());
	}
}
