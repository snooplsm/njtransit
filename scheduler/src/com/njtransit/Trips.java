package com.njtransit;

import java.util.HashMap;
import java.util.Map;

import com.njtransit.domain.IService;

public class Trips extends HashMap<Integer, IService> {

	private Map<Integer,String> tripIdToBlock = new HashMap<Integer,String>();
	
	public void add(Integer tripId, String block) {
		tripIdToBlock.put(tripId, block);
	}
	
	public String block(int tripId) {
		return tripIdToBlock.get(tripId);
	}
}
