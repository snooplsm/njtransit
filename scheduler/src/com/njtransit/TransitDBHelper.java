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
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.njtransit.DatabaseAdapter.InstallDatabaseMeter;

public class TransitDBHelper extends SQLiteOpenHelper {

	private AssetManager assets;

	private SQLiteDatabase db;

	private Context ctx;

	private InstallDatabaseMeter installMeter;

	public TransitDBHelper(Context context, InstallDatabaseMeter installMeter) {
		super(context, "data_base", null, 1);
		this.ctx = context;
		this.assets = context.getAssets();
		this.installMeter = installMeter;
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
			// ProgressDialog d = ProgressDialog.show(ctx, "Installing...",
			// "Unpackaging schedules database.");
			try {
				copyDataBase(at);
			} catch (IOException e) {
				throw new RuntimeException("Error copying database", e);
			} finally {
				// d.cancel();
			}
		}
	}

	private void copyDataBase(String at) throws IOException {
		long start = System.currentTimeMillis();
		try {
			installMeter.onBeforeCopy();
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "onBeforeCopy Exception", e);
		}
		List<String> partions = new ArrayList<String>();
		final String[] files = assets.list("database");
		for (String f : files) {
			if (f.startsWith("database.sqlite_")) {
				partions.add(f);
			}
		}
		Collections.sort(partions);
		long totalSize = partions.size()*51200;
		
		try {
			installMeter.onSizeToBeCopiedCalculated(totalSize);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(),
					"onSizeToBeCopiedCalculated Exception", e);
		}
		OutputStream out = null;
		try {
			File file = ctx.getDatabasePath(at);
			File parent = file.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			out = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			long totalBytesCopied = 0;
			for (String partition : partions) {
				final InputStream in = assets.open("database/" + partition);
				int read;
				while ((read = in.read(buffer)) > 0) {
					out.write(buffer);
					totalBytesCopied += read;
				}
				in.close();
				try {
					float percent = totalBytesCopied / (float)totalSize;
					percent = Math.min(1, percent);
					installMeter.onPercentCopied(totalSize, percent,
							totalBytesCopied);
					Thread.sleep(100);
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(),
							"onPercentCopied Exception", e);
				}
			}
			Root.deleteScheduleDates(ctx);
			Root.saveDatabaseVersion(ctx, Root.getVersion(ctx));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
		long end = System.currentTimeMillis();
		Root.saveCopyDatabaseDuration(ctx, end - start);
		try {
			installMeter.onFinishedCopying();
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "onFinishedCopying exception", e);
		}
		Log.d(getClass().getSimpleName(), "copyDatabase(...) took "
				+ Root.getCopyDatabaseDuration(ctx) + "ms");
	}

	public void openDataBase(String at) throws SQLException {
		if (db != null && db.isOpen()) {

		} else {
			db = SQLiteDatabase.openDatabase(ctx.getDatabasePath(at)
					.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
		}
	}

	@Override
	public synchronized void close() {
		if (db != null)
			db.close();
		super.close();
	}

	private boolean checkDataBase(String at) {
		SQLiteDatabase checkDB = null;
		try {
			File file = ctx.getDatabasePath(at);
			int version = Root.getVersion(ctx);
			int lastVersion = Root.getDatabaseVersion(ctx);
			if (lastVersion < version) {
				if (file.exists()) {
					file.delete();
				}
			}
			// Editor e = preferences.edit().putInt("last-version", version);
			checkDB = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			// database does't exist yet. if not, that's okay. we will create
			// one
		} finally {
			if (checkDB != null) {
				this.db = checkDB;
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