package com.njtransit;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.GridView;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;
import com.njtransit.domain.StopTime;
import com.njtransit.domain.Trip;

public class Main extends Activity implements LocationListener {
    /** Called when the activity is first created. */
	
	private NJTransitDBAdapter adapter;   
	
	private LocationManager locationManager;
	
	private Session session = Session.get();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        GridView grid = (GridView) findViewById(R.id.grid);
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3600000, 0,
                this);
        
        Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        session.setLastKnownLocation(lastKnown);
        
        new AsyncTask<Void,Void,Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				// TODO Auto-generated method stub
				Long now = SystemClock.currentThreadTimeMillis();
				adapter = new NJTransitDBAdapter(Main.this).open();
				Long later = SystemClock.currentThreadTimeMillis();
				Log.d("database", later - now + " seconds");
				ArrayList<Station> stations = adapter.getAllStations();
				session.setStations(stations);
				ArrayList<Trip> trips = adapter.getTrips(session.findClosestStation(null));
				for(Trip t : trips) {
					Log.i("trip", t.getHeadsign());
				}
				ArrayList<Station> stations2 = session.findClosestStations(null, 6);
				for(Station s : stations2) {
					Log.i("station", s.getName());
					ArrayList<Trip> tripz = adapter.getTrips(s);
					for(Trip t : tripz) {
						ArrayList<StopTime> stopTimes = adapter.getAllStopTimes(s, t);
						for(StopTime st : stopTimes) {
							
						}
					}
				}
				return 1;
			}
        	
        }.execute();
        
        
    }

	@Override
	public void onLocationChanged(Location location) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		double wtf = latitude + longitude;
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}