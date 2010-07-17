package com.njtransit;

import android.app.TabActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;

public class StopListHome extends TabActivity {

	private Session session = Session.get();
	
	private StopImpl stopTimes;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stop_list_home);
		
		((TextView) findViewById(R.id.title)).setText(renderTitle(session.getDepartureStation(), session.getArrivalStation()));
		
		final String defaultTabTxt = "Default";
		TabHost tabHost =  getTabHost();
		
		tabHost.addTab(tabHost.newTabSpec(defaultTabTxt).setIndicator(defaultTabTxt).setContent(new TabContentFactory() {

			@Override
			public View createTabContent(String arg) {
				if(defaultTabTxt.equals(arg)) {
					if(stopTimes == null) {
						stopTimes = (StopImpl)getLayoutInflater().inflate(R.layout.stop_impl, null);
					}
					return stopTimes;
				}			
				return null;
			}
		}));	
		tabHost.setCurrentTab(0);	
	}
	
	protected void onPause() {
		super.onPause();
		if(stopTimes!=null) {
			stopTimes.onPause();
		}
	}
	
	protected void onResume() {
		super.onResume();
		if(stopTimes!=null) {
			stopTimes.onResume();
		}
	}
	
	private String renderTitle(Station departing, Station arriving) {
		return String.format("%s > %s", departing, arriving);
	}
}