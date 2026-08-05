package com.example.demo.service;

import com.example.demo.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    public String createUser(User user) {
    	 if (user.getEmail() == null || user.getEmail().isBlank()) {
    	        throw new IllegalArgumentException("Email is required");
    	    }

    	    // Email format check
    	    if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
    	        throw new IllegalArgumentException("Invalid email format");
    	    }

    	    // Weight checks
    	    if (user.getWeightDistance() < 0 ||
    	        user.getWeightTime() < 0 ||
    	        user.getWeightCost() < 0 ||
    	        user.getWeightProfit() < 0) {

    	        throw new IllegalArgumentException("Weights cannot be negative");
    	    }
    	 if (user.getName() == null || user.getName().isEmpty()) {
    		 throw new IllegalArgumentException("User Name cannot be empty");
         }
    	 if (user.getRole() == null) {
    		 throw new IllegalArgumentException("User Email cannot be empty");
         }
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("users");
        
        // If no ID provided, generate one (Simulating a new registration)
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            user.setUserId(db.push().getKey());
        }

        db.child(user.getUserId()).setValueAsync(user);
        return user.getUserId();
    }

    public User getUser(String userId) throws InterruptedException, ExecutionException {
    	   if (userId == null || userId.isBlank()) {
    	        throw new IllegalArgumentException("User ID cannot be empty");
    	    }
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("users");
        CompletableFuture<User> future = new CompletableFuture<>();

        db.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
            	if (snapshot.exists()) {
            	    future.complete(snapshot.getValue(User.class));
            	} else {
            	    future.completeExceptionally(new RuntimeException("User not found"));
            }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        return future.get();
    }
    public String updateWeights(String userId, User user) throws InterruptedException, ExecutionException {
    	 if (user.getWeightDistance() < 0 ||
     	        user.getWeightTime() < 0 ||
     	        user.getWeightCost() < 0 ||
     	        user.getWeightProfit() < 0) {

     	        throw new IllegalArgumentException("Weights cannot be negative");
     	    }
    	 getUser(userId);

        DatabaseReference db = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(userId);

        Map<String, Object> updates = new HashMap<>();

        updates.put("weightDistance", user.getWeightDistance());
        updates.put("weightTime", user.getWeightTime());
        updates.put("weightCost", user.getWeightCost());
        updates.put("weightProfit", user.getWeightProfit());

        db.updateChildrenAsync(updates);

        return "Weights updated successfully";
    }
}