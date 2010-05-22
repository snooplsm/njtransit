package com.njtransit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
	
	private boolean mocking = true;
	
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
	
	
	private List<StopTime> mockTimes(Integer sid, Integer tid) {
		ArrayList<StopTime> times = new ArrayList<StopTime>();
		Calendar a = Calendar.getInstance();			
		a.setTimeInMillis(System.currentTimeMillis() * 60 * 60);
		
		Calendar d = Calendar.getInstance();
		d.setTimeInMillis(System.currentTimeMillis() * 60 * 60 * 60);			
		
		times.add(new StopTime(sid, tid, a,
				d, null, null,
				null));
		
		times.add(new StopTime(sid, tid, a,
				d, null, null,
				null));
		return times;
	}
	
	private List<Station> mockStationsWithin() {
		return new ArrayList<Station>(20) {
			private static final long serialVersionUID = 1L;
		{
			add(new Station(1,"30TH ST. PHL.",39.956565,-75.182327,5961));
			add(new Station(2,"ABSECON",39.424333,-74.502091,333));
			add(new Station(3,"ALLENDALE",41.030902,-74.130957,2893));
			add(new Station(4,"ALLENHURST",40.237659,-74.006769,5453));
			add(new Station(5,"ANDERSON STREET",40.894458,-74.043781,1357));
			add(new Station(6,"ANNANDALE",40.645173,-74.878569,5197));
			add(new Station(8,"ASBURY PARK",40.215359,-74.014782,5453));
			add(new Station(9,"ATCO",39.783547,-74.907588,4429));
			add(new Station(10,"ATLANTIC CITY",39.363573,-74.442523,77));
			add(new Station(11,"AVENEL",40.577620,-74.277530,2381));
			add(new Station(12,"BASKING RIDGE",40.711380,-74.555267,4173));
			add(new Station(13,"BAY HEAD",40.075843,-74.046931,5965));
			add(new Station(14,"BAY STREET",40.808178,-74.208681,1357));
			add(new Station(15,"BELMAR",40.180590,-74.027301,5709));
			add(new Station(17,"BERKELEY HEIGHTS",40.682345,-74.442649,2893));
			add(new Station(18,"BERNARDSVILLE",40.716847,-74.571023,4173));
			add(new Station(19,"BLOOMFIELD AVENUE",40.792709,-74.200043,1101));
			add(new Station(20,"BOONTON",40.903378,-74.407733,3661));
		}};
	}
	
	/** All stations within a given trips train line */
	public List<Station> stationsWithin(Integer tripId) {
		List<Station> stations = new ArrayList<Station>();
		if(mocking) {
			stations.addAll(mockStationsWithin());
			return stations;
		}
		
		Cursor cursor = db.rawQuery("select s.id, s.name, s.lat, s.lon, s.zone_id from stop_times st inner join stops s on s.id=st.fk_stop_id where ts.trip_id=?", new String[] {
				tripId.toString()
		});
		
		cursor.moveToFirst();
		for(int i = 0; i < cursor.getCount(); i++) {
			stations.add(new Station(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getInt(4)));
			cursor.moveToNext();
		}
		
		return stations;
	}
	
	public List<StopTime> getAllStopTimes(Integer sid, Integer tid) {
		if(mocking) {
			return mockTimes(sid, tid);
		}
		
		Cursor cursor = db.rawQuery("select arrival, departure from stop_times where trip_id=? and stop_id=?", new String[] {
			tid.toString(), sid.toString()
		});
		
		cursor.moveToFirst();
		ArrayList<StopTime> stopTimes = new ArrayList<StopTime>(cursor.getCount());
		for(int i = 0; i < cursor.getCount(); i++) {
			
			Calendar a = Calendar.getInstance();			
			a.setTimeInMillis(cursor.getLong(0));
			
			Calendar d = Calendar.getInstance();
			d.setTimeInMillis(cursor.getLong(1));			
			
			stopTimes.add(new StopTime(sid, tid, a,
					d, null, null,
					null));
						
			cursor.moveToNext();
		}
		return stopTimes;
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