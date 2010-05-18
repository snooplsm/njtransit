package com.njtransit;

import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.Toast;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;
import com.njtransit.ui.adapter.MainGridAdapter;

public class Main extends Activity implements LocationListener {
   
	private LocationManager locationManager;
	
	private Session session;
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        session = Session.get();
        
        GridView grid = (GridView) findViewById(R.id.grid);
        grid.setAdapter(new MainGridAdapter(this));
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3600000, 0,
                this);
        session.setLastKnownLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));

        final ProgressDialog dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
        
        SessionInitializer.exec(new NJTransitDBAdapter(this), session, new InitializationListener() {
			@Override
			public void initialized(Map<Station, Double> closestStations) {
				dialog.dismiss();
				onShowClosestStations(closestStations);
			}
        });
    }
	
	/** show list of closest stations */
	public void onShowClosestStations(final Map<Station, Double> closest) {
		final CharSequence[] options = new CharSequence[closest.size() + 1];
		final Station[] stations = new Station[closest.size()];
		           
		int i = 0;
		for(Map.Entry<Station, Double> s : closest.entrySet()) {
			stations[i] = s.getKey();
			options[i] = s.getKey().getName() + " about " + s.getValue() + " meters away";
			i++;
		}
		options[closest.size()] = closest.isEmpty() ? "Go": "Or find your own";
		
		new AlertDialog.Builder(this).setTitle(closest.isEmpty() ? "Select Station" : "Select a Station closest to you").setItems(options, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int index) {
		    	if(closest.size() < index) {
		    		onStationSelected(stations[index]);
		    	} else {
		    		info("Show full list of stations");
		    	}
		    }
		}).create().show();
	}
	
	public void onStationSelected(Station s) {
		info("selected " + s.getName());
	}

	/** @see LocationListener#onLocationChanged(Location) */
	@Override
	public void onLocationChanged(Location l) {
		info(String.format("location changed [%s,%s]",l.getLatitude(), l.getLongitude()));
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

	private void info(CharSequence msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}
}