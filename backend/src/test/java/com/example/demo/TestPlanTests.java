package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
class TestPlanTests {
	 @Autowired
	    private ProposalService proposalService;
	 @Autowired
	    private TripService tripService;
	 @Autowired
	    private RouteService routeService;
	 @Autowired
	    private MockMvc mockMvc;
	 
	 //T-01
	 @Test
	    void calculateTotalCost_shouldReturnCorrectSum() {

	        RouteOptimizer.Place p1 =
	                new RouteOptimizer.Place(
	                        "Museum",
	                        48.861,
	                        2.337,
	                        0,
	                        60,
	                        "09:00",
	                        "17:00",
	                        20,
	                        30
	                );

	        RouteOptimizer.Place p2 =
	                new RouteOptimizer.Place(
	                        "Restaurant",
	                        48.862,
	                        2.338,
	                        0,
	                        90,
	                        "12:00",
	                        "15:00",
	                        35,
	                        50
	                );


	        double result =
	                routeService.calculateTotalCost(List.of(p1, p2));


	        assertEquals(55, result);
	    }
	 
	 // T-02
	 @Test
		void createTrip_negativeBudget_shouldFail() throws Exception {

		    String json = """
		        {
		          "guideId":"guide-123",
		          "destination":"Paris",
		          "budget":-500,
		          "maxTimeMinutes":300,
		          "numberOfTravelers":2
		        }
		        """;

		    assertThrows(Exception.class, () -> {
			    mockMvc.perform(post("/api/trips")
			            .contentType(MediaType.APPLICATION_JSON)
			            .content(json));

		    	});

		}
//       T-03
	@Test
	void createTrip_validTrip_shouldReturnSuccess() throws Exception {

		
		//Create Trip
	    String json = """
	        {
	          "tripId":"test-trip-1",
	          "guideId":"guide-123",
	          "destination":"Paris",
	          "startDate":"2026-08-10",
	          "budget":500,
	          "maxTimeMinutes":300,
	          "startLocation":"48.8566,2.3522",
	          "endLocation":"48.8606,2.3376",
	          "numberOfTravelers":2,
	          "startTime":"08:00"
	        }
	        """;


	    String response = mockMvc.perform(post("/api/trips")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(json))
	            .andExpect(status().isOk())
	            .andReturn()
	            .getResponse()
	            .getContentAsString();
	    FirebaseDatabase.getInstance()
        .getReference("trips")
        .child("test-trip-1")
        .removeValueAsync()
        .get();
	    
	}
	
