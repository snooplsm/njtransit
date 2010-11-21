package com.njtransit;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class Scheduler extends Application implements LocationListener {

	private Session session;

	private DeviceInformation deviceInformation;

	private static String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;

	@Override
	public void onCreate() {
		super.onCreate();

		deviceInformation = DeviceInformation.getDeviceInformatino(this);

		DatabaseAdapter a = new DatabaseAdapter(getApplicationContext()).open();
		Toast.makeText(getApplicationContext(), getString(R.string.disclaimer),
				Toast.LENGTH_SHORT).show();
		session = new Session(a);
		session.setStations(a.getStations());
		//session.setDepartureStation(session.getStation(148));
		//session.setArrivalStation(session.getStation(105));

		session.setLastKnownLocation(getLocations().getLastKnownLocation(
				LOCATION_PROVIDER));
		if (session.getLastKnownLocation() == null) {
			getLocations().requestLocationUpdates(LOCATION_PROVIDER, 3600000,
					0, this);
		}
	}

	public Session getSession() {
		return session;
	}

	private LocationManager getLocations() {
		return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public void onLocationChanged(Location l) {
		session.setLastKnownLocation(l);
		// getTabHost().setCurrentTab(getTabHost().getCurrentTab());
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
