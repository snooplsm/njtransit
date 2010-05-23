package com.njtransit;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NJTransitDBHelper extends SQLiteOpenHelper {

	private Context context;

	private SQLiteDatabase db;

	public NJTransitDBHelper(Context context) {
		super(context, "njtransit", null, 1);
		this.context = context;
	}
	
	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		return db;
	}
	
	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		return db;
	}

	public void createDataBase(String at) throws IOException {
		boolean dbExist = checkDataBase(at);
		if (dbExist) {
			// do nothing - database already exist
		} else {
			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are goina be able to overwrite that
			// database with our database.
			super.getReadableDatabase();
			try {
				copyDataBase(at);
			} catch (IOException e) {
				throw new RuntimeException("Error copying database", e);
			}
		}
	}

	private void copyDataBase(String at) throws IOException {
		long start = System.currentTimeMillis();
		
		List<String> partions = new ArrayList<String>();
		final String[] assets = context.getAssets().list("");
		for(String a : assets) {
			if(a.startsWith("njtransit.sqlite.partition")) {
				partions.add(a);
			}
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(at);
			byte[] buffer = new byte[1024];
			for(String partition : partions) {
				final InputStream in = context.getAssets().open(partition);
				while(in.read(buffer) > 0) {
					out.write(buffer);
				}
				in.close();
			}
		} finally {
			if(out != null) {
				out.flush();
				out.close();
			}
		}
		
		Log.i(getClass().getSimpleName(), "Slurped in db in "+(System.currentTimeMillis()-start)+"ms");
	}

	public void openDataBase(String at) throws SQLException {
		db = SQLiteDatabase.openDatabase(at, null,
				SQLiteDatabase.OPEN_READWRITE);
	}

	public synchronized void close() {
		if (db != null) db.close();
		super.close();
	}

	private boolean checkDataBase(String at) {
		SQLiteDatabase checkDB = null;
		try {
			checkDB = SQLiteDatabase.openDatabase(at, null,
					SQLiteDatabase.OPEN_READWRITE);
		} catch (SQLiteException e) {
			// database does't exist yet. if not, that's okay. we will create one
		} finally {
			if (checkDB != null) {
				checkDB.close();
			}
		}		
		return checkDB != null ? true : false;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}