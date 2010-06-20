package com.njtransit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.njtransit.domain.Session;

public class StopImpl extends ListView {

	private Session session = Session.get();
	
//	private TextView headerText;
	
	public StopImpl(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	
}
