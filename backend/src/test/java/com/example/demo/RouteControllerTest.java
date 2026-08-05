package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.demo.model.GeoHashUtil;
import com.example.demo.model.Proposal;
import com.example.demo.model.Route;
import com.example.demo.model.Trip;
import com.example.demo.service.ProposalService;
import com.example.demo.service.RouteOptimizer;
import com.example.demo.service.RouteService;
import com.example.demo.service.TripService;
import com.google.firebase.database.FirebaseDatabase;

import tools.jackson.databind.ObjectMapper;
@SpringBootTest
@AutoConfigureMockMvc
class RouteControllerTest {

	@Autowired
    private MockMvc mockMvc;
	 @Autowired
	    private RouteService routeService;
	 @Autowired
	    private TripService tripService;
	 @Autowired
	    private ProposalService proposalService;
	
	 
	 //Generate Route
	 
	 @Test
	 void generateRoute_shouldReturnRoute() throws Exception {

	     Proposal proposal = new Proposal();
	     proposal.setProposalId("proposal-1");
	     proposal.setLocation("48.8610, 2.3370");
	     proposal.setOpenTime("09:00");
	     proposal.setCloseTime("10:00");
	     proposal.setPrice(20);
	     proposal.setProfit(30);
	     Set<String> hashes = GeoHashUtil.getGeoHashGrid(48.8610, 2.3370);
	     proposal.setGeohash(hashes.iterator().next());

	     FirebaseDatabase.getInstance()
	         .getReference("proposals")
	         .child("GLOBAL_MARKETPLACE")
	         .child("proposal-1")
	         .setValueAsync(proposal)
	         .get();


	     Trip trip = new Trip();
	     trip.setTripId("test-trip");
	     trip.setGuideId("guide-1");
	     trip.setDestination("Paris");
	     trip.setBudget(500);
	     trip.setMaxTimeMinutes(300);
	     trip.setStartLocation("48.8566,2.3522");
	     trip.setEndLocation("48.8606,2.3376");
	     trip.setStartTime("08:00");

	     FirebaseDatabase.getInstance()
	         .getReference("trips")
	         .child("test-trip")
	         .setValueAsync(trip)
	         .get();


	     MvcResult result = mockMvc.perform(post("/api/routes/generate/test-trip")
	    	        .contentType(MediaType.APPLICATION_JSON)
	    	        .content("[\"proposal-1\"]"))
	    	        .andExpect(status().isOk())
	    	        .andReturn();
	     String response = result.getResponse().getContentAsString();

	     ObjectMapper mapper = new ObjectMapper();

	     Route route = mapper.readValue(response, Route.class);

	     String routeId = route.getRouteId();

	     FirebaseDatabase.getInstance()
	         .getReference("proposals")
	         .child("GLOBAL_MARKETPLACE")
	         .child("proposal-1")
	         .removeValueAsync()
	         .get();

	     FirebaseDatabase.getInstance()
	         .getReference("trips")
	         .child("test-trip")
	         .removeValueAsync()
	         .get();
	     
	     FirebaseDatabase.getInstance()
         .getReference("routes")
         .child(routeId)
         .removeValueAsync()
         .get();
	 }
	 @Test
	 void generateRoute_nonExistingTrip_shouldFail() throws Exception {

	     var result = mockMvc.perform(
	             post("/api/routes/generate/not-exist-123")
	             .contentType(MediaType.APPLICATION_JSON)
	             .content("[]"))
	             .andReturn();

	     assertEquals(500, result.getResponse().getStatus());
	     assertTrue(result.getResponse().getContentAsString()
	             .contains("Trip not found"));
	 }
	 @Test
	 void generateRoute_withInvalidProposalIds_shouldFail() throws Exception {

	     Trip trip = new Trip();
	     trip.setTripId("test-trip");
	     trip.setGuideId("guide-1");
	     trip.setDestination("Paris");
	     trip.setStartDate("2026-08-10");
	     trip.setBudget(500);
	     trip.setMaxTimeMinutes(300);
	     trip.setStartLocation("48.8566,2.3522");
	     trip.setEndLocation("48.8606,2.3376");
	     trip.setStartTime("08:00");

	     tripService.createTrip(trip);

	     var result = mockMvc.perform(
	             post("/api/routes/generate/test-trip")
	             .contentType(MediaType.APPLICATION_JSON)
	             .content("""
	                 [
	                   "wrong-id"
	                 ]
	             """))
	             .andReturn();

	     assertEquals(500, result.getResponse().getStatus());

	     FirebaseDatabase.getInstance()
	         .getReference("trips")
	         .child("test-trip")
	         .removeValueAsync()
	         .get();
	 }	 
	 //GET ROUTE
	 
	 @Test
	 void getRoute_existing_shouldReturnRoute() throws Exception {

	     Route route = new Route();
	     route.setRouteId("route-1");
	     route.setTripId("test-trip");
	     route.setStops(new ArrayList<>());
	     route.setTotalCost(100);
	     route.setTotalTime(120);

	     FirebaseDatabase.getInstance()
	             .getReference("routes")
	             .child("route-1")
	             .setValueAsync(route)
	             .get();


	     var result = mockMvc.perform(get("/api/routes/trip/test-trip"))
	             .andExpect(status().isOk())
	             .andReturn();


	     assertNotNull(result.getResponse().getContentAsString());


	     FirebaseDatabase.getInstance()
	             .getReference("routes")
	             .child("route-1")
	             .removeValueAsync()
	             .get();
	 }
	 
	 @Test
	 void getRoute_nonExisting_shouldReturn404() throws Exception {

	     mockMvc.perform(get("/api/routes/trip/not-exist-123"))
	             .andExpect(status().isNotFound());
	 }
	 @Test
	 void getRoute_nullRoute_shouldReturn404() throws Exception {

	     mockMvc.perform(get("/api/routes/trip/not-exist-123"))
	             .andExpect(status().isNotFound());
	 }
	 
}
