package com.njtransit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class NJTransitDBHelper extends SQLiteOpenHelper {

	private AssetManager assets;

	private SQLiteDatabase db;

	private Context ctx;

	public NJTransitDBHelper(Context context) {
		super(context, "njtransit", null, 1);
		this.ctx = context;
		this.assets = context.getAssets();
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
		if (!checkDataBase(at)) {
			try {
				copyDataBase(at);
			} catch (IOException e) {
				throw new RuntimeException("Error copying database", e);
			}
		}
	}

	private void copyDataBase(String at) throws IOException {
		List<String> partions = new ArrayList<String>();
		final String[] files = assets.list("");
		for (String f : files) {
			if (f.startsWith("njtransit.sqlite_")) {
				partions.add(f);
			}
		}
		Collections.sort(partions);
		OutputStream out = null;
		try {
			File file = new File(at);
			File parent = file.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			out = new FileOutputStream(at);
			byte[] buffer = new byte[1024];
			for (String partition : partions) {
				final InputStream in = assets.open(partition);
				while (in.read(buffer) > 0) {
					out.write(buffer);
				}
				in.close();
			}
			SharedPreferences preferences = ctx.getSharedPreferences(
					"properties", Context.MODE_PRIVATE);
			Editor e = preferences.edit().putInt("last-version", getVersion(ctx));
			e.commit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

	public void openDataBase(String at) throws SQLException {
		db = SQLiteDatabase.openDatabase(at, null,
				SQLiteDatabase.OPEN_READWRITE);
	}

	@Override
	public synchronized void close() {
		if (db != null)
			db.close();
		super.close();
	}

	private static int getVersion(Context ctx) {

		try {
			return ctx.getPackageManager().getPackageInfo(
					Root.class.getPackage().getName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e);
		}

	}

	private boolean checkDataBase(String at) {
		SQLiteDatabase checkDB = null;
		try {
			SharedPreferences preferences = ctx.getSharedPreferences(
					"properties", Context.MODE_PRIVATE);
			int version = getVersion(ctx);
			int lastVersion = preferences.getInt("last-version", -1);
			if(lastVersion<version) {
				File file = ctx.getDatabasePath(at);
				if(file.exists()) {
					file.delete();
				}
			}
			//Editor e = preferences.edit().putInt("last-version", version);
			checkDB = SQLiteDatabase.openDatabase(at, null,
					SQLiteDatabase.OPEN_READWRITE);
		} catch (SQLiteException e) {
			// database does't exist yet. if not, that's okay. we will create
			// one
		} finally {
			if (checkDB != null) {
				checkDB.close();
			}
		}
		return checkDB != null;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}