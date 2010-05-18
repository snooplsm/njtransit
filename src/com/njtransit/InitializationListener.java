package com.njtransit;

import java.util.Map;

import com.njtransit.domain.Station;

/** Listens for initialization of session */
public interface InitializationListener {
	/** A session will be initialized with a list of closest stations */
	void initialized(Map<Station, Double> closestStations);
}