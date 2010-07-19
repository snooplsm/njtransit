package com.njtransit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.njtransit.domain.Service;
import com.njtransit.domain.Session;
import com.njtransit.domain.Station;
import com.njtransit.domain.Stop;
import com.njtransit.domain.Trip;
import com.njtransit.model.StopsQueryResult;

public class DatabaseAdapter {

	public static String[] DAYS = new String[] {"sunday","monday","tuesday","wednesday","thursday","friday","saturday"};

	private static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");

	private static String[] STATION_COLUMNS = new String[] {"id","name","lat","lon","zone_id"};

	private Context context;

	private SQLiteDatabase db;

	private NJTransitDBHelper helper;

	private SQLiteDatabase localDb;

	private LocalStorageHelper localStorageHelper;

	private Integer tempTableIndex = 0;

	public DatabaseAdapter(Context context) {
		this.context = context;
	}

	public void closeDB() {
		helper.close();
		localStorageHelper.close();
	}

	public int countStations() {
		Cursor cursor = db.rawQuery("select count(*) from stops", null);
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		cursor.close();
		return count;
	}

	/**
	 * This will load ~250 stations, should we page them?  Right now sqlite doesn't suppor trig functions so its easier to do it this way.
	 * prob takes 8-32K of mem to represent all this data.
	 * @return all stations
	 */
	public ArrayList<Station> getAllStations() {
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
		return stations;
	}
	
	public ArrayList<Station> getAllStationsLike(String name) {
		Cursor cursor = null;
		ArrayList<Station> stations = null;
		try {
			db.beginTransaction();	
			String sql = "select id, name, lat, lon, zone_id from stops where name like ?";
			cursor = db.rawQuery(sql, new String[] { "%"+name+"%"});
			int count = cursor.getCount();
			stations = new ArrayList<Station>(count);
			cursor.moveToFirst();
			while(count > 0) {
				count--;
				stations.add(new Station(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), null));
				cursor.moveToNext();
			}
			cursor.close();
		} finally {
			if(cursor != null) {
				cursor.close();
			}
			db.endTransaction();
		}		
		return stations;
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
		cursor.close();
		return services;
	}

	public StopsQueryResult getStopTimes(final Map<Integer, Service> services, Station depart, Station arrive, int...days) {
		db.beginTransaction();

		StopsQueryResult sqr = null;
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
			 sqr = new StopsQueryResult(depart,arrive,queryStart,queryEnd,trips,stops);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {			
			db.endTransaction();
		}
		return sqr;
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

	public ArrayList<Trip> getTrips(Station station) {
		return station == null ? new ArrayList<Trip>() : getTrips(station.getId());
	}	

	public void saveHistory(Integer departureId, Integer arrivalId, Long queried) {
		localDb.beginTransaction();
		String query = "select id from trip_summary where station_depart=%s and station_arrive=%s";
		query = String.format(query,departureId,arrivalId);
		Cursor cursor = localDb.rawQuery(query,null);
		Integer id = null;
		if(cursor.getCount()==1) {
			cursor.moveToFirst();
			id = cursor.getInt(0);
			query = "update trip_summary set total=total+1 where id = " + id;
			localDb.execSQL(query);
		} else {
			query = "insert into trip_summary (station_depart, station_arrive, created, total) values (%s,%s,datetime(%s,'unixepoch','gmt'),1) ";
			query = String.format(query,departureId,arrivalId,queried/1000);			
			localDb.execSQL(query);
		}
		cursor.close();
		query = "select id from trip_summary where station_depart=%s and station_arrive=%s";
		query = String.format(query,departureId,arrivalId);
		cursor = localDb.rawQuery(query, null);

		cursor.moveToFirst();
		int summaryId = cursor.getInt(0);
		cursor.close();
		query = "insert into trip_history (trip_summary, created) values (%s,datetime(%s,'unixepoch','gmt'))";
		query = String.format(query,summaryId,queried);
		localDb.execSQL(query);
		localDb.setTransactionSuccessful();
		localDb.endTransaction();
	}

	public ArrayList<Station> getMostVisitedStations(Session session, Long now) {
		String query = "select station_depart, station_arrive from trip_summary order by total desc";
		Cursor cursor = localDb.rawQuery(query, null);
		ArrayList<Station> station = new ArrayList<Station>();
		cursor.moveToFirst();
		for(int i = 0; i < cursor.getCount(); i++) {
			int depart = cursor.getInt(0);
			int arrive = cursor.getInt(1);
			Station departS = session.getStation(depart);
			Station arriveS = session.getStation(arrive);
			if(!station.contains(departS)) {
				station.add(departS);
			}
			if(!station.contains(arriveS)) {
				station.add(arriveS);
			}
			cursor.moveToNext();
		}
		cursor.close();
		return station;
	}

	public DatabaseAdapter open() {
		if(db!=null) {
			return this;
		}
		try {
			helper = new NJTransitDBHelper(context);
			final String atPath = context.getDatabasePath("njtransit.sqlite").getAbsolutePath();
			helper.createDataBase(atPath);
			helper.openDataBase(atPath);
			db = helper.getWritableDatabase();
			localStorageHelper = new LocalStorageHelper(context);
			localDb = localStorageHelper.getWritableDatabase();
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
}