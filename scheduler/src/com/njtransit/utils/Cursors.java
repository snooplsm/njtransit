package com.njtransit.utils;

import java.util.Iterator;

import android.database.Cursor;

/**
 * Cursor utility functions
 * 
 * Cursors.foreach(c, new Fn {
 *   public void next(Cursor c) {
 *     // do business
 *   }
 * });
 * 
 * @author dtangren
 */
public class Cursors {
	
	public static interface Fn {
		void next(Cursor c);
	}
	
	public static void foreach(Cursor c, Fn f) {
		try {
			Iterator<Cursor> i = new CursorIterator(c);
			while(i.hasNext()) {
				f.next(i.next());
			}
		} finally {
			c.close();
		}
	}
	
	private Cursors() { }
}