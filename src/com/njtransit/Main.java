package com.njtransit;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    
	private NJTransitDBAdapter adapter;   
	
	private LocationManager locationManager;
	
	private Session session = Session.get();
	
	private void warn(CharSequence msg) {
		//Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		Log.e("err!", msg.toString());
	}
	
	/** Called when the activity is first created. */
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
				Long now = SystemClock.currentThreadTimeMillis();
				adapter = new NJTransitDBAdapter(Main.this).open();
				
				Long later = SystemClock.currentThreadTimeMillis();
				Log.d("database", later - now + " seconds");
				
				ArrayList<Station> stations = adapter.getAllStations();
				if(stations != null) {
					warn("could not retrieve stations");
				} else {
					session.setStations(stations);
				}
				
				ArrayList<Trip> trips = adapter.getTrips(session.findClosestStation(null));
				if(trips == null) {
					warn("could not retrieve trips");
				} else {
					for(Trip t : trips) {
						Log.i("trip", t.getHeadsign());
					}
					
					final CharSequence[] tripNames = new CharSequence[trips.size()];
					for(int i=0;i<trips.size();i++) {
						tripNames[i] = trips.get(i).getHeadsign();
					}
					
					AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
					builder.setTitle("Select a trip");
					builder.setItems(tripNames, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
					        warn(tripNames[item]);
					    }
					});
					AlertDialog alert = builder.create();
				}
				
				ArrayList<Station> closestStations = session.findClosestStations(null, 6);
				if(closestStations == null) {
					warn("could not retrieve closest stations");
				} else {
					for(Station s : closestStations) {
						Log.i("station", s.getName());
						ArrayList<Trip> tripz = adapter.getTrips(s);
						for(Trip t : tripz) {
							ArrayList<StopTime> stopTimes = adapter.getAllStopTimes(s, t);
							for(StopTime st : stopTimes) {
								
							}
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

	/** @see LocationListener#onProviderDisabled(String) */
	@Override
	public void onProviderDisabled(String provider) {
		
	}

	/** @see LocationListener#onProviderEnabled(String) */
	@Override
	public void onProviderEnabled(String provider) {
		
	}

	/** @see LocationListener#onStatusChanged(String, int, Bundle) */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}
}