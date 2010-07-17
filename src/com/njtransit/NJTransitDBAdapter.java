package com.njtransit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.njtransit.domain.Route;
import com.njtransit.domain.Service;
import com.njtransit.domain.Station;
import com.njtransit.domain.Stop;
import com.njtransit.domain.StopTime;
import com.njtransit.domain.Trip;
import com.njtransit.model.StopsQueryResult;

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
		if(db!=null && db.isOpen()) {
			return this;
		}
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
	
	public void openDB() {
		if(db!=null && !db.isOpen()) {
			db = helper.getWritableDatabase();
		}
	}
	
	public void closeDB() {
		helper.close();
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
	
	public static String[] DAYS = new String[] {"sunday","monday","tuesday","wednesday","thursday","friday","saturday"};
	
	
	
	public StopsQueryResult getStopTimes(final Map<Integer, Service> services, Station depart, Station arrive, int...days) {
		db.beginTransaction();
		int tempTableIndex = ++this.tempTableIndex;
		String tableName = "atrips"+tempTableIndex;
		try{
			String createTableStatement = String.format("create temporary table %s (id int)", tableName);
			db.execSQL(createTableStatement);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			String calendarSql = null;
			Cursor c;
			ArrayList<Integer> count = new ArrayList<Integer>();
			if(days.length>0) {
				String query = "";
				for(int i = 0 ; i < days.length; i++) {
					int day = days[i];
					
					if(i!=0 && i!=days.length-1) {
						query+=" or ";
					}
					query+=DAYS[day-1];
					query+="=1";
				}
				calendarSql = "select service_id from calendar where " + query;
				c = db.rawQuery(calendarSql, null);
				c.moveToFirst();
				for(int i = 0; i < c.getCount(); i++) {
					Integer val = c.getInt(0);
					count.add(val);
					c.moveToNext();
				}
				c.close();
			} else {
				int day = cal.get(Calendar.DAY_OF_WEEK);
				String dayA = DAYS[day-1];
				String dayB = DAYS[day % 7];
				calendarSql = String.format("select service_id from calendar where %s=1 or %s=1",dayA,dayB);
				c = db.rawQuery(calendarSql, null);
				c.moveToFirst();
				for(int i = 0; i < c.getCount(); i++) {
					Integer val = c.getInt(0);
					count.add(val);
					c.moveToNext();
				}
				c.close();
			}
			StringBuilder b = new StringBuilder("(");
			for(Iterator<Integer> i = count.iterator(); i.hasNext();) {
				b.append(i.next());
				if(i.hasNext()) {
					b.append(",");
				}				
			}
			b.append(")");
			db.execSQL(String.format("insert into %s select id from trips where service_id in " + b.toString(),tableName));
			c = db.rawQuery(String.format("select st.trip_id, datetime(st.departure,'unixepoch'), datetime(sp.arrival,'unixepoch'), time(st.departure,'unixepoch'), time(sp.arrival,'unixepoch') from stop_times st join stop_times sp on (sp.stop_id=%s) where st.stop_id=%s AND st.trip_id=sp.trip_id and st.trip_id in (select id from %s) AND st.sequence < sp.sequence order by st.departure",arrive.getId(),depart.getId(),tableName), null);
			Long queryStart = System.currentTimeMillis();
			c.moveToFirst();
			Long queryEnd = System.currentTimeMillis();

			ArrayList<Stop> stops = new ArrayList<Stop>(c.getCount());
			HashSet<Integer> tripIds = new HashSet<Integer>();
			
			Calendar now = Calendar.getInstance();
			for(int i = 0; i < c.getCount(); i++) {
				String dept = c.getString(1);
				String arrv = c.getString(2);
				Calendar temp = Calendar.getInstance();
				Calendar dc = Calendar.getInstance();
				temp.setTime(DF.parse(dept));
				dc.set(Calendar.YEAR, now.get(Calendar.YEAR));
				dc.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
				dc.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
				dc.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
				dc.set(Calendar.SECOND, 0);
				Calendar ac = Calendar.getInstance();
				temp.setTime(DF.parse(arrv));
				ac.set(Calendar.YEAR, now.get(Calendar.YEAR));
				ac.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
				ac.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
				ac.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));			
				ac.set(Calendar.SECOND, 0);
				if(ac.get(Calendar.DAY_OF_YEAR)!=dc.get(Calendar.DAY_OF_YEAR)) {
					ac.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
					dc.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
				}
				if(ac.get(Calendar.HOUR_OF_DAY)<dc.get(Calendar.HOUR_OF_DAY)) {
					ac.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR)+1);
				}
				int tripId = c.getInt(0);
				tripIds.add(tripId);

				Stop stop = new Stop(tripId,dc,ac);
				stops.add(stop);
				c.moveToNext();
			}
			c.close();
			db.execSQL("drop table " + tableName);
			HashMap<Integer,Service> trips =  getTrips(services, tripIds);
			StopsQueryResult sqr = new StopsQueryResult(depart,arrive,queryStart,queryEnd,trips,stops);
			return sqr;
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {			
			db.endTransaction();
		}
		return null;
	}
	
	private static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
	
	public HashMap<Integer, Service> getTrips(Map<Integer, Service> services, Collection<Integer> tripIds) {
		db.beginTransaction();
		StringBuilder b = new StringBuilder("(");
		for(Iterator<Integer> i = tripIds.iterator(); i.hasNext();) {
			b.append(i.next());
			if(i.hasNext()) {
				b.append(",");
			}				
		}
		b.append(")");
		Cursor cursor = db.rawQuery(String.format("select t.id, t.service_id from trips t where t.id in %s", b.toString()),null);
		cursor.moveToFirst();
		HashMap<Integer, Service> tripToService = new HashMap<Integer,Service>();
		for(int i = 0; i < cursor.getCount(); i++) {
			tripToService.put(cursor.getInt(0),services.get(cursor.getInt(1)));
			cursor.moveToNext();
		}
		cursor.close();
		db.endTransaction();
		return tripToService;
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

	public ArrayList<Service> getServices() {
		Cursor cursor = db.rawQuery("select service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday from calendar", null);
		cursor.moveToFirst();
		ArrayList<Service> services = new ArrayList<Service>(cursor.getCount());
		for(int i = 0; i < cursor.getCount(); i++) {
			boolean[] result = new boolean[7];
			result[0] = cursor.getInt(1)==1;
			result[1] = cursor.getInt(2)==1;
			result[2] = cursor.getInt(3)==1;
			result[3] = cursor.getInt(4)==1;
			result[4] = cursor.getInt(5)==1;
			result[5] = cursor.getInt(6)==1;
			result[6] = cursor.getInt(7)==1;
			
			Service s = new Service(cursor.getInt(0),result);
			services.add(s);
			cursor.moveToNext();
		}
		return services;
	}
}