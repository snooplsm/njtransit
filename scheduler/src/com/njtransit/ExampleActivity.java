package com.njtransit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.njtransit.R;
import com.njtransit.SchedulerActivity;
import com.njtransit.SchedulerApplication;
import com.njtransit.Session;
import com.njtransit.StationListActivity;
import com.njtransit.StopActivity;
import com.njtransit.domain.Station;

public class ExampleActivity extends SchedulerActivity {
	
	public static int DEPARTURE_REQUEST_CODE = 1, ARRIVAL_REQUEST_CODE = 2;
	
	private TextView departureText, arrivalText;
	private View getSchedule;
	private ImageView getScheduleImage;
	
	private Session session;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==RESULT_OK) {
			Station station = getSchedulerContext().getStation(data.getIntExtra("stationId",-1));
			if(requestCode==DEPARTURE_REQUEST_CODE) {
				getSchedulerContext().setDepartureStation(station);
				departureText.setText(station.getName());
			} else {
				getSchedulerContext().setArrivalStation(station);
				arrivalText.setText(station.getName());
			}
		}	
		if(getSchedulerContext().getDepartureStation()!=null && getSchedulerContext().getArrivalStation()!=null) {
			getSchedule.setEnabled(true);
			getScheduleImage.setVisibility(View.VISIBLE);
		} else {
			getScheduleImage.setVisibility(View.INVISIBLE);
			getSchedule.setEnabled(false);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jumper);
		
		final SchedulerApplication app = getSchedulerContext();

		RelativeLayout btn = (RelativeLayout) findViewById(R.id.departure);
		departureText = (TextView)findViewById(R.id.departureText);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {	
				Intent intent = new Intent(getApplicationContext(), StationListActivity.class);
				startActivityForResult(intent, DEPARTURE_REQUEST_CODE);
			}
		});
		RelativeLayout arrival = (RelativeLayout) findViewById(R.id.arrival);
		arrivalText = (TextView)findViewById(R.id.arrivalText);
		arrival.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), StationListActivity.class);
				startActivityForResult(intent, ARRIVAL_REQUEST_CODE);
			}
		});
				
		getSchedule = (RelativeLayout)findViewById(R.id.get_schedule);
		getSchedule.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(), StopActivity.class);
				startActivity(intent);
			}
			
		});
		getScheduleImage = (ImageView)findViewById(R.id.getScheduleChevron);
	}
	
}