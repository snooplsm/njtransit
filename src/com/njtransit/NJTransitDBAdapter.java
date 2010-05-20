package com.njtransit;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.njtransit.domain.Route;
import com.njtransit.domain.Station;
import com.njtransit.domain.StopTime;
import com.njtransit.domain.Trip;

public class NJTransitDBAdapter {

	public NJTransitDBAdapter(Context context) {
		this.context = context;
	}
	
	private static final int VERSION = 5;
	
	private Context context;

	private NJTransitDBHelper helper;
	
	private SQLiteDatabase db;
	
	private static String[] STATION_COLUMNS = new String[] {"id","name","lat","lon","zone_id"};
	private static String[] ROUTE_COLUMNS = new String[] {"id", "agency_id", "short_name", "long_name", "route_type"};
	private static String[] TRIP_COLUMNS = new String[] {"id", "service_id", "route_id", "headsign", "direction", "block_id"};
	private static String[] STOPTIME_COLUMNS = new String[] {"arrival","departure","sequence","pickup_type","drop_off_type"};
	
	public NJTransitDBAdapter open() {
		helper = new NJTransitDBHelper(context, "njtransit", null, VERSION);
		//readable
		db = helper.getWritableDatabase();
		return this;
	}
	
	
	public Station getStation(Integer id) {
		Cursor cursor = db.query("stops", STATION_COLUMNS, "id=?", new String[] {
		  id.toString()
		}, null, null, null);
		int cnt = cursor.getCount();
		cursor.moveToFirst();
		while(cnt > 0) {
			cnt--;			
			Station station = new Station();
			station.setId(cursor.getInt(0));
			station.setName(cursor.getString(1));
			station.setLatitude(cursor.getDouble(2));
			station.setLongitude(cursor.getDouble(3));
			return station;
		}
		return null;
	}
	
	/**
	 * This will load ~250 stations, should we page them?  Right now sqlite doesn't suppor trig functions so its easier to do it this way.
	 * prob takes 8-32K of mem to represent all this data.
	 * @return all stations
	 */
	public ArrayList<Station> getAllStations() {
		db.beginTransaction();
		Cursor cursor = db.query("stops", STATION_COLUMNS, null, null, null, null, "name");
		int count = cursor.getCount();
		ArrayList<Station> stations = new ArrayList<Station>(count);
		cursor.moveToFirst();
		while(count>0) {
			count--;			
			Station station = new Station();
			station.setId(cursor.getInt(0));
			station.setName(cursor.getString(1));
			station.setLatitude(cursor.getDouble(2));
			station.setLongitude(cursor.getDouble(3));
			stations.add(station);
			cursor.moveToNext();
		}
		db.endTransaction();
		return stations;
	}
	
	/**
	 * This will load ~12 routes.
	 * 
	 * @return routes
	 */
	public ArrayList<Route> getAllRoutes() {
		db.beginTransaction();
		Cursor cursor = db.query("routes", ROUTE_COLUMNS, null, null, null, null, null);
		int count = cursor.getCount();
		ArrayList<Route> routes = new ArrayList<Route>(count);
		cursor.moveToFirst();
		while(count>0) {
			count--;
			Route route = new Route();
			route.setId(cursor.getInt(0));
			route.setAgencyId(cursor.getInt(1));
			route.setShortName(cursor.getString(2));
			route.setLongName(cursor.getString(3));
			route.setRouteType(cursor.getInt(4));
			cursor.moveToNext();
		}
		db.endTransaction();
		return routes;
	}
	
	public ArrayList<StopTime> getAllStopTimes(Station station, Trip trip) {
		db.beginTransaction();
		Cursor cursor = db.rawQuery("select arrival, departure from stop_times where trip_id=? and stop_id=?", new String[] {trip.getId().toString(), station.getId().toString()});
		cursor.moveToFirst();
		ArrayList<StopTime> stopTimes = new ArrayList<StopTime>(cursor.getCount());
		for(int i = 0; i < cursor.getCount(); i++) {
			StopTime t = new StopTime();
			t.setStationId(station.getId());
			t.setTripId(trip.getId());
			Calendar a = Calendar.getInstance();			
			Long arrival = cursor.getLong(0);
			a.setTimeInMillis(arrival);
			t.setArrival(a);
			Long departure = cursor.getLong(1);
			Calendar d = Calendar.getInstance();
			t.setDeparture(d);
			d.setTimeInMillis(departure);			
			cursor.moveToNext();
			stopTimes.add(t);
		}
		db.endTransaction();
		return stopTimes;
	}
	
	public Trip getTrip(Integer id) {
		return new Trip(id, 1, "343 River Line Camden",
				0, "175B43003", 1);
	}
	
	/** Return at most 2 trips for a station. North | South bound */
	public ArrayList<Trip> getTrips(Integer stationId) {
		if(stationId == null || true) {
			return new ArrayList<Trip>(){
				private static final long serialVersionUID = 1L;
				{
					add(new Trip(1, 1, "343 River Line Camden",
							0, "175B43003", 1));
					add(new Trip(1, 1, "343 River Line Trenton",
							1, "175B43001", 1));
				}
			};
		}
		db.beginTransaction();
		ArrayList<Trip> trips = new ArrayList<Trip>();
		Cursor cursor = db.rawQuery("select trips.id, trips.service_id, trips.route_id, trips.headsign, trips.direction, trips.block_id from stop_times join trips where ? = stop_times.stop_id AND stop_times.trip_id=trips.id group by trips.direction",new String[] {
		  stationId.toString() 
		});
		cursor.moveToFirst();
		for(int i =0; i < cursor.getCount(); i++) {
			Trip t = new Trip();
			t.setId(cursor.getInt(0));
			t.setServiceId(cursor.getInt(1));
			t.setRouteId(cursor.getInt(2));
			t.setHeadsign(cursor.getString(3));
			t.setDirection(cursor.getInt(4));
			t.setBlockId(cursor.getString(5));
			trips.add(t);
			cursor.moveToNext();
		}
		db.endTransaction();
		return trips;
	}
	
	public ArrayList<Trip> getTrips(Station station) {
		return station == null ? new ArrayList<Trip>() : getTrips(station.getId());
	}	
}