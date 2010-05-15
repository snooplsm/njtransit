package com.njtransit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class NJTransitDBAdapter {

	public NJTransitDBAdapter(Context context) {
		this.context = context;
	}
	
	private static final int VERSION = 1;
	
	private Context context;

	private NJTransitDBHelper helper;
	
	private SQLiteDatabase db;
	
	public NJTransitDBAdapter open() {
		helper = new NJTransitDBHelper(context, "njtransit", null, VERSION);
		//readable
		db = helper.getWritableDatabase();
		return this;
	}
	
	public List<Str>
	
}
