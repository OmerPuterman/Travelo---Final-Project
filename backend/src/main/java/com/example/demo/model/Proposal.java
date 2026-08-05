package com.example.demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proposal {
    private String proposalId;
    private String tripId;      // The Trip this offer is for
    private String businessId;  // The Business User who sent it
    private double lat;
    private double lng;
    private String description; // e.g., "Entry ticket + Lunch"
    private double price;       // The cost to the group
    private String location;    // e.g., "32.0853, 34.7818" (Lat/Lng)
    
    // In your diagram, availability is a "TimeWindow". 
    // For MVP, we can just use simple Strings for start/end time.
    public String openTime;
    public String closeTime;
    private double durationMinutes;
    private String geohash;
    private double profit;
    // Status: PENDING, ACCEPTED, REJECTED
    private Status status; 

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}