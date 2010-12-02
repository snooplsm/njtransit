package com.njtransit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.admob.android.ads.AdManager;
import com.njtransit.domain.IService;
import com.njtransit.domain.Preferences;
import com.njtransit.domain.Station;
import com.njtransit.utils.Distance;
import com.njtransit.R;

/** Shared state management */
public class SchedulerApplication extends Application implements
		LocationListener {

	private Location lastKnownLocation;
	private Station arrivalStation, departureStation;
	private Calendar departureDate;
	private int stationOrderType = 1;

	private Preferences preferences;

	private DeviceInformation deviceInformation;
	private static String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
	private List<Station> stations;
	private Map<Integer, IService> services;

	@SuppressWarnings("unused")
	private LocationManager locationManager;

	private DatabaseAdapter adapter;

	/**
	 * 
	 * @param location
	 *            can be null, if so defaults to lastKnownLocation
	 * @return
	 */
	public Map<Station, Double> findClosestStation(Location location) {
		Map<Station, Double> s = findClosestStations(location, 1);
		if (s == null || s.isEmpty()) {
			return Collections.<Station, Double> emptyMap();
		}
		return s;
	}

	/**
	 * This is a weak algorithm, if performance is a concern we should address
	 * it.
	 * 
	 * @param location
	 * @param max
	 * @return Key-Value collection of station to relative metered distances
	 */
	public Map<Station, Double> findClosestStations(Location location, int max) {
		if (location == null)
			location = lastKnownLocation;
		if (location == null || stations == null || stations.isEmpty())
			return new HashMap<Station, Double>(2) {
				private static final long serialVersionUID = 1L;
				{
					put(getStations().get(0), 100.0);
					put(getStations().get(1), 200.0);
				}
			};
		TreeMap<Double, Station> closest = new TreeMap<Double, Station>();
		for (Station s : stations) {
			double dist = Distance.greatCircle(location.getLatitude(), location
					.getLongitude(), s.getLatitude(), s.getLongitude());
			if (closest.size() < max) {
				closest.put(dist, s);
			} else {
				for (Double oldDist : closest.keySet()) {
					if (dist < oldDist) {
						closest.remove(oldDist);
						closest.put(dist, s);
						break;
					}
				}
			}
		}
		Map<Station, Double> inverted = new HashMap<Station, Double>(closest
				.size());
		for (Map.Entry<Double, Station> e : closest.entrySet()) {
			inverted.put(e.getValue(), e.getKey());
		}
		return inverted;
	}

	public DatabaseAdapter getAdapter() {
		return adapter;
	}

	public Station getArrivalStation() {
		return arrivalStation;
	}

	public Calendar getDepartureDate() {
		return departureDate;
	}

	public Station getDepartureStation() {
		return departureStation;
	}

	public DeviceInformation getDeviceInformation() {
		return deviceInformation;
	}

	public Location getLastKnownLocation() {
		return lastKnownLocation;
	}

	private LocationManager getLocations() {
		return (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	public Preferences getPreferences() {
		if (preferences == null) {
			preferences = new Preferences();
		}
		return preferences;
	}

	public Map<Integer, IService> getServices() {
		return services;
	}

	public Station getStation(Integer id) {
		for (Station s : stations) {
			if (s.getId().equals(id)) {
				return s;
			}
		}
		return null;
	}

	public int getStationOrderType() {
		return stationOrderType;
	}

	public List<Station> getStations() {
		return stations;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		deviceInformation = DeviceInformation.getDeviceInformation(this);                    
		adapter = new DatabaseAdapter(getApplicationContext()).open();

		Toast.makeText(getApplicationContext(), getString(R.string.disclaimer),
				Toast.LENGTH_SHORT).show();
		this.stations = adapter.getStations();

		setLastKnownLocation(getLocations().getLastKnownLocation(
				LOCATION_PROVIDER));
		if (getLastKnownLocation() == null) {
			getLocations().requestLocationUpdates(LOCATION_PROVIDER, 3600000,
					0, this);
		}
		if (Root.getScheduleEndDate(getApplicationContext()) < 0) {
			long max = adapter.getMaxCalendarDate();
			Root.saveScheduleEndDate(getApplicationContext(), max);
		}
		if (Root.getScheduleStartDate(getApplicationContext()) < 0) {
			long min = adapter.getMinCalendarDate();
			Root.saveScheduleStartDate(getApplicationContext(), min);
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		if (cal.getTimeInMillis() > Root
				.getScheduleEndDate(getApplicationContext())) {
			// Toast.makeText(getApplicationContext(), "Your sched, duration)
		}
		AdManager.setAllowUseOfLocation(true);
		AdManager.setTestDevices(new String[] { "0A3A55190402402C",
				AdManager.TEST_EMULATOR });
		

	}

	@Override
	public void onLocationChanged(Location l) {
		setLastKnownLocation(l);
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

	public void reverseTrip() {
		Station tmp = departureStation;
		departureStation = arrivalStation;
		arrivalStation = tmp;
	}

	public void setAdapter(DatabaseAdapter adapter) {
		this.adapter = adapter;
	}

	public void setArrivalStation(Station arrivalStation) {
		this.arrivalStation = arrivalStation;
	}

	public void setDepartureDate(Calendar departureDate) {
		this.departureDate = departureDate;
	}

	public void setDepartureStation(Station departureStation) {
		this.departureStation = departureStation;
	}

	public void setLastKnownLocation(Location lastKnownLocation) {
		this.lastKnownLocation = lastKnownLocation;
	}

	public void setPreferences(Preferences preferences) {
		this.preferences = preferences;
	}

	public void setServices(List<IService> services) {
		this.services = new HashMap<Integer, IService>();
		for (IService s : services) {
			this.services.put(s.getId(), s);
		}
	}

	public void setStationOrderType(int stationOrderType) {
		this.stationOrderType = stationOrderType;
	}

	public void setStations(ArrayList<Station> stations) {
		this.stations = stations;
	}

}
