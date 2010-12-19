package com.njtransit.model;

import com.njtransit.domain.IService;
import com.njtransit.domain.Station;
import com.njtransit.domain.Stop;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class StopsQueryResult {

    private Station depart, arrive;

    private Long queryStart, queryEnd;

    private Map<Integer, IService> tripToService;

    private Station transferStation;

    private List<Stop> stops;

    private Calendar departureDate;

    public StopsQueryResult(Calendar departureDate, Station depart, Station arrive, Long queryStart, Long queryEnd, Map<Integer, IService> tripToService, List<Stop> stops) {
        this.depart = depart;
        this.arrive = arrive;
        this.queryEnd = queryEnd;
        this.queryStart = queryStart;
        this.tripToService = tripToService;
        this.departureDate = departureDate;
        this.stops = stops;
    }

    public StopsQueryResult(Station depart, Station arrive, Long queryStart, Long queryEnd, Map<Integer, IService> tripToService, List<Stop> stops) {
        this(null, depart, arrive, queryStart, queryEnd, tripToService, stops);
    }

    public Calendar getDepartureDate() {
        return departureDate;
    }

    public Long getQueryDuration() {
        return queryEnd - queryStart;
    }

    public Station getDepart() {
        return depart;
    }

    public Station getArrive() {
        return arrive;
    }

    public Long getQueryStart() {
        return queryStart;
    }

    public Long getQueryEnd() {
        return queryEnd;
    }

    public Map<Integer, IService> getTripToService() {
        return tripToService;
    }

    public List<Stop> getStops() {
        return stops;
    }


    public Station getTransferStation() {
        return transferStation;
    }

}