	//T-04
	 @Test
	 void createProposal_shouldSaveProposal() throws Exception {

	     // Create trip first
	     Trip trip = new Trip();
	     trip.setTripId("test-trip");
	     trip.setGuideId("guide-1");
	     trip.setDestination("Paris");
	     trip.setBudget(500);
	     trip.setMaxTimeMinutes(300);
	     trip.setStartLocation("48.8566,2.3522");
	     trip.setEndLocation("48.8606,2.3376");
	     trip.setStartTime("08:00");

	     tripService.createTrip(trip);


	     mockMvc.perform(post("/api/proposals")
	             .contentType(MediaType.APPLICATION_JSON)
	             .content("""
	             {
	                 "proposalId":"test-proposal-1",
	                 "tripId":"test-trip",
	                 "businessId":"business-1",
	                 "lat":48.8610,
	                 "lng":2.3370,
	                 "description":"Museum ticket",
	                 "price":25,
	                 "location":"48.8610,2.3370",
	                 "openTime":"09:00",
	                 "closeTime":"17:00",
	                 "durationMinutes":60,
	                 "profit":40,
	                 "status":"PENDING"
	             }
	             """))
	             .andExpect(status().isOk());


	     List<Proposal> proposals =
	             proposalService.getProposalsForTrip("test-trip");


	     assertEquals(1, proposals.size());
	     assertEquals("test-proposal-1",
	             proposals.get(0).getProposalId());


	     // cleanup
	     FirebaseDatabase.getInstance()
	     .getReference("proposals")
	     .child("test-trip")
	     .child("test-proposal-1")
	     .removeValueAsync()
	     .get();

	FirebaseDatabase.getInstance()
	        .getReference("trips")
	        .child("test-trip")
	        .removeValueAsync()
	        .get();
	 }
	 //T-05
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
	//T-06
	 @Test
	 void generateRoute_100Requests_shouldFinishUnder10Seconds() throws Exception {//100 route generations took: 19225 ms (19.25 seconds)

	     List<String> routeIds = new ArrayList<>();

	     try {

	         // Create proposal
	         Proposal proposal = new Proposal();

	         proposal.setProposalId("performance-proposal");
	         proposal.setLocation("48.8610,2.3370");
	         proposal.setDescription("Performance Museum");
	         proposal.setOpenTime("09:00");
	         proposal.setCloseTime("17:00");
	         proposal.setPrice(20);
	         proposal.setProfit(30);
	         proposal.setDurationMinutes(60);


	         Set<String> hashes =
	                 GeoHashUtil.getGeoHashGrid(48.8610, 2.3370);

	         proposal.setGeohash(hashes.iterator().next());


	         FirebaseDatabase.getInstance()
	                 .getReference("proposals")
	                 .child("GLOBAL_MARKETPLACE")
	                 .child("performance-proposal")
	                 .setValueAsync(proposal)
	                 .get();



	         // Create trip
	         Trip trip = new Trip();

	         trip.setTripId("performance-trip");
	         trip.setGuideId("guide-1");
	         trip.setDestination("Paris");
	         trip.setBudget(500);
	         trip.setMaxTimeMinutes(300);
	         trip.setStartLocation("48.8566,2.3522");
	         trip.setEndLocation("48.8606,2.3376");
	         trip.setStartTime("08:00");


	         FirebaseDatabase.getInstance()
	                 .getReference("trips")
	                 .child("performance-trip")
	                 .setValueAsync(trip)
	                 .get();



	         ObjectMapper mapper = new ObjectMapper();


	         long startTime = System.currentTimeMillis();


	         // Generate 100 routes
	         for(int i = 0; i < 100; i++) {


	             MvcResult result = mockMvc.perform(
	                     post("/api/routes/generate/performance-trip")
	                     .contentType(MediaType.APPLICATION_JSON)
	                     .content("[\"performance-proposal\"]"))
	                     .andExpect(status().isOk())
	                     .andReturn();



	             Route route = mapper.readValue(
	                     result.getResponse().getContentAsString(),
	                     Route.class
	             );


	             // Save generated UUID
	             routeIds.add(route.getRouteId());
	         }


	         long endTime = System.currentTimeMillis();


	         long totalTime = endTime - startTime;


	         System.out.println(
	                 "100 route generations took: "
	                 + totalTime
	                 + " ms"
	         );


	         // Less than 10 seconds
	         assertTrue(totalTime < 10000);



	     }
	     finally {


	         // Delete generated routes
	         for(String routeId : routeIds) {

	             FirebaseDatabase.getInstance()
	                     .getReference("routes")
	                     .child(routeId)
	                     .removeValueAsync()
	                     .get();
	         }



	         // Delete proposal
	         FirebaseDatabase.getInstance()
	                 .getReference("proposals")
	                 .child("GLOBAL_MARKETPLACE")
	                 .child("performance-proposal")
	                 .removeValueAsync()
	                 .get();



	         // Delete trip
	         FirebaseDatabase.getInstance()
	                 .getReference("trips")
	                 .child("performance-trip")
	                 .removeValueAsync()
	                 .get();
	     }
	 }
	 //T-09
	 @Test
	 void generateRoute_with50RandomProposals_100Routes_shouldScale() throws Exception {//100 routes with 50 random proposals took: 24315 ms (24.3 sec)

	     List<String> routeIds = new ArrayList<>();
	     List<String> proposalIds = new ArrayList<>();

	     Random random = new Random();

	     try {

	         // Create 50 random business proposals
	         for (int i = 0; i < 50; i++) {

	             String proposalId = "scale-proposal-" + i;
	             proposalIds.add(proposalId);

	             Proposal proposal = new Proposal();

	             // Random location near Paris
	             double lat = 48.8600 + (random.nextDouble() * 0.005);
	             double lng = 2.3350 + (random.nextDouble() * 0.005);


	             int openHour = 8 + random.nextInt(5);
	             int closeHour = openHour + 3 + random.nextInt(5);


	             proposal.setProposalId(proposalId);
	             proposal.setDescription("Business offer " + i);

	             proposal.setLat(lat);
	             proposal.setLng(lng);

	             proposal.setLocation(
	                     lat + "," + lng
	             );


	             proposal.setOpenTime(
	                     String.format("%02d:00", openHour)
	             );

	             proposal.setCloseTime(
	                     String.format("%02d:00", closeHour)
	             );


	             proposal.setPrice(
	                     10 + random.nextInt(60)
	             );

	             proposal.setProfit(
	                     20 + random.nextInt(80)
	             );

	             proposal.setDurationMinutes(
	                     30 + random.nextInt(90)
	             );


	             Set<String> hashes =
	                     GeoHashUtil.getGeoHashGrid(lat, lng);

	             proposal.setGeohash(
	                     hashes.iterator().next()
	             );


	             FirebaseDatabase.getInstance()
	                     .getReference("proposals")
	                     .child("GLOBAL_MARKETPLACE")
	                     .child(proposalId)
	                     .setValueAsync(proposal)
	                     .get();
	         }



	         // Create trip
	         Trip trip = new Trip();

	         trip.setTripId("scale-trip");
	         trip.setGuideId("guide-scale");
	         trip.setDestination("Paris");
	         trip.setBudget(350);
	         trip.setMaxTimeMinutes(300);
	         trip.setStartLocation("48.8566,2.3522");
	         trip.setEndLocation("48.8606,2.3376");
	         trip.setStartTime("08:00");


	         FirebaseDatabase.getInstance()
	                 .getReference("trips")
	                 .child("scale-trip")
	                 .setValueAsync(trip)
	                 .get();




	         long start = System.currentTimeMillis();

	         
	         ObjectMapper mapper = new ObjectMapper();

	      // Generate 100 routes
	         for (int i = 0; i < 100; i++) {

	             MvcResult result = mockMvc.perform(
	                     post("/api/routes/generate/scale-trip")
	                     .contentType(MediaType.APPLICATION_JSON)
	                     .content(mapper.writeValueAsString(proposalIds))
	             )
	             .andExpect(status().isOk())
	             .andReturn();


	             Route route = mapper.readValue(
	                     result.getResponse().getContentAsString(),
	                     Route.class
	             );

	             routeIds.add(route.getRouteId());
	         }

	         long end = System.currentTimeMillis();

	         long totalTime = end - start;

	         System.out.println(
	                 "100 routes with 50 proposals took: "
	                 + totalTime
	                 + " ms"
	         );

	         assertEquals(100, routeIds.size());


	     }
	     finally {


	         // Delete generated routes
	         for (String routeId : routeIds) {

	             FirebaseDatabase.getInstance()
	                     .getReference("routes")
	                     .child(routeId)
	                     .removeValueAsync()
	                     .get();
	         }



	         // Delete proposals
	         for (String proposalId : proposalIds) {

	             FirebaseDatabase.getInstance()
	                     .getReference("proposals")
	                     .child("GLOBAL_MARKETPLACE")
	                     .child(proposalId)
	                     .removeValueAsync()
	                     .get();
	         }



	         // Delete trip
	         FirebaseDatabase.getInstance()
	                 .getReference("trips")
	                 .child("scale-trip")
	                 .removeValueAsync()
	                 .get();
	     }
	 }
}
