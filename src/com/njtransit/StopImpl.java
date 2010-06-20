package com.njtransit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.njtransit.domain.Session;

public class StopImpl extends LinearLayout {

	private Session session = Session.get();
	
//	private TextView headerText;
	
	public StopImpl(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		headerText = (TextView)findViewById(R.id.station_header);
//		
//		headerText.setText(session.getDepartureStation().getName() + " > " + session.getArrivalStation().getName());
		
		super.onLayout(changed, l, t, r, b);
	}
	

	

}
