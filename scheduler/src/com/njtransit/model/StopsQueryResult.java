package com.njtransit.model;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.njtransit.Trips;
import com.njtransit.domain.IService;
import com.njtransit.domain.Station;
import com.njtransit.domain.Stop;

public class StopsQueryResult {

    private Station depart, arrive;

    private Long queryStart, queryEnd;

    private Trips tripToService;

    private Station transferStation;

    private List<Stop> stops;

    private Calendar departureDate;

    public StopsQueryResult(Calendar departureDate, Station depart, Station arrive, Long queryStart, Long queryEnd, Trips trips, List<Stop> stops) {
        this.depart = depart;
        this.arrive = arrive;
        this.queryEnd = queryEnd;
        this.queryStart = queryStart;
        this.tripToService = trips;
        this.departureDate = departureDate;
        this.stops = stops;
        for(Stop s : stops) {
        	s.setBlockId(trips.block(s.getTripId()));
        }
    }

    public StopsQueryResult(Station depart, Station arrive, Long queryStart, Long queryEnd, Trips trips, List<Stop> stops) {
        this(null, depart, arrive, queryStart, queryEnd, trips, stops);
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