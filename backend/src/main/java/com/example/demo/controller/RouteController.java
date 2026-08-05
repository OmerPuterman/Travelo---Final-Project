package com.example.demo.controller;

import com.example.demo.model.Route;
import com.example.demo.service.RouteOptimizer;
import com.example.demo.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @PostMapping("/generate/{tripId}")
    public ResponseEntity<?> generateLiveRoute(
            @PathVariable String tripId, 
            @RequestBody(required = false) List<String> selectedProposalIds) {
        try {
            Route route = routeService.generateRoute(tripId, selectedProposalIds);
            return ResponseEntity.ok(route); 
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<?> getRoute(@PathVariable String tripId) {
        try {
            Route route = routeService.getRouteByTripId(tripId);
            if (route == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(route);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error fetching route");
        }
    }
}