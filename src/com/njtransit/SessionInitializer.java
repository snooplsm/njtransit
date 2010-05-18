package com.njtransit;

import java.util.List;

import android.os.AsyncTask;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;

/** Initializes the current user's session */
public class SessionInitializer {
	public static void exec(final NJTransitDBAdapter db, final Session session, final InitializationListener l) {
		new AsyncTask<Void, Void, List<Station>>() {

			@Override
			protected List<Station> doInBackground(Void... params) {
				db.open();
				session.setStations(db.getAllStations());
				return session.findClosestStations(null, 6);
			}
			
			@Override
			protected void onPostExecute(List<Station> closestedStations) {
				l.initialized(closestedStations);
		    }
			
		}.execute();
	}
}