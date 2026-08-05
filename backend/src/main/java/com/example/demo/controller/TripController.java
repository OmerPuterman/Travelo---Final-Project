package com.example.demo.controller;

import com.example.demo.model.Trip;
import com.example.demo.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    // The Android App will send a POST request here
    @PostMapping
    public String createTrip(@RequestBody Trip trip) {
        String newId = tripService.createTrip(trip);
        return "Trip created successfully with ID: " + newId;
    }
 // ... existing createTrip method ...

    @GetMapping("/{id}")
    public Trip getTrip(@PathVariable String id) throws Exception { // Just throw generic Exception
        return tripService.getTrip(id);
    }
    
    @GetMapping
    public List<Trip> getAllTrips() throws Exception {
        return tripService.getAllTrips();
    }
}