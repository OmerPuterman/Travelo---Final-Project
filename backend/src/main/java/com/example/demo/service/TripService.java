package com.example.demo.service;

import com.example.demo.model.Trip;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.stereotype.Service;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.util.ArrayList;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class TripService {

    public String createTrip(Trip trip) {
    	if (trip == null) {
            throw new IllegalArgumentException("Trip cannot be null");
        }

        if (trip.getDestination() == null || trip.getDestination().isBlank()) {
            throw new IllegalArgumentException("Destination is required");
        }

        if (trip.getGuideId() == null || trip.getGuideId().isBlank()) {
            throw new IllegalArgumentException("Guide ID is required");
        }

        if (trip.getBudget() < 0) {
            throw new IllegalArgumentException("Budget cannot be negative");
        }

        if (trip.getMaxTimeMinutes() <= 0) {
            throw new IllegalArgumentException("Max time must be positive");
        }
        if (trip.getStartTime() != null && 
            !trip.getStartTime().matches("\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("Invalid time format");
        }
        // 1. Get a reference to the "trips" section in your database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("trips");

        // 2. Generate a unique ID for the trip if it doesn't have one
        if (trip.getTripId() == null) {
            String uniqueID = UUID.randomUUID().toString();
            trip.setTripId(uniqueID);
        }

        // 3. Save the data to Firebase (Non-blocking)
        database.child(trip.getTripId()).setValueAsync(trip);

        return trip.getTripId();
    }
    
    public Trip getTrip(String tripId) throws InterruptedException, java.util.concurrent.ExecutionException {
    	if(tripId == null || tripId.isBlank()){
    	    throw new IllegalArgumentException("Trip ID cannot be empty");
    	}
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("trips");
        java.util.concurrent.CompletableFuture<Trip> future = new java.util.concurrent.CompletableFuture<>();

        // Use Query to find the object where the "tripId" field matches your string
        db.orderByChild("tripId").equalTo(tripId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Iterate through results; there should be only one match
                    for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                        Trip trip = child.getValue(Trip.class);
                        future.complete(trip);
                        return; // Return the first match found
                    }
                } else {
                    System.out.println("DEBUG: No trip found with tripId: " + tripId);
                    future.completeExceptionally(
                            new RuntimeException("Trip not found")
                        );
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        
        return future.get();
    }
    
    public List<Trip> getAllTrips() throws InterruptedException, ExecutionException {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("trips");
        CompletableFuture<List<Trip>> future = new CompletableFuture<>();

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Trip> tripList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Trip trip = child.getValue(Trip.class);
                    if (trip != null) {
                        tripList.add(trip);
                    }
                }
                future.complete(tripList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future.get();
    }
}