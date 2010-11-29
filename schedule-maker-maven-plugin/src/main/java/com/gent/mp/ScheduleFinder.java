package com.gent.mp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import au.com.bytecode.opencsv.CSVReader;

public class ScheduleFinder {

	private int serviceIdPosition;
	private int datePosition;
	private int tripIdPosition;
	private int tripServiceIdPosition;
	private int stopTimeTripIdPosition;
	private int arrivalTimeIdPosition;
	private int stopIdPosition;
	private int stopNamePosition;
	private int stopLatPosition;
	private int stopLngPosition;
	
	private int departureTimeIdPosition;
	private int sequencePosition;
	private int stopTimesStopIdPosition;

	private File gtfsFolder;

	private static String CALENDAR_DATES = "calendar_dates.txt";
	private static String TRIPS = "trips.txt";
	private static String STOP_TIMES = "stop_times.txt";
	private static String STOPS = "stops.txt";
	private static String DATE = "date";
	private static String SERVICE_ID = "service_id";
	private static String TRIP_ID = "trip_id";
	private static String STOP_NAME = "stop_name";
	private static String STOP_LAT = "stop_lat";
	private static String STOP_LNG = "stop_lon";
	
	private static String ARRIVAL_TIME = "arrival_time";
	private static String DEPARTURE_TIME = "departure_time";
	private static String STOP_SEQUENCE = "stop_sequence";
	private static String STOP_ID = "stop_id";

	private static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	private static DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

	public ScheduleFinder(File gtfsFolder) {
		this.gtfsFolder = gtfsFolder;
		initialize();
	}

	private void initialize() {
		processCalendarDateHeader();
		processTripHeader();
		processStopTimesHeader();
		processStops();
	}

	private void processStops() {
		String[] header = readHeader(STOPS);
		for(int i = 0; i < header.length; i++) {
			if(STOP_ID.equals(header[i])) {
				stopIdPosition = i;
			}
			if(STOP_NAME.equals(header[i])) {
				stopNamePosition = i;
			}
			if(STOP_LAT.equals(header[i])) {
				stopLatPosition = i;
			}
			if(STOP_LNG.equals(header[i])) {
				stopLngPosition = i;
			}
		}
	}
	
	private void processCalendarDateHeader() {
		String[] header = readHeader(CALENDAR_DATES);
		for (int i = 0; i < header.length; i++) {
			if (DATE.equals(header[i])) {
				datePosition = i;
			}
			if (SERVICE_ID.equals(header[i])) {
				serviceIdPosition = i;
			}
		}
	}

	private void processTripHeader() {
		String[] header = readHeader(TRIPS);
		for (int i = 0; i < header.length; i++) {
			if (TRIP_ID.equals(header[i])) {
				tripIdPosition = i;
			}
			if (SERVICE_ID.equals(header[i])) {
				tripServiceIdPosition = i;
			}
		}
	}

	private void processStopTimesHeader() {
		String[] header = readHeader(STOP_TIMES);
		for (int i = 0; i < header.length; i++) {
			if(STOP_ID.equals(header[i])) {	
				stopTimesStopIdPosition = i;
			}
			if(TRIP_ID.equals(header[i])) {
				stopTimeTripIdPosition = i;
			}
			if(ARRIVAL_TIME.equals(header[i])) {
				arrivalTimeIdPosition = i;
			}
			if(DEPARTURE_TIME.equals(header[i])) {
				departureTimeIdPosition = i;
			}
			if(STOP_SEQUENCE.equals(header[i])) {
				sequencePosition = i;
			}
		}
	}

