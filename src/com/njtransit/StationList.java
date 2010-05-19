package com.njtransit;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;

public class StationList extends ListActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final List<Station> stations = Session.get().getStations();
        setListAdapter(new StationAdapter(this, R.layout.station_row, stations));
        
        ListView list = getListView();
        list.setTextFilterEnabled(true);

        list.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int pos, long id) {
            Toast.makeText(getApplicationContext(), stations.get(pos).getName(),
                Toast.LENGTH_SHORT).show();
          }
        });
    }
}