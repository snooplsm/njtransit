package com.njtransit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

public class NJTransitDBHelper extends SQLiteOpenHelper {

	/** Does work within implicit transaction */
	private interface Transactional {
		public void work(SQLiteDatabase db);
	}
	
	/** Provides ContentValues objects for db insertion */
	private interface ContentValuesProvider {
		List<ContentValues> getContentValues(CSVReader reader) throws IOException;
	}
	
	/** Manages Transactional execution of work */
	private class TransactionManager {
		private SQLiteDatabase db;
		public TransactionManager(SQLiteDatabase db) {
			this.db = db;
		}
		
		public void exec(Transactional t) {
			db.beginTransaction();
			t.work(db);
			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}
	
	private Context context;
	
	public NJTransitDBHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		this.context = context;
	}
	
	/** @see SQLiteOpenHelper#onCreate(SQLiteDatabase) */
	@Override
	public void onCreate(SQLiteDatabase db) {
		final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
		final TransactionManager manager = new TransactionManager(db);
		
		createSchema(manager);
		
		load(manager, "stop_times", new ContentValuesProvider(){

			@Override
			public List<ContentValues> getContentValues(CSVReader reader) throws IOException {
				List<ContentValues> values = new ArrayList<ContentValues>();
				String[] nextLine;
				while((nextLine=reader.readNext())!=null) {
					//trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					ContentValues cv = new ContentValues();
					cv.put("trip_id", nextLine[0]);
					try {
						if(nextLine[1].trim().length()!=0) {
							cv.put("arrival", df.parse("01/01/1970 " + nextLine[1]).getTime());
						}
						if(nextLine[2].trim().length()!=0) {
							cv.put("departure", df.parse("01/01/1970 " + nextLine[2]).getTime());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					cv.put("stop_id", nextLine[3]);
					cv.put("sequence", nextLine[4]);
					cv.put("pickup_type", nextLine[5]);
					cv.put("drop_off_type", nextLine[6]);
					values.add(cv);
				}
				return values;
			}
		});
		
		load(manager, "stop_times", new ContentValuesProvider(){
			@Override
			public List<ContentValues> getContentValues(CSVReader reader)
					throws IOException {
				List<ContentValues> values = new ArrayList<ContentValues>();
				String[] nextLine;
				while((nextLine=reader.readNext())!=null) {
					//trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					ContentValues cv = new ContentValues();
					cv.put("trip_id", nextLine[0]);
					try {
						if(nextLine[1].trim().length()!=0) {
							cv.put("arrival", df.parse("01/01/1970 " + nextLine[1]).getTime());
						}
						if(nextLine[2].trim().length()!=0) {
							cv.put("departure", df.parse("01/01/1970 " + nextLine[2]).getTime());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					cv.put("stop_id", nextLine[3]);
					cv.put("sequence", nextLine[4]);
					cv.put("pickup_type", nextLine[5]);
					cv.put("drop_off_type", nextLine[6]);
					values.add(cv);
				}
				return values;
			}
		});
			
		load(manager, "agency", new ContentValuesProvider(){
			@Override
			public List<ContentValues> getContentValues(CSVReader reader)
					throws IOException {
				List<ContentValues> values = new ArrayList<ContentValues>();
				String[] nextLine;
				while((nextLine = reader.readNext())!=null) {
					int id = Integer.parseInt(nextLine[0]);
					String name = nextLine[1];
					String url = nextLine[2];
					ContentValues cv = new ContentValues();
					cv.put("id", id);
					cv.put("url", url);
					cv.put("name", name);
					values.add(cv);				
				}	
				return values;
			}
		});
		
		load(manager, "calendar", new ContentValuesProvider(){

			@Override
			public List<ContentValues> getContentValues(CSVReader reader)
					throws IOException {
				List<ContentValues> values = new ArrayList<ContentValues>();
				String[] nextLine;
				while((nextLine = reader.readNext())!=null) {
					int serviceId = Integer.parseInt(nextLine[0].trim());
					int monday = Integer.parseInt(nextLine[1].trim());
					int tuesday = Integer.parseInt(nextLine[2].trim());
					int wednesday = Integer.parseInt(nextLine[3].trim());
					int thursday = Integer.parseInt(nextLine[4].trim());
					int friday = Integer.parseInt(nextLine[5].trim());
					int saturday = Integer.parseInt(nextLine[6].trim());
					int sunday = Integer.parseInt(nextLine[7].trim());
					String start = nextLine[8];
					String end = nextLine[9];
					String year = start.substring(0,4);
					String month = start.substring(4,6);
					String day = start.substring(6,8);
					ContentValues cv = new ContentValues();
					cv.put("monday", monday);
					cv.put("tuesday", tuesday);
					cv.put("wednesday", wednesday);
					cv.put("thursday",thursday);
					cv.put("friday",friday);
					cv.put("saturday", saturday);
					cv.put("sunday", sunday);
					cv.put("start", year+"-"+month+"-"+day);
					year = end.substring(0,4);
					month = end.substring(4,6);
					day = end.substring(6,8);
					cv.put("end", year+"-"+month+"-"+day);
					values.add(cv);
				}
				return values;
			}
		});
			

		load(manager, "calendar_dates", new ContentValuesProvider(){

			@Override
			public List<ContentValues> getContentValues(CSVReader reader)
					throws IOException {
				List<ContentValues> values = new ArrayList<ContentValues>();
				String[] nextLine;
				while((nextLine = reader.readNext())!=null) {
					int serviceId = Integer.parseInt(nextLine[0]);
					String date = nextLine[1];
					int exceptionType = Integer.parseInt(nextLine[2]);
					String year = date.substring(0,4);
					String month = date.substring(4,6);
					String day = date.substring(6,8);
					ContentValues cv = new ContentValues();
					cv.put("service_id", serviceId);
					cv.put("calendar_date", year+"-"+month+"-"+day);
					cv.put("exception_type", exceptionType);
					values.add(cv);
				}
				return values;
			}
		});
			
		
		load(manager, "stops", new ContentValuesProvider() {

			@Override
			public List<ContentValues> getContentValues(CSVReader reader)
					throws IOException {
				List<ContentValues> values = new ArrayList<ContentValues>();
				String[] nextLine;
				while((nextLine = reader.readNext())!=null) {
					int id = Integer.parseInt(nextLine[0]);
					String name = nextLine[1];
					String desc = nextLine[2];
					Double lat = Double.parseDouble(nextLine[3]);
					Double lng = Double.parseDouble(nextLine[4]);
					int zoneId = Integer.parseInt(nextLine[5]);
					ContentValues cv = new ContentValues();
					cv.put("id", id);
					cv.put("name", name);
					cv.put("desc", desc);
					cv.put("lat",lat);
					cv.put("lon",lng);
					cv.put("zone_id",zoneId);
					values.add(cv);
				}
				return values;
			}
		});
		
		load(manager, "trips", new ContentValuesProvider() {

			@Override
			public List<ContentValues> getContentValues(CSVReader reader)
					throws IOException {
				List<ContentValues> values = new ArrayList<ContentValues>();
				String[] nextLine;
				while((nextLine = reader.readNext())!=null) {
					int routeId = Integer.parseInt(nextLine[0]);
					// route_id, service_id, trip_id, trip_headsign, direction_id, block_id
					int serviceId = Integer.parseInt(nextLine[1]);
					int id = Integer.parseInt(nextLine[2]);
					String headsign = nextLine[3];
					int direction = Integer.parseInt(nextLine[4]);
					String blockId = nextLine[5];
					ContentValues cv = new ContentValues();
					cv.put("route_id",routeId);
					cv.put("service_id", serviceId);
					cv.put("id", id);
					cv.put("direction", direction);
					cv.put("headsign", headsign);
					cv.put("block_id", blockId);
					values.add(cv);
				}
				return values;
			}
		});	
		

		load(manager, "routes", new ContentValuesProvider() {

			@Override
			public List<ContentValues> getContentValues(CSVReader reader)
					throws IOException {
				List<ContentValues> values = new ArrayList<ContentValues>();
				String[] nextLine;
				while((nextLine=reader.readNext())!=null) {
					int id = Integer.parseInt(nextLine[0]);
					int agencyId = Integer.parseInt(nextLine[1]);
					String shortName = nextLine[2];
					String longName = nextLine[3];
					int routeType = Integer.parseInt(nextLine[4]);
					ContentValues cv = new ContentValues();
					cv.put("id", id);
					cv.put("agency_id",agencyId);
					cv.put("short_name",shortName);
					cv.put("long_name",longName);
					cv.put("route_type",routeType);
					values.add(cv);
				}
				return values;
			} 
			
		});
	}

	/** @see SQLiteOpenHelper#onUpgrade(SQLiteDatabase, int, int) */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists agency");
		db.execSQL("drop table if exists routes");
		db.execSQL("drop table if exists stop_times");
		db.execSQL("drop table if exists stops");
		db.execSQL("drop table if exists trips");
		db.execSQL("drop table if exists calendar");
		db.execSQL("drop table if exists calendar_dates");
		onCreate(db);
	}

	/** Creates the db schema */
	private void createSchema(TransactionManager tm) {
		tm.exec(new Transactional(){
			@Override
			public void work(SQLiteDatabase db) {
				String[] creates = new String[] {
						"create table if not exists trips(id int primary key, route_id int, service_id int, headsign varchar(255), direction int, block_id varchar(255))",
						"create table if not exists stops(id int primary key, name varchar(255), desc varchar(255), lat real, lon real, zone_id)",
						"create table if not exists stop_times(trip_id int, arrival int, departure int, stop_id int, sequence int, pickup_type int, drop_off_type int)",
						"create table if not exists routes(id int primary key, agency_id int, short_name varchar(255), long_name varchar(255), route_type int)",
						"create table if not exists calendar(service_id int, monday int, tuesday int, wednesday int, thursday int, friday int, saturday int, sunday int, start date, end date)",
						//agency_id,agency_name,agency_url,agency_timezone
						"create table if not exists calendar_dates(service_id int, calendar_date int, exception_type int)",
						"create table if not exists agency(id int primary key, name varchar(255), url varchar(255))"
					};
				for(String str : creates) {
					db.execSQL(str);
				}
			}
		});
	}
	
	/**
	 * Loads a txt file based on the name of a table, extracts ContentValues from valuesProvider and inserts data into db
	 * @param tm
	 * @param tableName
	 * @param valuesProvider
	 */
	private void load(TransactionManager tm, final String tableName, final ContentValuesProvider valuesProvider) {
		tm.exec(new Transactional(){
			@Override
			public void work(SQLiteDatabase db) {
				InputStream input = null;
				CSVReader reader = null;
				try {
					input = context.getAssets().open(tableName + ".txt");
					reader = new CSVReader(new InputStreamReader(input));
					reader.readNext(); // read in the header line
					List<ContentValues> values = valuesProvider.getContentValues(reader);
					for(ContentValues cv:values) {
						db.insert(tableName, null, cv);
					}
				} catch(Throwable t) {
					Log.e("Error loadding " + tableName, t.getMessage(), t);
					t.printStackTrace();
				} finally {
					try {
						input.close();
						reader.close();
					} catch(Throwable t) {
						Log.e("Error closing streams for " + tableName, t.getMessage(), t);
						t.printStackTrace();
					}
				}
			}
		});
	}
}