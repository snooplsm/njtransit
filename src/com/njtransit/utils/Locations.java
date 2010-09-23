package com.njtransit.utils;

import java.text.DecimalFormat;

import android.location.Location;

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
		public enum Units {
			METRIC, ENGLISH
		}
		private LatLong from, to;
		
		public RelativeDistance(Location from) {
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
			return  "~ " + (dist/1000 > 0 ? (format(dist/1000) + " kilometers") : (format(dist) + " meters")) + " away";
		}
		
		private String format(float f) {
			return new DecimalFormat("#0.0").format(f);
		}
	}	
	
	public static RelativeDistance relativeDistanceFrom(Location l) {
		return new RelativeDistance(l);
	}
}
