package com.example.demo.service;

import com.example.demo.model.GeoHashUtil;
import com.example.demo.model.Proposal;
import com.example.demo.model.Route;
import com.example.demo.model.Trip;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class RouteService {

    @Autowired
    private ProposalService proposalService;

    @Autowired
    private TripService tripService; 

    @Autowired
    private RouteOptimizer routeOptimizer;

    public Route generateRoute(String tripId, List<String> selectedProposalIds) throws Exception {
        Trip trip = tripService.getTrip(tripId);
        if (trip == null) {
            throw new RuntimeException("Trip not found! Please create constraints first.");
        }
        String startLoc = (trip.getStartLocation() != null) ? trip.getStartLocation() : "48.8606, 2.3376";
        String[] startCoords = startLoc.split(",");
        Set<String> allowedHashes = new HashSet<>();
        String endLoc = (trip.getEndLocation() != null) ? trip.getEndLocation() : "49.0097, 2.5479";
        String[] endCoords = endLoc.split(",");

        allowedHashes.addAll(GeoHashUtil.getGeoHashGrid(Double.parseDouble(startCoords[0].trim()), Double.parseDouble(startCoords[1].trim())));
        allowedHashes.addAll(GeoHashUtil.getGeoHashGrid( Double.parseDouble(endCoords[0].trim()), Double.parseDouble(endCoords[1].trim())));

        List<Proposal> allProposals = proposalService.getProposalsForTrip("GLOBAL_MARKETPLACE");
        if (allProposals == null || allProposals.isEmpty()) { 
            throw new RuntimeException("No active offers found in the marketplace!");
        }

        List<Proposal> approvedProposals = new ArrayList<>();
        List<Proposal> filtered = new ArrayList<>();


        for (Proposal p : allProposals) {
            if (p.getGeohash() != null && allowedHashes.contains(p.getGeohash())) {
                filtered.add(p);
            }
        }

        allProposals = filtered;
        for (Proposal p : allProposals) {
            if (selectedProposalIds == null || selectedProposalIds.isEmpty() || selectedProposalIds.contains(p.getProposalId())) {
                approvedProposals.add(p);
            }
        }

        if (approvedProposals.isEmpty()) {
            throw new RuntimeException("No proposals were selected for optimization!");
        }


        List<RouteOptimizer.Place> allPlaces = new ArrayList<>();
        for (Proposal p : approvedProposals) { 
            try {
                String[] coords = p.getLocation().split(",");
                double lat = Double.parseDouble(coords[0].trim());
                double lon = Double.parseDouble(coords[1].trim());
                allPlaces.add(new RouteOptimizer.Place(p.getDescription(), lat, lon, 0, p.getDurationMinutes(),
                        p.getOpenTime(),
                        p.getCloseTime(),
                        p.getPrice(), p.getProfit()));
            } catch (Exception e) {
                System.out.println("Skipping malformed proposal: " + p.getDescription());
            }
        }

        
        RouteOptimizer.Place startNode = new RouteOptimizer.Place("Start", Double.parseDouble(startCoords[0].trim()), Double.parseDouble(startCoords[1].trim()), 0, 0,trip.getStartTime(),
        	    "23:59", 0, 0);

        
        RouteOptimizer.Place endNode = new RouteOptimizer.Place("End", Double.parseDouble(endCoords[0].trim()), Double.parseDouble(endCoords[1].trim()), 0, 0,"00:00",
        	    "23:59", 0, 0);

        List<RouteOptimizer.Place> optimizedPath = routeOptimizer.optimizeRoute(
                startNode, endNode, allPlaces, 50000, trip.getMaxTimeMinutes(), trip.getBudget(), 0.001, 0.1, 0.5, 2.0
        );

        Route route = new Route();
        route.setRouteId(UUID.randomUUID().toString());
        route.setTripId(tripId);
        
        List<Route.Stop> stops = new ArrayList<>();
        int order = 1;
        double totalTime = 0;

        for (RouteOptimizer.Place p : optimizedPath) {
            Route.Stop stop = new Route.Stop();
            stop.setOrder(order++);
            stop.setProposalId(p.id);
            stop.setDescription(p.id);
            stop.setLat(p.lat);
            stop.setLon(p.lon);
            stop.setArrivalTime(
                    calculateArrivalTime(trip.getStartTime(), totalTime));
            stops.add(stop);

            totalTime += p.activityTime+ (p.travelDistanceFromPrev / 1000.0 * 15);
        }
        double totalCost = calculateTotalCost(optimizedPath);
        route.setStops(stops);
        route.setTotalCost(totalCost);
        route.setTotalTime(totalTime);

        // FIX: Use setValueAsync().get() to ensure data is saved BEFORE returning
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("routes");
        db.child(route.getRouteId()).setValueAsync(route).get(); 

        return route;
    }
    public double calculateTotalCost(List<RouteOptimizer.Place> places) {
        double totalCost = 0;

        for (RouteOptimizer.Place p : places) {
            totalCost += p.cost;
        }

        return totalCost;
    }
    
    public Route getRouteByTripId(String tripId) throws Exception {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("routes");
        CompletableFuture<Route> future = new CompletableFuture<>();

        db.orderByChild("tripId").equalTo(tripId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        future.complete(child.getValue(Route.class));
                        return;
                    }
                }
                future.complete(null);
            }
            @Override
            public void onCancelled(DatabaseError error) { future.completeExceptionally(error.toException()); }
        });
        return future.get();
    }
    private String calculateArrivalTime(String startTime, double minutes) {
        LocalTime time = LocalTime.parse(startTime);
        return time.plusMinutes((long) minutes).toString();
    }
}