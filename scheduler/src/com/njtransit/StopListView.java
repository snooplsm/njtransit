package com.njtransit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/** List of StopTimeRows */
public class StopListView extends ListView {

	public StopListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setSmoothScrollbarEnabled(true);
	}

}