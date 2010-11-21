package com.njtransit.utils;

import java.util.Iterator;

import android.database.Cursor;

public class CursorIterator implements Iterator<Cursor> {
	private Cursor cursor;
	private int i = 0;
	
	public CursorIterator(Cursor c) {
		this.cursor = c;
		cursor.moveToFirst();
	}

	@Override
	public boolean hasNext() {
		return i < cursor.getCount();
	}

	@Override
	public Cursor next() {
		cursor.moveToNext();
		i++;
		return cursor;
	}

	@Override
	public void remove() {
	}
	
	public void close() {
		cursor.close();
	}
}