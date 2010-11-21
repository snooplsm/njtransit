package com.njtransit;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.njtransit.domain.Station;

public class ExampleActivity extends SchedulerActivity implements LocationListener  {
	
	private static String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;

	public static int DEPARTURE_REQUEST_CODE = 1, ARRIVAL_REQUEST_CODE = 2;
	
	private TextView departureText, arrivalText;
	private View getSchedule;
	private ImageView getScheduleImage;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==RESULT_OK) {
			Station station = getSchedulerContext().getStation(data.getIntExtra("stationId",-1));
			if(requestCode==DEPARTURE_REQUEST_CODE) {
				getSchedulerContext().setDepartureStation(station);
				departureText.setText(station.getName());
			} else {
				getSchedulerContext().setArrivalStation(station);
				arrivalText.setText(station.getName());
			}
		}	
		if(getSchedulerContext().getDepartureStation()!=null && getSchedulerContext().getArrivalStation()!=null) {
			getSchedule.setEnabled(true);
			getScheduleImage.setVisibility(View.VISIBLE);
		} else {
			getScheduleImage.setVisibility(View.INVISIBLE);
			getSchedule.setEnabled(false);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jumper);
		
		final SchedulerApplication app = getSchedulerContext();
		
		if(app.getAdapter() == null) {
			Toast.makeText(app, getString(R.string.disclaimer), Toast.LENGTH_SHORT).show();
			DatabaseAdapter a = new DatabaseAdapter(this).open();
			app.setAdapter(a);
			app.setStations(a.getStations());
			app.setDepartureStation(app.getStation(148));
			app.setArrivalStation(app.getStation(105));
		}
		app.setLastKnownLocation(getLocations().getLastKnownLocation(LOCATION_PROVIDER));
		if(app.getLastKnownLocation() == null) {
			getLocations().requestLocationUpdates(LOCATION_PROVIDER, 3600000, 0, this);
		}
		RelativeLayout btn = (RelativeLayout) findViewById(R.id.departure);
		departureText = (TextView)findViewById(R.id.departureText);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {	
				Intent intent = new Intent(getApplicationContext(), StationListActivity.class);
				startActivityForResult(intent, DEPARTURE_REQUEST_CODE);
			}
		});
		RelativeLayout arrival = (RelativeLayout) findViewById(R.id.arrival);
		arrivalText = (TextView)findViewById(R.id.arrivalText);
		arrival.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), StationListActivity.class);
				startActivityForResult(intent, ARRIVAL_REQUEST_CODE);
			}
		});
				
		getSchedule = (RelativeLayout)findViewById(R.id.get_schedule);
		getSchedule.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				app.getAdapter().getStopTimesAlternate(app.getDepartureStation(), app.getArrivalStation());
				Intent intent = new Intent(getApplicationContext(), StopActivity.class);
				startActivity(intent);
			}
			
		});
		getScheduleImage = (ImageView)findViewById(R.id.getScheduleChevron);
	}
	
	private LocationManager getLocations() {
		return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}
	
	@Override
	public void onLocationChanged(Location l) {
		getSchedulerContext().setLastKnownLocation(l);
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