	private List<Trip> getTrips(CalendarDate calendarDate) {
		CSVReader reader = null;
		try {
			InputStream input = new FileInputStream(new File(gtfsFolder,
					TRIPS));
			reader = new CSVReader(new InputStreamReader(input));
			String[] line = reader.readNext();
			TreeSet<String> s = new TreeSet<String>();
			for (int k : calendarDate.getServiceIdToDate().keySet()) {
				s.add("" + k);
			}
			List<Trip> trips = new ArrayList<Trip>();
			while ((line = reader.readNext()) != null) {
				String serviceId = line[tripServiceIdPosition];
				if (s.contains(serviceId)) {
					String tripId = line[tripIdPosition];
					Trip trip = new Trip(Integer
							.parseInt(serviceId),Integer.parseInt(tripId));
					trips.add(trip);
				}
			}
			return trips;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private CalendarDate getCalendarDate(Date... dates) {
		TreeSet<String> s = new TreeSet<String>();
		for (Date date : dates) {
			s.add(DATE_FORMAT.format(date));
		}
		CSVReader reader = null;
		try {
			InputStream input = new FileInputStream(new File(gtfsFolder,
					CALENDAR_DATES));
			reader = new CSVReader(new InputStreamReader(input));
			CalendarDate calendarDate = null;
			String[] line = reader.readNext();
			while ((line = reader.readNext()) != null) {
				String date = line[datePosition];
				if (s.contains(date)) {
					Integer serviceId = Integer
							.parseInt(line[serviceIdPosition]);
					if (calendarDate == null) {
						Date calDate = DATE_FORMAT.parse(line[datePosition]);
						calendarDate = new CalendarDate(serviceId, calDate);
					} else {
						Date calDate = DATE_FORMAT.parse(line[datePosition]);
						calendarDate.getServiceIdToDate().put(serviceId,
								calDate);
					}
				}
			}
			return calendarDate;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private String[] readHeader(String file) {
		CSVReader reader = null;
		try {
			InputStream input = new FileInputStream(new File(gtfsFolder, file));
			reader = new CSVReader(new InputStreamReader(input));
			return reader.readNext();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public StopTimesResult getStops(String departingStationId, String arrivingStationId,
			Date... dates) {
		CalendarDate calendarDate = getCalendarDate(dates);
		List<Trip> trips = getTrips(calendarDate);
		List<Stop> stops = getStops(departingStationId, arrivingStationId, trips);
		return new StopTimesResult(trips,stops,calendarDate);
	}
	
	public List<Station> getStations() {
		CSVReader reader = null;
		try {
			InputStream input = new FileInputStream(new File(gtfsFolder,
					STOPS));
			reader = new CSVReader(new InputStreamReader(input));
			String[] line = reader.readNext();
			List<Station> stations = new ArrayList<Station>();
			while((line = reader.readNext())!=null) {
				String name = line[stopNamePosition];
				StringBuilder sb = new StringBuilder(name);
				char lastChar=' ';
				for(int i = 0; i < sb.length();i++) {
					char nowChar = sb.charAt(i);
					if(lastChar==' ' || lastChar=='/') {
						sb.setCharAt(i, Character.toUpperCase(nowChar));
					} else {
						sb.setCharAt(i, Character.toLowerCase(nowChar));
					}
					lastChar = nowChar;
				}
				String id = line[stopIdPosition];
				String lat = line[stopLatPosition];
				String lng = line[stopLngPosition];
				float latF=Float.NaN;
				float lngF=Float.NaN;
				if(lat.length()>0) {
					latF = Float.parseFloat(lat);
					lngF = Float.parseFloat(lng);
				}
				Station station = new Station(name,id,latF,lngF);
				stations.add(station);
			}
			return stations;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private List<Stop> getStops(final String departingStationId, String arrivingStationId,
			List<Trip> trips) {
		CSVReader reader = null;
		try {
			InputStream input = new FileInputStream(new File(gtfsFolder,
					STOP_TIMES));
			reader = new CSVReader(new InputStreamReader(input));
			String[] line = reader.readNext();
			TreeSet<String> tripIds = new TreeSet<String>();
			for(Trip t : trips) {
				tripIds.add(""+t.getTripId());
			}
			List<Stop> stops = new ArrayList<Stop>();
			while ((line = reader.readNext()) != null) {
				String tripId = line[stopTimeTripIdPosition];
				if(tripIds.contains(tripId)) {
					String stopId = line[stopTimesStopIdPosition];
					if(stopId.equals(departingStationId) || stopId.equals(arrivingStationId)) {
						Stop stop = new Stop();
						stop.setStopId(stopId);
						stop.setTripId(tripId);
						stop.setDeparture(TIME_FORMAT.parse(line[departureTimeIdPosition]));
						stop.setArrival(TIME_FORMAT.parse(line[arrivalTimeIdPosition]));
						stop.setSequence(Integer.parseInt(line[sequencePosition]));
						stops.add(stop);
					}
				}
			}
			Collections.sort(stops, new Comparator<Stop>() {

				@Override
				public int compare(Stop o1, Stop o2) {
					int c = o1.getTripId().compareTo(o2.getTripId());
					if(c==0) {
						if(o1.getStopId().equals(departingStationId)) {
							return -1;
						}
						return 1;
					}
					return c;
				}
				
			});
			
			List<Stop> properStops = new ArrayList<Stop>();
			
			for (int i = 0; i < stops.size(); i++) {
				if (i + 1 >= stops.size()) {
					break;
				}
				Stop a = stops.get(i);
				Stop b = stops.get(i + 1);
				if (a.getTripId().equals(b.getTripId())) {
					if (a.getSequence() < b.getSequence()) {
						Stop stop = new Stop();
						stop.setDeparture(a.getDeparture());
						stop.setArrival(b.getArrival());
						properStops.add(stop);
					}
				}
			}
			return properStops;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
