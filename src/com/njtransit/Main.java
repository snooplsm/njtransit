package com.njtransit;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.njtransit.domain.Station;

public class Main extends Activity {
    /** Called when the activity is first created. */
	
	private NJTransitDBAdapter adapter;   
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        new AsyncTask<Void,Void,Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				// TODO Auto-generated method stub
				Long now = SystemClock.currentThreadTimeMillis();
				adapter = new NJTransitDBAdapter(Main.this).open();
				Long later = SystemClock.currentThreadTimeMillis();
				Log.d("database", later - now + " seconds");
				ArrayList<Station> stations = adapter.getAllStations();
				for(Station s : stations) {
					Log.i("station", s.getName());
				}
				return 1;
			}
        	
        }.execute();
        
        
    }
}