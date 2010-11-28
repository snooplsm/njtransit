package com.njtransit;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.njtransit.domain.Station;
import com.scheduler.njtransit.R;

public class MainActivity extends SchedulerActivity {

	public static int DEPARTURE_REQUEST_CODE = 1, ARRIVAL_REQUEST_CODE = 2;

	private TextView departureText, arrivalText;
	private View getSchedule;
	private ImageView getScheduleImage;
	private DatePickerDialog datePickDialog;
	private boolean needDatePickDialog;
	private Calendar minDate;
	private Calendar maxDate;
	private TextView departureDateText;

	private SimpleDateFormat DF = new SimpleDateFormat("M/d/yy");

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Station station = getSchedulerContext().getStation(
					data.getIntExtra("stationId", -1));
			if (requestCode == DEPARTURE_REQUEST_CODE) {
				getSchedulerContext().setDepartureStation(station);
				tracker.trackEvent("station-depart", "selected", station
						.getName(), station.getId());
			} else {
				getSchedulerContext().setArrivalStation(station);
				tracker.trackEvent("station-arrive", "selected", station
						.getName(), station.getId());
			}
		}
		Station departure = getSchedulerContext().getDepartureStation();
		Station arrival = getSchedulerContext().getArrivalStation();
		if (departure != null) {
			departureText.setText(departure.getName());
		}
		if (arrival != null) {
			arrivalText.setText(arrival.getName());
		}
		if (departure != null && arrival != null) {
			getSchedule.setEnabled(true);
			getScheduleImage.setVisibility(View.VISIBLE);

		} else {
			getScheduleImage.setVisibility(View.INVISIBLE);
			getSchedule.setEnabled(false);
		}
		Calendar now = Calendar.getInstance();
		Calendar dept = getSchedulerContext().getDepartureDate();
		if(dept!=null) {
			int year = dept.get(Calendar.YEAR);
			int monthOfYear = dept.get(Calendar.MONTH);
			int dayOfMonth = dept.get(Calendar.DAY_OF_MONTH);
			if(!(now.get(Calendar.YEAR)==year && now.get(Calendar.MONTH)==monthOfYear && now.get(Calendar.DAY_OF_MONTH)==dayOfMonth)) {
				departureDateText.setVisibility(View.VISIBLE);
				departureDateText.setText(DF.format(dept.getTime()));
			} else {
				departureDateText.setVisibility(View.GONE);
			}
		} else {
			departureDateText.setVisibility(View.GONE);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jumper);

		RelativeLayout btn = (RelativeLayout) findViewById(R.id.departure);
		departureText = (TextView) findViewById(R.id.departureText);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				tracker.trackEvent("departure-clicked", "button", "clicked", 0);
				Intent intent = new Intent(getApplicationContext(),
						StationListActivity.class);
				startActivityForResult(intent, DEPARTURE_REQUEST_CODE);
			}
		});
		RelativeLayout arrival = (RelativeLayout) findViewById(R.id.arrival);
		arrivalText = (TextView) findViewById(R.id.arrivalText);
		arrival.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				tracker.trackEvent("arrival-clicked", "button", "clicked", 1);
				Intent intent = new Intent(getApplicationContext(),
						StationListActivity.class);
				startActivityForResult(intent, ARRIVAL_REQUEST_CODE);
			}
		});

		getSchedule = (RelativeLayout) findViewById(R.id.get_schedule);
		getSchedule.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				SchedulerApplication sc = getSchedulerContext();
				tracker.trackEvent("get-schedule-clicked", "button", sc
						.getDepartureStation().getName()
						+ " to " + sc.getArrivalStation().getName(), 2);
				Intent intent = new Intent(getApplicationContext(),
						StopActivity.class);
				startActivity(intent);
			}

		});
		getScheduleImage = (ImageView) findViewById(R.id.getScheduleChevron);
		departureDateText = (TextView)findViewById(R.id.departureDate);
		tracker.trackPageView("/"+getClass().getSimpleName());
		minDate = Calendar.getInstance();
		maxDate = Calendar.getInstance();
		minDate.setTimeInMillis(Root
				.getScheduleStartDate(getApplicationContext()));
		maxDate.setTimeInMillis(Root
				.getScheduleEndDate(getApplicationContext()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		tracker.trackEvent("menu-click", "MenuButton", "click", 1);
		MenuItem cal = menu.add(Menu.NONE, 1, Menu.FIRST,
				getString(R.string.date_pick));
		MenuItem reverse = menu.add(Menu.NONE,2,2,getString(R.string.reverse));
		reverse.setIcon(R.drawable.signpost);
		cal.setIcon(R.drawable.clock);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 1) {
			tracker.trackEvent("menu-click", "MenuButton", item.getTitle()
					.toString(), item.getItemId());
			showDatePicker();
		}
		if(item.getItemId()==2) {
			tracker.trackEvent("menu-click","MenuButton", item.getTitle().toString(), item.getItemId());
			getSchedulerContext().reverseTrip();
			onActivityResult(-1, Activity.RESULT_CANCELED, null);
		}
		return super.onOptionsItemSelected(item);
	}

	private void showDatePicker() {
		final Calendar cal;
		Calendar departureDate = getSchedulerContext().getDepartureDate();
		if (departureDate != null) {
			cal = departureDate;
		} else {
			cal = Calendar.getInstance();
		}
		datePickDialog = new DatePickerDialog(this, new OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, monthOfYear);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);	
				getSchedulerContext().setDepartureDate(cal);				
				onActivityResult(-1, Activity.RESULT_CANCELED, null);
				//if(now.)
				
				needDatePickDialog = false;
			}

		}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
				.get(Calendar.DAY_OF_MONTH)) {

			@Override
			public void onDateChanged(DatePicker view, int year, int month,
					int day) {
				Calendar newDate = Calendar.getInstance();
				newDate.set(Calendar.YEAR, year);
				newDate.set(Calendar.MONTH, month);
				newDate.set(Calendar.DAY_OF_MONTH, day);
				
				if (newDate.getTimeInMillis()<minDate.getTimeInMillis()) {
					Toast.makeText(getApplicationContext(), "Date was out of schedule range, reset.", Toast.LENGTH_LONG).show();
					updateDate(minDate.get(Calendar.YEAR), minDate.get(Calendar.MONDAY), minDate.get(Calendar.DAY_OF_MONTH));
				} else if (newDate.getTimeInMillis()>maxDate.getTimeInMillis()) {
					Toast.makeText(getApplicationContext(), "Date was out of schedule range, reset.", Toast.LENGTH_LONG).show();
					updateDate(maxDate.get(Calendar.YEAR), maxDate.get(Calendar.MONDAY), maxDate.get(Calendar.DAY_OF_MONTH));
				}
			}

		};
		datePickDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				needDatePickDialog = false;
			}

		});
		needDatePickDialog = true;
		datePickDialog.show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (datePickDialog != null) {
			datePickDialog.dismiss();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (needDatePickDialog) {
			datePickDialog.show();
		}
	}

	@Override
	public void onAttachedToWindow() {
		onActivityResult(-1, Activity.RESULT_CANCELED, null);
		super.onAttachedToWindow();
	}

}