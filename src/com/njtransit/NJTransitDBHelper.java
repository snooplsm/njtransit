package com.njtransit;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.text.method.DateTimeKeyListener;
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
				"create table trips(id int primary key, route_id int, service_id int, headsign varchar(255), direction_id int, block_id varchar(255))",
				"create table stops(id int primary key, name varchar(255), desc varchar(255), lat real, lon real, zone_id)",
				"create table stop_times( trip_id int, arrival date, departure date, stop_id int, stop_sequence int, pickup_type int, drop_off_type int)",
				"create table routes(id int primary key, short_name varchar(255), long_name varchar(255), route_type int)",
				"create table calendar(service_id int, monday int, tuesday int, wednesday int, thursday int, friday int, saturday int, sunday int, start date, end date)",
				//agency_id,agency_name,agency_url,agency_timezone
				"create table calendar_dates(service_id int, calendar_date date, exception_type int)",
				"create table agency(id int primary key, name varchar(255), url varchar(255))"
			};
		
		db.beginTransaction();
		for(String str : create) {
			db.execSQL(str);
		}
		db.setVersion(1);
		db.endTransaction();
		
		InputStream input = null;
		try {
			input = context.getAssets().open("agency.txt");
			CSVReader reader = new CSVReader(new InputStreamReader(input));
			String[] nextLine;
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
			input = context.getAssets().open("calendar.txt");
			reader = new CSVReader(new InputStreamReader(input));
			reader.readNext();
			while((nextLine = reader.readNext())!=null) {
				int serviceId = Integer.parseInt(nextLine[0]);
				int monday = Integer.parseInt(nextLine[1]);
				int tuesday = Integer.parseInt(nextLine[2]);
				int wednesday = Integer.parseInt(nextLine[3]);
				int thursday = Integer.parseInt(nextLine[4]);
				int friday = Integer.parseInt(nextLine[5]);
				int saturday = Integer.parseInt(nextLine[6]);
				int sunday = Integer.parseInt(nextLine[7]);
				String start = nextLine[8];
				String end = nextLine[9];
				//Date d = new Date(
				
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			db.endTransaction();
			try {
				input.close();
			} catch (Exception e) {
				
			}
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
