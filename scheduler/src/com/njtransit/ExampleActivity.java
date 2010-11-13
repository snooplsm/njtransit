package com.njtransit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;

public class ExampleActivity extends Activity implements LocationListener  {

	private Session session = Session.get();	
	
	private static String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;

	public static int DEPARTURE_REQUEST_CODE = 1;
	public static int ARRIVAL_REQUEST_CODE = 2;
	
	private Button departure;
	private Button arrival;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==RESULT_OK) {
			Station station = session.getStation(data.getIntExtra("stationId",-1));
			if(requestCode==DEPARTURE_REQUEST_CODE) {
				session.setDepartureStation(station);
				departure.setText(station.getName());
			} else {
				session.setArrivalStation(station);
				arrival.setText(station.getName());
			}
		}		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jumper);
		DeviceInformation i = DeviceInformation.getDeviceInformatino(this);
		String uuid = i.getUuid();
		if(session.getAdapter() == null) {
			Toast.makeText(getApplicationContext(), getString(R.string.disclaimer), Toast.LENGTH_LONG).show();
			DatabaseAdapter a = new DatabaseAdapter(this).open();
			session.setAdapter(a);
			session.setStations(a.getStations());
			session.setDepartureStation(session.getStation(148));
			session.setArrivalStation(session.getStation(105));
			//getApplicationContext().startService(new Intent(getApplicationContext(),UpdaterService.class));
		}
		session.setLastKnownLocation(getLocations().getLastKnownLocation(LOCATION_PROVIDER));
		if(session.getLastKnownLocation() == null) {
			getLocations().requestLocationUpdates(LOCATION_PROVIDER, 3600000, 0, this);
		}
		final Button btn = (Button) findViewById(R.id.departure);
		departure = btn;
		
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {	
				Intent intent = new Intent(getApplicationContext(), StationListActivity.class);
				startActivityForResult(intent, DEPARTURE_REQUEST_CODE);
			}
		});
		arrival = (Button) findViewById(R.id.arrival);
		arrival.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {	
				Intent intent = new Intent(getApplicationContext(), StationListActivity.class);
				startActivityForResult(intent, ARRIVAL_REQUEST_CODE);
			}
		});
		
		Button getSchedule = (Button)findViewById(R.id.get_schedule);
		getSchedule.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
//				new Thread() {
//					public void run() {
//						final StopsQueryResult sqr = session.getAdapter().getStopTimesAlternate(session.getDepartureStation(), session.getArrivalStation());
//					}
//				}.start();
				Intent intent = new Intent(getApplicationContext(), StopActivity.class);
				startActivity(intent);
			}
			
		});
	}
	
	/**
	 * Expose for testing purposes.
	 * 
	 * @return
	 */
	public Session getSession() {
		return session;
	}
	
	private LocationManager getLocations() {
		return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}
	
	@Override
	public void onLocationChanged(Location l) {
		session.setLastKnownLocation(l);
		//getTabHost().setCurrentTab(getTabHost().getCurrentTab());
		getLocations().removeUpdates(this);
	}
	
	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}