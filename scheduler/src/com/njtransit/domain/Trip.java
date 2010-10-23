package com.njtransit.domain;

public class Trip {

	private Integer id;
	
	private Integer serviceId;
	
	private String headsign;
	
	private Integer direction;
	
	private String blockId;
	
	private Integer routeId;
	
	public Trip(Integer id, Integer serviceId, String headsign,
			Integer direction, String blockId, Integer routeId) {
		this.id = id;
		this.serviceId = serviceId;
		this.headsign = headsign;
		this.direction = direction;
		this.blockId = blockId;
		this.routeId = routeId;
	}

	public boolean isNorth() {
		return direction.intValue() == 1;
	}
	
	public Integer getRouteId() {
		return routeId;
	}

	public Integer getId() {
		return id;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public String getHeadsign() {
		return headsign;
	}

	public Integer getDirection() {
		return direction;
	}

	public String getBlockId() {
		return blockId;
	}
	
	public String toString() {
		return getHeadsign();
	}	
}