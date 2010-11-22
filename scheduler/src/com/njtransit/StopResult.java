package com.njtransit;

import com.njtransit.domain.Stop;
import com.njtransit.model.StopsQueryResult;

public class StopResult {

	private StopsQueryResult stopQueryResult;
	
	private Stop closest;

	public StopsQueryResult getStopQueryResult() {
		return stopQueryResult;
	}

	public Stop getClosest() {
		return closest;
	}

	public StopResult(StopsQueryResult stopQueryResult, Stop closest) {
		super();
		this.stopQueryResult = stopQueryResult;
		this.closest = closest;
	}
	
}
