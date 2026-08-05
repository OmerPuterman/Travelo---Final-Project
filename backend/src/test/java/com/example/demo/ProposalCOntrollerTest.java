package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.example.demo.model.Proposal;
import com.example.demo.model.Trip;
import com.example.demo.service.ProposalService;
import com.example.demo.service.TripService;
import com.google.firebase.database.FirebaseDatabase;
@SpringBootTest
@AutoConfigureMockMvc
class ProposalCOntrollerTest {
	 @Autowired
	    private ProposalService proposalService;
	 @Autowired
	    private TripService tripService;
	 @Autowired
	    private MockMvc mockMvc;
//CREATE PROPOSAL
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
	 @Test
	 void createProposal_withoutTripId_shouldFail() {

	     Proposal proposal = new Proposal();
	     proposal.setProposalId("test-proposal-2");
	     proposal.setBusinessId("business-1");
	     proposal.setDescription("Lunch");
	     proposal.setPrice(30);
	     proposal.setLocation("48.8610,2.3370");

	     assertThrows(Exception.class, () -> {
	         proposalService.createProposal(proposal);
	     });
	 }

	  //GET
	 @Test
	 void getProposalsByTrip_shouldReturnProposal() throws Exception {

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


	     Proposal proposal = new Proposal();
	     proposal.setProposalId("test-proposal-2");
	     proposal.setTripId("test-trip");
	     proposal.setBusinessId("business-1");
	     proposal.setDescription("Lunch");
	     proposal.setPrice(30);
	     proposal.setLocation("48.8610,2.3370");


	     proposalService.createProposal(proposal);


	     var result = mockMvc.perform(
	             get("/api/proposals/trip/test-trip"))
	             .andExpect(status().isOk())
	             .andReturn();


	     assertTrue(result.getResponse()
	             .getContentAsString()
	             .contains("test-proposal-2"));


	     FirebaseDatabase.getInstance()
	     .getReference("proposals")
	     .child("test-trip")
	     .child("test-proposal-2")
	     .removeValueAsync()
	     .get();

	     FirebaseDatabase.getInstance()
	             .getReference("trips")
	             .child("test-trip")
	             .removeValueAsync();
	 }


	    @Test
	    void getProposalsByTrip_noProposals_shouldReturnEmptyList() throws Exception {


	        var result = mockMvc.perform(
	                get("/api/proposals/trip/no-proposals-trip"))
	                .andExpect(status().isOk())
	                .andReturn();


	        assertEquals("[]",
	                result.getResponse().getContentAsString());
	    }
	    @Test
	    void getProposals_nonExistingTrip_shouldReturnEmptyList() throws Exception {

	        List<Proposal> proposals =
	                proposalService.getProposalsForTrip("trip-that-does-not-exist");

	        assertTrue(proposals.isEmpty());
	    }
	}
