package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RouteOptimizer {

    /* =======================
       DATA MODEL (Corrected)
       ======================= */
    public static class Place {
        public String id;
        public double lat, lon;
        public double distanceMeters; // <--- This was missing!
        public double activityTime; 
        public String openTime;
        public String closeTime;
        public double cost;         
        public double profit;
        
        // Output field: How far we traveled to get here
        public double travelDistanceFromPrev; 

        // 1. Constructor for creating Places (7 Arguments)
        public Place(String id, double lat, double lon, double distanceMeters,
                     double activityTime,String openTime,String closeTIme, double cost, double profit) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
            this.distanceMeters = distanceMeters;
            this.activityTime = activityTime;
            this.cost = cost;
            this.profit = profit;
            this.openTime=openTime;
            this.closeTime=closeTIme;
            this.travelDistanceFromPrev = 0;
        }

        // 2. Helper Constructor for the Algorithm (Internal use)
        public Place(Place p, double travelDist) {
            this.id = p.id;
            this.lat = p.lat;
            this.lon = p.lon;
            this.openTime = p.openTime;
            this.closeTime = p.closeTime;
            this.distanceMeters = p.distanceMeters;
            this.activityTime = p.activityTime;
            this.cost = p.cost;
            this.profit = p.profit;
            this.travelDistanceFromPrev = travelDist;
        }

        @Override
        public String toString() { return id; }
    }

    /* =======================
       SEARCH STATE
       ======================= */
    static class State implements Comparable<State> {
        Place current;
        List<Place> path;
        Set<Place> unvisited;
        int currentClockMinutes;
        double distance;
        double time;
        double cost;
        double profit;
        double gScore;
        double fScore;

        @Override
        public int compareTo(State other) {
            return Double.compare(other.fScore, this.fScore);
        }
    }

    /* =======================
       HELPER FUNCTIONS
       ======================= */
    private double distance(Place a, Place b) {
        double dx = a.lat - b.lat;
        double dy = a.lon - b.lon;
        // Approximation: 1 degree ~ 111km
        return Math.sqrt(dx * dx + dy * dy) * 111000; 
    }

    private double score(double dist, double time, double cost, double profit,
                        double wDist, double wTime, double wCost, double wProfit) {
        return (profit * wProfit) - (dist * wDist) - (time * wTime) - (cost * wCost);
    }

    private double heuristic(Set<Place> unvisited, double remainingTime, double remainingCost, double wProfit) {
        double estimate = 0;
        for (Place p : unvisited) {
            if (p.activityTime <= remainingTime && p.cost <= remainingCost) {
                estimate += p.profit * wProfit;
            }
        }
        return estimate;
    }

    /* =======================
       MAIN ALGORITHM
       ======================= */
    public List<Place> optimizeRoute(
            Place start,
            Place end, 
            List<Place> places,
            double maxDistance,
            double maxTime,
            double maxCost,
            double wDist,
            double wTime,
            double wCost,
            double wProfit) {

        PriorityQueue<State> openSet = new PriorityQueue<>();

        State startState = new State();
        startState.current = start;
        startState.path = new ArrayList<>();
        // Start point has 0 travel distance
        startState.path.add(new Place(start, 0)); 
        startState.unvisited = new HashSet<>(places);
        startState.currentClockMinutes =toMinutes(start.openTime); //hard code change!!!! omer (changed)
        startState.distance = 0;
        startState.time = 0;
        startState.cost = 0;
        startState.profit = 0;
        startState.gScore = 0;
        startState.fScore = 0;

        openSet.add(startState);
        State bestState = startState;
        
        int iterations = 0;
        int maxIterations = 5000; 

        while (!openSet.isEmpty() && iterations < maxIterations) {
            State current = openSet.poll();
            iterations++;

            // Check if we can reach the end from here
            double distToEnd = distance(current.current, end);
            if (current.distance + distToEnd <= maxDistance && 
                current.time + (distToEnd / 1000.0 * 15) <= maxTime) {
                 if (current.gScore > bestState.gScore) {
                     bestState = current;
                 }
            }

            for (Place nextPlace : current.unvisited) {
                double legDist = distance(current.current, nextPlace);
                double travelMinutes = legDist / 1000.0 * 15;
                int arrival =current.currentClockMinutes+ (int) travelMinutes;
                int open = toMinutes(nextPlace.openTime);
                int close = toMinutes(nextPlace.closeTime);
                int visitStart = Math.max(arrival, open);
                int visitEnd =visitStart+ (int) nextPlace.activityTime;
                if (visitEnd > close) {
                    continue;
                }
                double newDist = current.distance + legDist;
                double newTime = current.time + nextPlace.activityTime + (legDist / 1000.0 * 15);
                double newCost = current.cost + nextPlace.cost;
                double newProfit = current.profit + nextPlace.profit;

                // Critical: Can we make it to the End Node afterwards?
                double distFromNextToEnd = distance(nextPlace, end);
                double travelToEndMinutes = (distFromNextToEnd / 1000.0) * 15;
                if (newDist + distFromNextToEnd > maxDistance || 
                    newTime+travelToEndMinutes > maxTime || 
                    newCost > maxCost)
                    continue;

                State next = new State();
                next.current = nextPlace;
                next.path = new ArrayList<>(current.path);
                next.path.add(new Place(nextPlace, legDist)); 
                next.unvisited = new HashSet<>(current.unvisited);
                next.unvisited.remove(nextPlace);

                next.distance = newDist;
                next.time = newTime;
                next.cost = newCost;
                next.profit = newProfit;

                next.gScore = score(newDist, newTime, newCost, newProfit, wDist, wTime, wCost, wProfit);
                double h = heuristic(next.unvisited, maxTime - newTime, maxCost - newCost, wProfit);
                next.fScore = next.gScore + h;
                next.currentClockMinutes = visitEnd;
                openSet.add(next);
            }
        }

        // Add final leg to End Node
        double finalLegDist = distance(bestState.current, end);
        bestState.path.add(new Place(end, finalLegDist));

        return bestState.path;
    }
    private int toMinutes(String time) {
        String[] parts = time.split(":");

        return Integer.parseInt(parts[0]) * 60
                + Integer.parseInt(parts[1]);
    }
}