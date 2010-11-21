package com.njtransit;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.njtransit.domain.AlternateService;
import com.njtransit.domain.IService;
import com.njtransit.domain.Service;
import com.njtransit.domain.Station;
import com.njtransit.domain.Stop;
import com.njtransit.domain.Trip;
import com.njtransit.model.StopsQueryResult;

public class DatabaseAdapter {
	public static String[] DAYS = new String[] {"sunday","monday","tuesday","wednesday","thursday","friday","saturday"};
	private static SimpleDateFormat DF = new SimpleDateFormat("yyyyMMdd");
	private static SimpleDateFormat DTF = new SimpleDateFormat("HH:mm:ss");

	private Context context;

	private SQLiteDatabase db;

	private TransitDBHelper helper;

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
				stations.add(new Station(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3)));
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

	public ArrayList<IService> getServices() {
		long before = System.currentTimeMillis();
		Cursor cursor = db.rawQuery("select service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday from calendar", null);
		cursor.moveToFirst();
		ArrayList<IService> services = new ArrayList<IService>(cursor.getCount());
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
		
		Log.d("DatebaseAdapter", String.format("getServices (%s ms)", System.currentTimeMillis() - before));
		
		return services;
	}

	
	public ArrayList<Station> getStations() {
		Long start = System.currentTimeMillis();	    
	    Cursor cursor = db.rawQuery("select id,name,lat,lon from stops order by name",null);
	    ArrayList<Station> stations = new ArrayList<Station>(cursor.getCount());
	    for(int i =0; i < cursor.getCount();i++) {
	      cursor.moveToNext();
	      int id = cursor.getInt(0);
	      String name = cursor.getString(1);
	      //String descName = cursor.getString(2);
	      float lat = cursor.getFloat(2);
	      float lng = cursor.getFloat(3);
	      Station s = new Station(id,name,(double)lat,(double)lng);
	      //s.setDescriptiveName(descName);
	      stations.add(s);
	    }
	    cursor.close();	    
	    Long end = System.currentTimeMillis();
	    Root.saveGetStationsDuration(context,end-start);
	    Log.d(getClass().getSimpleName(),"getStations() took " + Root.getGetStationsDuration(context)+ "ms");
	    return stations;
	}
	
	public StopsQueryResult getStopTimesAlternate(Station depart, Station arrive) {
		long before = System.currentTimeMillis();
		
		try {
			Calendar cal = Calendar.getInstance();
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH)+1;
			int day = cal.get(Calendar.DAY_OF_MONTH);
			String today = year+""+(month<10 ? "0"+month : month)+""+(day < 10 ? ("0"+day) : day);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			int year2 = cal.get(Calendar.YEAR);
			int month2 = cal.get(Calendar.MONTH)+1;
			int day2 = cal.get(Calendar.DAY_OF_MONTH);
			String tomorrow = year2+""+(month2<10 ? "0"+month2 : month2)+""+(day2 < 10 ? ("0"+day2) : day2);
			Cursor c = db.rawQuery("select service_id, calendar_date from calendar_dates where calendar_date in ('"+today+"', '"+tomorrow+"') ",null);
			
			Map<Integer, IService> services = new HashMap<Integer,IService>(c.getCount());
			Set<Calendar> dates = new HashSet<Calendar>();
			for(int i = 0; i < c.getCount(); i++) {
				c.moveToNext();
				int id = c.getInt(0);
				String dateString = c.getString(1);
				long date = DF.parse(dateString).getTime();
				Calendar dateCal = Calendar.getInstance();
				dateCal.setTimeInMillis(date);
				AlternateService service = (AlternateService)services.get(id);
				if(service==null) {
					service = new AlternateService(id);
					services.put(id,service);
				}
				service.getDates().add(dateCal);
				dates.add(dateCal);
			}
			c.close();
			StringBuilder b = new StringBuilder(" service_id=-1 ");
			for(Map.Entry<Integer, IService> k : services.entrySet()) {
				b.append(" or ");
				b.append(" service_id=");
				b.append(services.get(k.getKey()).getId());
			}
			
			String sql = String.format("select d.departure, a.arrival, d.trip_id from stop_times d join stop_times a on (a.stop_id=%s) where d.stop_id=%s and d.trip_id=a.trip_id and a.trip_id in (select id from trips where %s) and d.sequence<a.sequence ",arrive.getId(),depart.getId(),b);
			
			c = db.rawQuery(sql,null);
			int count = c.getCount();
			
			List<Stop> times = new ArrayList<Stop>(count);
			Calendar now = Calendar.getInstance();
			Set<Integer> tripIds = new HashSet<Integer>();
			for(int i = 0; i < count; i++) {
				c.moveToNext();
				Date departure = DTF.parse(c.getString(0));
				Date arrival = DTF.parse(c.getString(1));
				Calendar temp = Calendar.getInstance();
				Calendar dc = Calendar.getInstance();
				if(c.getInt(2)==2191) {
					System.out.println("ok");
				}
				temp.setTime(departure);
				dc.set(Calendar.YEAR, now.get(Calendar.YEAR));
				dc.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
				int hour = temp.get(Calendar.HOUR_OF_DAY);
				int minute = temp.get(Calendar.MINUTE);
				dc.set(Calendar.HOUR_OF_DAY, hour);
				dc.set(Calendar.MINUTE, minute);
				dc.set(Calendar.SECOND, 0);
				Calendar ac = Calendar.getInstance();
				//ac.setTimeInMillis(s.getValue().arrival);
				temp.setTime(arrival);
				ac.set(Calendar.YEAR, now.get(Calendar.YEAR));
				ac.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
				hour = temp.get(Calendar.HOUR_OF_DAY);
				minute = temp.get(Calendar.MINUTE);
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
				
				Stop stop = new Stop(c.getInt(2),dc,ac);
				tripIds.add(stop.getTripId());
				times.add(stop);
			}
			
			Log.d("DatabaseAdapter", String.format("getStopTimesAlternate (%s ms)", (System.currentTimeMillis() - before)));
			
			return new StopsQueryResult(depart, arrive, 0L, 1L, getTrips(services, tripIds), times);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public StopsQueryResult getStopTimes(final Map<Integer, IService> services, Station depart, Station arrive, int...days) {
		long before = System.currentTimeMillis();
		
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
			if(services.size()==0) {
				
			} else {
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
			HashMap<Integer,IService> trips =  getTrips(services, tripIds);
			 sqr = new StopsQueryResult(depart,arrive,queryStart,queryEnd,trips,stops);
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		} finally {			
			db.endTransaction();
		}
		
		Log.d("DatabaseAdapter", String.format("getStopTimes (%s ms)", System.currentTimeMillis() - before));
		
		return sqr;
	}

	/** Return at most 2 trips for a station. North | South bound */
	public ArrayList<Trip> getTrips(Integer stationId) {
		long before = System.currentTimeMillis();		
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
		Cursor cursor = db.rawQuery("select trips.id, trips.service_id, trips.route_id, trips.headsign, trips.direction, trips.block_id from stop_times join trips where ? = stop_times.stop_id AND stop_times.trip_id=trips.id group by trips.direction", new String[] {
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
		
		Log.d("DatabaseAdapter", String.format("getTrips (%s ms)", (System.currentTimeMillis() - before)));
		
		return trips;
	}

	public HashMap<Integer, IService> getTrips(Map<Integer, IService> services, Collection<Integer> tripIds) {
		long before = System.currentTimeMillis();
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
		HashMap<Integer, IService> tripToService = new HashMap<Integer,IService>();
		for(int i = 0; i < cursor.getCount(); i++) {
			tripToService.put(cursor.getInt(0),services.get(cursor.getInt(1)));
			cursor.moveToNext();
		}
		cursor.close();
		
		Log.d("DatabaseAdapter", String.format("getTrips (%s ms)", (System.currentTimeMillis() - before)));
		
		return tripToService;
	}

	public ArrayList<Trip> getTrips(Station station) {
		return station == null ? new ArrayList<Trip>() : getTrips(station.getId());
	}	

	public void saveHistory(Integer departureId, Integer arrivalId, Long queried) {
		localDb.beginTransaction();
		String query = "select id from trip_summary where station_depart=%s and station_arrive=%s limit 10";
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

	public ArrayList<Station> getMostVisitedStations(SchedulerApplication app, Long now) {
		String query = "select station_depart, station_arrive from trip_summary order by total desc";
		Cursor cursor = localDb.rawQuery(query, null);
		ArrayList<Station> station = new ArrayList<Station>();
		cursor.moveToFirst();
		for(int i = 0; i < cursor.getCount(); i++) {
			int depart = cursor.getInt(0);
			int arrive = cursor.getInt(1);
			Station departS = app.getStation(depart);
			Station arriveS = app.getStation(arrive);
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
			helper = new TransitDBHelper(context);
			File dbFile = context.getDatabasePath("database.sqlite");			
			String moveOnRestart = Root.getMoveOnRestart(context);
			if(moveOnRestart!=null) {
				File file = new File(context.getFilesDir(), moveOnRestart);
				if(file.exists()) {
					ProgressDialog d = ProgressDialog.show(context, "Installing", "Installing updated database");
					boolean moved = file.renameTo(dbFile);
					if(moved) {
						Root.setMoveOnRestart(context, null);
					}
					d.cancel();
				}
			}
			helper.createDataBase(dbFile.getName());
			helper.openDataBase(dbFile.getName());
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

	public void deleteFavorites() {
		localDb.beginTransaction();
		localDb.execSQL("delete from trip_history");
		localDb.execSQL("delete from trip_summary");
		localDb.setTransactionSuccessful();
		localDb.endTransaction();	
	}
}