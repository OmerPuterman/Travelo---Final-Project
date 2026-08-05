package com.example.demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    private String tripId;
    private String guideId;
    private String destination;
    private String startDate;
    private double budget;              // <-- NEW
    private int maxTimeMinutes;         // <-- NEW
    private String startLocation;       // <-- NEW
    private String endLocation;         // <-- NEW
    private Integer numberOfTravelers;
    public String startTime;
    }