package com.njtransit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.njtransit.domain.Stop;

public class StopTimeRow extends LinearLayout {

	private TextView arrival, departure;
	private Stop stop;
	
	private static String format(int time) {
		int hour = time / 3600000;
		final String hourS;
		final boolean am;
		if(hour==0) {
			am = true;
			hourS = "12";
		} else {
			am = hour/12>1;
			hourS = ""+(hour%12==0 ? 12 : hour%12);
		}
		return hourS;
	}
	
	public StopTimeRow(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onFinishInflate() {
		 arrival = (TextView)findViewById(R.id.arrival);
		 departure = (TextView)findViewById(R.id.departure);		 
	}
	
	public void setStop(Stop stop) {
		this.stop = stop;
		arrival.setText(format(stop.getArrive()));
	}

}
