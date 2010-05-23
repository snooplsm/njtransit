package com.njtransit;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;
import com.njtransit.ui.adapter.MainGridAdapter;

public class Main extends Activity implements LocationListener {
   
	private static final int PREFS = 1;
	private static final int QUIT = 2;
	private static final int REFRESH_LOC = 3;
	private static final DecimalFormat df = new DecimalFormat("#");
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

        final ProgressDialog dialog = progress("Loading. Please wait...");
        
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
		for(Station s : closest.keySet()) {
			stations[i++] = s;
		}
		i = 0;
		Arrays.sort(stations, new Comparator<Station>() {

			@Override
			public int compare(Station object1, Station object2) {
				Double d1 = closest.get(object1);
				Double d2 = closest.get(object2);
				return d1.compareTo(d2);
			}
		});
		
		for(Station s : stations) {		
			options[i] = s.getName() + "\n " + df.format(closest.get(s)) + " meters";
			i++;
		}
		options[closest.size()] = closest.isEmpty() ? "Go": "Or find your own";

		new AlertDialog.Builder(this).setTitle(closest.isEmpty() ? "Select Station" : "Select a Station closest to you").setItems(options, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int index) {
		    	if(closest.size() > index) {
		    		onStationSelected(stations[index]);
		    	} else {
		    		onListStations();
		    	}
		    }
		}).create().show();
	}

	/** @see LocationListener#onLocationChanged(Location) */
	@Override
	public void onLocationChanged(Location l) {

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

	/** @see Activity#onCreateOptionsMenu(Menu) */
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, PREFS, 0, "Prefs");
	    menu.add(0, QUIT, 0, "Quit");
	    menu.add(0, REFRESH_LOC, 0, "Refresh Location");
	    return true;
	}

	/** @see Activity#onOptionsItemSelected(MenuItem) */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case PREFS:
			onShowPrefs();
			return true;
	    case QUIT:
	        finish();
	        return true;
	    case REFRESH_LOC:
	    	refreshClosestStations();
	    	return true;
	    }
	    return false;
	}

	private void refreshClosestStations() {
		ProgressDialog pd = progress("Fetching current location");
		Map<Station, Double> closest = session.findClosestStations(null, 6);
		pd.dismiss();
		onShowClosestStations(closest);
	}

	private ProgressDialog progress(String msg) {
		return ProgressDialog.show(this, "", msg, true);
	}

	private void onListStations() {
		startActivity(new Intent(this, StationList.class));
	}

	private void onStationSelected(Station s) {
		Toast.makeText(getApplicationContext(), "Selected station " + s.getName(),
                Toast.LENGTH_SHORT).show();
		Intent next = new Intent(this, TripList.class);
		next.putExtra("station", s.getId());
		startActivity(next);
	}

	private void onShowPrefs() {
		startActivity(new Intent(this, Prefs.class));
	}
}