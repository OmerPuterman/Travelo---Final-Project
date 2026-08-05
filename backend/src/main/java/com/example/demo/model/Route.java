package com.example.demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    private String routeId;
    private String tripId;
    private List<Stop> stops; 
    private double totalCost;
    private double totalTime; 
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stop {
        private int order;          
        private String proposalId;  
        private String description; 
        private String arrivalTime; 
        
        // --- NEW: Keep the exact coordinates for Google Maps ---
        private double lat;
        private double lon;
    }
}