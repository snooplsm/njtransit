package com.njtransit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalStorageHelper extends SQLiteOpenHelper {

	
	public LocalStorageHelper(Context context) {
		super(context, "local_storage", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		version1(db);	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	
	@Override
	public synchronized void close() {
		super.close();
	}

	private void version1(SQLiteDatabase db) {
		db.execSQL("create table trip_summary (id integer primary key autoincrement, station_depart int, station_arrive int, created int, total int default 0)");
		db.execSQL("create table trip_history (trip_summary int primary key, created int)");
	}

}
