package com.njtransit.utils;

import java.text.DecimalFormat;

import android.location.Location;

import com.njtransit.domain.Preferences.Unit;
import com.njtransit.domain.Station;

public class Locations {
	private static class LatLong {
		private double lat, lon;
		
		public LatLong(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}
		
		public double getLatitude() {
			return lat;
		}
		
		public double getLongitude() {
			return lon;
		}
	}
	
	public static class RelativeDistance {

		private LatLong from, to;
		private Unit unit;
		
		public RelativeDistance(Location from, Unit unit) {
			this.unit = unit;
			this.from = new LatLong(from.getLatitude(), from.getLongitude());
		}
		
		public RelativeDistance to(Double lat, Double lon) {
			to = new LatLong(lat, lon);
			return this;
		}
		
		public RelativeDistance to(Station s) {
			return to(s.getLatitude(), s.getLongitude());
		}
		
		public float get() {
			float[] results = new float[3];
			Location.distanceBetween(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude(), results);
			return results[0];
		}
		
		public String inWords() {
			float dist = get();
			float calc;
			if(unit==Unit.METRIC) {
				calc = dist/1000;
				return (calc > 0 ? (format(calc) + " kilometers") : (format(dist) + " meters"));
			}
			calc = (float)(dist/1609.344);
			return (calc > 0 ? (format(calc) + " miles") : (format(dist/.3048) + " feet"));
		}
		
		private String format(Number f) {
			return new DecimalFormat("#0.0").format(f);
		}
	}	
	
	public static RelativeDistance relativeDistanceFrom(Location l, Unit unit) {
		return new RelativeDistance(l, unit);
	}
}
