package com.njtransit;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;

public class Root {
	public static int getVersion(Context ctx) {

		try {
			return ctx.getPackageManager().getPackageInfo(
					Root.class.getPackage().getName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e);
		}

	}

	private static SharedPreferences getProperties(Context ctx) {
		SharedPreferences preferences = ctx.getSharedPreferences("properties",
				Context.MODE_PRIVATE);
		return preferences;
	}

	public static int getDatabaseVersion(Context ctx) {
		SharedPreferences prefs = getProperties(ctx);
		return prefs.getInt("database-version", -1);
	}

	public static String getMoveOnRestart(Context ctx) {
		SharedPreferences prefs = getProperties(ctx);
		return prefs.getString("move-db-on-restart", null);
	}

	public static void setMoveOnRestart(Context ctx, String value) {
		SharedPreferences prefs = getProperties(ctx);
		Editor editor = prefs.edit();
		if (value == null) {
			editor.remove("move-db-on-restart");
		} else {
			editor.putString("move-db-on-restart", value);
		}
		editor.commit();
	}

	public static Date getLastChecked(Context ctx) {
		SharedPreferences prefs = getProperties(ctx);
		Long lng = prefs.getLong("last-checked", -1);
		if (lng.equals(-1L)) {
			return null;
		}
		return new Date(lng);
	}

	public static void saveDatabaseVersion(Context ctx, int version) {
		SharedPreferences prefs = getProperties(ctx);
		Editor e = prefs.edit().putInt("database-version", version);
		e.commit();
	}

	public static void setLastChecked(Context ctx, long currentTimeMillis) {
		SharedPreferences prefs = getProperties(ctx);
		Editor e = prefs.edit().putLong("last-checked", currentTimeMillis);
		e.commit();
	}

	public static void saveCopyDatabaseDuration(Context ctx, long duration) {
		SharedPreferences prefs = getProperties(ctx);
		Editor e = prefs.edit().putLong("copy-database-duration", duration);
		e.commit();
	}

	public static long getCopyDatabaseDuration(Context ctx) {
		SharedPreferences prefs = getProperties(ctx);
		return prefs.getLong("copy-database-duration", -1);
	}

	public static void saveGetStationsDuration(Context context, long duration) {
		SharedPreferences prefs = getProperties(context);
		Editor e = prefs.edit().putLong("get-stations-duration", duration);
		e.commit();
	}

	public static long getGetStationsDuration(Context ctx) {
		SharedPreferences prefs = getProperties(ctx);
		return prefs.getLong("get-stations-duration", -1);
	}
	
	public static long getScheduleEndDate(Context ctx) {
		SharedPreferences prefs = getProperties(ctx);
		return prefs.getLong("schedule-end-date", -1);
	}
	
	public static void saveScheduleEndDate(Context ctx, long end) {
		SharedPreferences prefs = getProperties(ctx);
		Editor e = prefs.edit().putLong("schedule-end-date", end);
		e.commit();
	}
}
