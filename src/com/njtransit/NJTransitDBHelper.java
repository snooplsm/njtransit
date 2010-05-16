package com.njtransit;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import au.com.bytecode.opencsv.CSVReader;

public class NJTransitDBHelper extends SQLiteOpenHelper {

	private Context context;
	
	public NJTransitDBHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		this.context = context;
	}
	
	

	@Override
	public void onCreate(SQLiteDatabase db) {
		String[] create = new String[] {
				"create table if not exists trips(id int primary key, route_id int, service_id int, headsign varchar(255), direction int, block_id varchar(255))",
				"create table if not exists stops(id int primary key, name varchar(255), desc varchar(255), lat real, lon real, zone_id)",
				"create table if not exists stop_times(trip_id int, arrival int, departure int, stop_id int, sequence int, pickup_type int, drop_off_type int)",
				"create table if not exists routes(id int primary key, agency_id int, short_name varchar(255), long_name varchar(255), route_type int)",
				"create table if not exists calendar(service_id int, monday int, tuesday int, wednesday int, thursday int, friday int, saturday int, sunday int, start date, end date)",
				//agency_id,agency_name,agency_url,agency_timezone
				"create table if not exists calendar_dates(service_id int, calendar_date int, exception_type int)",
				"create table if not exists agency(id int primary key, name varchar(255), url varchar(255))"
			};
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy kk:mm:ss");
		db.beginTransaction();
		for(String str : create) {
			db.execSQL(str);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		
		InputStream input = null;
		try {
			input = context.getAssets().open("stop_times.txt");
			CSVReader reader = new CSVReader(new InputStreamReader(input));
			reader.readNext();
			String[] nextLine;
			db.beginTransaction();
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
				db.insert("stop_times", null, cv);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			input = context.getAssets().open("agency.txt");
			reader = new CSVReader(new InputStreamReader(input));			
			reader.readNext();
			db.beginTransaction();
			while((nextLine = reader.readNext())!=null) {
				int id = Integer.parseInt(nextLine[0]);
				String name = nextLine[1];
				String url = nextLine[2];
				ContentValues values = new ContentValues();
				values.put("id", id);
				values.put("url", url);
				values.put("name", name);
				db.insert("agency", null, values);				
			}	
			try {
				reader.close();
				input.close();
			} catch (Exception e) {
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			
			input = context.getAssets().open("calendar.txt");
			reader = new CSVReader(new InputStreamReader(input));
			reader.readNext();
			db.beginTransaction();
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
				db.insert("calendar", null, cv);
			}
			try {
				reader.close();
				input.close();
			} catch (Exception e) {}
			db.setTransactionSuccessful();
			db.endTransaction();
			input = context.getAssets().open("calendar_dates.txt");
			reader = new CSVReader(new InputStreamReader(input));
			reader.readNext();
			db.beginTransaction();
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
				db.insert("calendar_dates", null, cv);
			}
			try {
				reader.close();
				input.close();
			} catch (Exception e) {}
			db.setTransactionSuccessful();
			db.endTransaction();
			input = context.getAssets().open("stops.txt");
			reader = new CSVReader(new InputStreamReader(input));
			reader.readNext();
			db.beginTransaction();
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
				db.insert("stops", null, cv);
			}
			try {
				reader.close();
				input.close();
			} catch (Exception e) {}
			db.setTransactionSuccessful();
			db.endTransaction();
			input = context.getAssets().open("trips.txt");
			reader = new CSVReader(new InputStreamReader(input));
			reader.readNext();
			db.beginTransaction();
			while((nextLine = reader.readNext())!=null) {
				int routeId = Integer.parseInt(nextLine[0]);
				//route_id,service_id,trip_id,trip_headsign,direction_id,block_id
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
				db.insert("trips", null, cv);
			}
			try {
				reader.close();
				input.close();
			} catch (Exception e) {}
			db.setTransactionSuccessful();
			db.endTransaction();
			input = context.getAssets().open("routes.txt");
			reader = new CSVReader(new InputStreamReader(input));
			reader.readNext();
			db.beginTransaction();			
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
				db.insert("routes", null, cv);
			}
			try {
				reader.close();
				input.close();
			} catch (Exception e) {}
			db.setTransactionSuccessful();
			db.endTransaction();			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				input.close();
				
			} catch (Exception e) {
				
			}
		}
	}

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

}
