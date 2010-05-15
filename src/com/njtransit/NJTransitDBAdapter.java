package com.njtransit;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.njtransit.domain.Station;

public class NJTransitDBAdapter {

	public NJTransitDBAdapter(Context context) {
		this.context = context;
	}
	
	private static final int VERSION = 1;
	
	private Context context;

	private NJTransitDBHelper helper;
	
	private SQLiteDatabase db;
	
	private static String[] STATION_COLUMNS = new String[] {"id","name","lat","lon","zone_id"};
	
	public NJTransitDBAdapter open() {
		helper = new NJTransitDBHelper(context, "njtransit", null, VERSION);
		//readable
		db = helper.getWritableDatabase();
		return this;
	}
	
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
	
}
