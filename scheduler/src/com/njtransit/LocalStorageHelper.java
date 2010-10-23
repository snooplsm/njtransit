package com.njtransit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalStorageHelper extends SQLiteOpenHelper {
	
	public LocalStorageHelper(Context context) {
		super(context, "local_storage", null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		version1(db);	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion<2) {
			version2(db);
		}
	}
	
	@Override
	public synchronized void close() {
		super.close();
	}

	private void version2(SQLiteDatabase db) {
		db.execSQL("create table preferences (name varchar(25) unique, value varchar(100))");
	}
	
	private void version1(SQLiteDatabase db) {
		db.execSQL("create table trip_summary (id integer primary key autoincrement, station_depart int, station_arrive int, created int, total int default 0)");
		db.execSQL("create table trip_history (trip_summary int, created int)");
	}
}
