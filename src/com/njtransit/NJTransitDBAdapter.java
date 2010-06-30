package com.njtransit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.njtransit.domain.Route;
import com.njtransit.domain.Station;
import com.njtransit.domain.Stop;
import com.njtransit.domain.StopTime;
import com.njtransit.domain.Trip;

public class NJTransitDBAdapter {

	public NJTransitDBAdapter(Context context) {
		this.context = context;
	}
	
	private Context context;
	
	private SQLiteDatabase db;
	
	private NJTransitDBHelper helper;
	
	private boolean mocking = false;
	
	private static String[] STATION_COLUMNS = new String[] {"id","name","lat","lon","zone_id"};
	private static String[] ROUTE_COLUMNS = new String[] {"id", "agency_id", "short_name", "long_name", "route_type"};
	
	public NJTransitDBAdapter open() {
		try {
			helper = new NJTransitDBHelper(context);
			final String atPath = context.getDatabasePath("njtransit.sqlite").getAbsolutePath();
			helper.createDataBase(atPath);
			helper.openDataBase(atPath);
			db = helper.getWritableDatabase();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
			return new Station(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), null);
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
			stations.add(new Station(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), null));
			cursor.moveToNext();
		}
		cursor.close();
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
			routes.add(new Route(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4)));
			cursor.moveToNext();
		}
		cursor.close();
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
	public List<Station> stationsWithin(Station station, Trip trip) {
		List<Station> stations = new ArrayList<Station>();
		if(mocking) {
			stations.addAll(mockStationsWithin());
			return stations;
		}
		char gtlt = trip.getDirection()==0 ? '>' : '<';
		String sql = "select s.id, s.name, s.lat, s.lon, s.zone_id from stop_times st inner join stops s on s.id=st.stop_id where st.trip_id=? and st.sequence " + gtlt + " (select sequence from stop_times where stop_id = ? and trip_id = ?)";
		Cursor cursor = db.rawQuery(sql, new String[] { trip.getId().toString(),
				station.getId().toString(), trip.getId().toString(), 
		});
		
		cursor.moveToFirst();
		for(int i = 0; i < cursor.getCount(); i++) {
			stations.add(new Station(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getInt(4)));
			cursor.moveToNext();
		}
		cursor.close();
		return stations;
	}
	
	public List<StopTime> getAllStopTimes(Integer sid, Trip trip) {
		if(mocking) {
			return mockTimes(sid, trip.getId());
		}
		
		Cursor cursor = db.rawQuery(
				"select time(arrival,'unixepoch','localtime'), time(departure,'unixepoch','localtime') " +
				"from stop_times where trip_id in " +
					"(select id from trips where route_id = ?)"
						, new String[] {
						trip.getRouteId().toString(), sid.toString()
				});
		
		cursor.moveToFirst();
		ArrayList<StopTime> stopTimes = new ArrayList<StopTime>(cursor.getCount());
		for(int i = 0; i < cursor.getCount(); i++) {
			
			Calendar a = Calendar.getInstance();			
			a.setTimeInMillis(cursor.getLong(0));
			
			Calendar d = Calendar.getInstance();
			d.setTimeInMillis(cursor.getLong(1));			
			
			stopTimes.add(new StopTime(sid, trip.getId(), a,
					d, null, null,
					null));
						
			cursor.moveToNext();
		}
		cursor.close();
		return stopTimes;
	}
	
	private Integer tempTableIndex = 0;
	
	private static String[] DAYS = new String[] {"sunday","monday","tuesday","wednesday","thursday","friday","saturday"};
	
	
	
	public ArrayList<Stop> getStopTimes(Station depart, Station arrive) {
		db.beginTransaction();
		int tempTableIndex = ++this.tempTableIndex;
		String tableName = "atrips"+tempTableIndex;
		try{
			String createTableStatement = String.format("create temporary table %s (id int)", tableName);
			db.execSQL(createTableStatement);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			int day = cal.get(Calendar.DAY_OF_WEEK);
			String dayA = DAYS[day-1];
			String dayB = DAYS[day % 7];
			String calendarSql = String.format("select service_id from calendar where %s=1 or %s=1",dayA,dayB);
			Cursor c = db.rawQuery(calendarSql, null);
			c.moveToFirst();
			ArrayList<Integer> count = new ArrayList<Integer>();
			for(int i = 0; i < c.getCount(); i++) {
				Integer val = c.getInt(0);
				count.add(val);
				c.moveToNext();
			}
			c.close();
			StringBuilder b = new StringBuilder("(");
			for(Iterator<Integer> i = count.iterator(); i.hasNext();) {
				b.append(i.next());
				if(i.hasNext()) {
					b.append(",");
				}				
			}
			b.append(")");
			
//			String newTempTable ="mytemp"+tempTableIndex;
//			db.execSQL(String.format("create temporary table %s (stop_id_a int, stop_id_b int, departure_a int, departure_b int, sequence_a int, sequence_b int, trip_id_a int, trip_id_b int)",newTempTable));
//			c = db.rawQuery(String.format("insert into %s select a.stop_id,null,a.sequence,null,a.departure,null,a.trip_id,null from stop_times a where a.stop_id=%s order by a.trip_id",newTempTable,arrive.getId()), null);
//			c = db.rawQuery(String.format("insert into %s select null,a.stop_id,null,null,a.sequence,null,a.departure,null,a.trip_id from stop_times a where a.stop_id=%s order by a.trip_id",newTempTable,depart.getId()), null);
//			c = db.rawQuery(String.format("update %s set stop_id_b=, args), selectionArgs)
//			int count2 = c.getCount();
//			
			db.execSQL(String.format("insert into %s select id from trips where service_id in " + b.toString(),tableName));
			c = db.rawQuery(String.format("select st.trip_id, time(st.departure,'unixepoch'), time(sp.arrival,'unixepoch'), time(st.departure,'unixepoch'), time(sp.arrival,'unixepoch') from stop_times st join stop_times sp on (sp.stop_id=%s) where st.stop_id=%s AND st.trip_id=sp.trip_id and st.trip_id in (select id from %s) AND st.sequence < sp.sequence order by st.departure",arrive.getId(),depart.getId(),tableName), null);
			//Long now = System.currentTimeMillis();
			c.moveToFirst();
			//Long after = System.currentTimeMillis();

			ArrayList<Stop> stops = new ArrayList<Stop>(c.getCount());
			for(int i = 0; i < c.getCount(); i++) {
				String[] args = c.getString(1).split(":");
				int hourDepart = toInt(args[0]);
				int minuteDepart = toInt(args[1]);
				args = c.getString(2).split(":");
				int hourArrive = toInt(args[0]);
				int minuteArrive = toInt(args[1]);
				Stop stop = new Stop(c.getInt(0),hourDepart*3600000+minuteDepart+60000,hourArrive*3600000+minuteArrive*60000);
				stops.add(stop);
				c.moveToNext();
			}
			c.close();
			db.execSQL("drop table " + tableName);
			return stops;
		} finally {
			
		}
	}
	
	private int toInt(String s) {
		return Integer.parseInt(s);
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
		cursor.close();
		db.endTransaction();
		return stopTimes;
	}
	
	public Trip getTrip(Integer id) {
		return new Trip(id, 1, "343 River Line Camden",
				0, "175B43003", 1);
	}
	
	/** Return at most 2 trips for a station. North | South bound */
	public ArrayList<Trip> getTrips(Integer stationId) {
		if(stationId == null) {
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
		Cursor cursor = db.rawQuery("select trips.id, trips.service_id, trips.route_id, trips.headsign, trips.direction, trips.block_id from stop_times join trips where ? = stop_times.stop_id AND stop_times.trip_id=trips.id group by trips.direction",new String[] {
		  stationId.toString() 
		});
		int count = cursor.getCount();
		ArrayList<Trip> trips = new ArrayList<Trip>(Math.max(0,count));
		cursor.moveToFirst();
		for(int i =0; i < cursor.getCount(); i++) {
			trips.add(new Trip(cursor.getInt(0), cursor.getInt(1), cursor.getString(3), cursor.getInt(4), cursor.getString(5),  null));
			cursor.moveToNext();
		}
		cursor.close();
		db.endTransaction();
		return trips;
	}
	
	public ArrayList<Trip> getTrips(Station station) {
		return station == null ? new ArrayList<Trip>() : getTrips(station.getId());
	}	
	
	public int countStations() {
		Cursor cursor = db.rawQuery("select count(*) from stops", null);
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		cursor.close();
		return count;
	}
}