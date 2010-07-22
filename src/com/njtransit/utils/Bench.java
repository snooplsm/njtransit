package com.njtransit.utils;

import android.util.Log;

/** basic benchmarking utility */
public class Bench {
	public static interface Fn<R> {
		 R  apply();
	}
	
	public static interface Reporter {
		void report(String subject, long time);
	}
	
	public static final Reporter DEFAULT_REPORTER = new Reporter() {
		public void report(String subject, long time) {
			Log.w("benchmarks", subject + "took " + time);
		}
	};
	
	public static <R> R time(String subject, Fn<R> f, Reporter reporter) {
		long start = System.currentTimeMillis();
		R result = f.apply();
		reporter.report(subject, System.currentTimeMillis() - start);
		return result;
	}
	
	public static <R> R time(String subject, Fn<R> f) {
		return time(subject, f, DEFAULT_REPORTER);
	}
}