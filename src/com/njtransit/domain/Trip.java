package com.njtransit.domain;

public class Trip {

	private Integer id;
	
	private Integer serviceId;
	
	private String headsign;
	
	private Integer direction;
	
	private String blockId;
	
	private Integer routeId;

	public Trip() {
		
	}
	
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

	public void setRouteId(Integer routeId) {
		this.routeId = routeId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(Integer serviceId) {
		this.serviceId = serviceId;
	}

	public String getHeadsign() {
		return headsign;
	}

	public void setHeadsign(String headsign) {
		this.headsign = headsign;
	}

	public Integer getDirection() {
		return direction;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}

	public String getBlockId() {
		return blockId;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}
	
}
