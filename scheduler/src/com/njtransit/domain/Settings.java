package com.njtransit.domain;

import java.util.ArrayList;

public class Settings {

	private ArrayList<Route> routes = new ArrayList<Route>();

	public ArrayList<Route> getRoutes() {
		return routes;
	}

	public void setRoutes(ArrayList<Route> routes) {
		this.routes = routes;
	}
	
}
