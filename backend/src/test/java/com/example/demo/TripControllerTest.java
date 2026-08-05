package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.model.Trip;
import com.example.demo.service.TripService;
import com.google.firebase.database.FirebaseDatabase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@SpringBootTest
@AutoConfigureMockMvc
class TripControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TripService tripService;
	

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
	@Test
	void createTrip_shouldSaveAllFields() throws Exception {

	    Trip trip = new Trip();

	    trip.setGuideId("guide-1");
	    trip.setDestination("Rome");
	    trip.setStartDate("2026-09-01");
	    trip.setBudget(1000);
	    trip.setMaxTimeMinutes(600);
	    trip.setStartLocation("Start");
	    trip.setEndLocation("End");
	    trip.setNumberOfTravelers(4);
	    trip.setStartTime("09:00");


	    String id = tripService.createTrip(trip);


	    Trip saved = tripService.getTrip(id);


	    assertEquals("guide-1", saved.getGuideId());
	    assertEquals("Rome", saved.getDestination());
	    assertEquals("2026-09-01", saved.getStartDate());
	    assertEquals(1000, saved.getBudget());
	    assertEquals(600, saved.getMaxTimeMinutes());
	    assertEquals(4, saved.getNumberOfTravelers());
	    assertEquals("09:00", saved.getStartTime());


	    FirebaseDatabase.getInstance()
	            .getReference("trips")
	            .child(id)
	            .removeValueAsync();
	}
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
	@Test
	void createTrip_withWrongStartDateType_shouldFail() {

	    String json = """
	        {
	          "tripId":"trip-123",
	          "guideId":"guide-123",
	          "destination":"Paris",
	          "startDate":123,
	          "budget":500,
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
	@Test
	void createTrip_withWrongStartLocationType_shouldFail() {

	    String json = """
	        {
	          "startLocation":123,
	          "endLocation":"48.8606,2.3376",
	          "budget":500,
	          "maxTimeMinutes":300
	        }
	        """;


	    assertThrows(Exception.class, () -> {
	        mockMvc.perform(post("/api/trips")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(json));
	    });
	}
	@Test
	void createTrip_withWrongEndLocationType_shouldFail() {

	    String json = """
	        {
	          "startLocation":"48.8566,2.3522",
	          "endLocation":123,
	          "budget":500,
	          "maxTimeMinutes":300
	        }
	        """;


	    assertThrows(Exception.class, () -> {
	        mockMvc.perform(post("/api/trips")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(json));
	    });
	}
	@Test
	void createTrip_withWrongStartTimeType_shouldFail() {

	    String json = """
	        {
	          "startTime":123,
	          "budget":500,
	          "maxTimeMinutes":300
	        }
	        """;


	    assertThrows(Exception.class, () -> {
	        mockMvc.perform(post("/api/trips")
	                .contentType(MediaType.APPLICATION_JSON)
	                .content(json));
	    });
	}
	@Test
	void createTrip_withoutId_shouldGenerateId() throws Exception {

	    Trip trip = new Trip();

	    trip.setGuideId("guide-123");
	    trip.setDestination("Paris");
	    trip.setBudget(500);
	    trip.setMaxTimeMinutes(300);
	    trip.setNumberOfTravelers(2);
	    trip.setStartTime("08:00");


	    String id = tripService.createTrip(trip);


	    assertNotNull(id);
	    assertFalse(id.isEmpty());


	    Trip saved = tripService.getTrip(id);

	    assertNotNull(saved);
	    assertEquals("Paris", saved.getDestination());
	    assertEquals(500, saved.getBudget());
	    assertEquals(300, saved.getMaxTimeMinutes());


	    FirebaseDatabase.getInstance()
	            .getReference("trips")
	            .child(id)
	            .removeValueAsync()
	            .get();
	}
	//Get Trip
	@Test
	void getTrip_existingTrip_shouldReturnTrip() throws Exception {

	    Trip trip = new Trip();

	    trip.setTripId("test-trip-123");
	    trip.setGuideId("guide-1");
	    trip.setDestination("Paris");
	    trip.setStartDate("2026-08-10");
	    trip.setBudget(500);
	    trip.setMaxTimeMinutes(300);
	    trip.setStartLocation("Start");
	    trip.setEndLocation("End");
	    trip.setNumberOfTravelers(2);
	    trip.setStartTime("08:00");


	    String result = tripService.createTrip(trip);


	    mockMvc.perform(get("/api/trips/" + result))
	            .andExpect(status().isOk())
	            .andExpect(jsonPath("$.destination")
	                    .value("Paris"));
	    
	    FirebaseDatabase.getInstance()
        .getReference("trips")
        .child(result)
        .removeValueAsync()
        .get();
	}
	@Test
	void getTrip_existingTrip_shouldReturnAllData() throws Exception {

	    Trip trip = new Trip();

	    trip.setTripId("test-trip-123");
	    trip.setGuideId("guide-1");
	    trip.setDestination("Paris");
	    trip.setStartDate("2026-08-10");
	    trip.setBudget(500);
	    trip.setMaxTimeMinutes(300);
	    trip.setStartLocation("Start");
	    trip.setEndLocation("End");
	    trip.setNumberOfTravelers(2);
	    trip.setStartTime("08:00");


	    String id = tripService.createTrip(trip);


	    Trip result = tripService.getTrip(id);


	    assertNotNull(result);
	    assertEquals("Paris", result.getDestination());
	    assertEquals(500, result.getBudget());
	    assertEquals(300, result.getMaxTimeMinutes());
	    assertEquals(2, result.getNumberOfTravelers());


	    FirebaseDatabase.getInstance()
	            .getReference("trips")
	            .child(id)
	            .removeValueAsync()
	            .get();
	}
	@Test
	void getTrip_nonExisting_shouldFail() throws Exception {

		  assertThrows(Exception.class, () -> {
		        mockMvc.perform(get("/api/trips/not-exist-123"));
		    });
	}
	@Test
	void getTrip_emptyId_shouldFail() {

	    assertThrows(Exception.class, () -> {
	        tripService.getTrip("");
	    });
	}
	
	//GET ALL TRIPS
	@Test
	void getAllTrips_shouldReturnTwoTrips() throws Exception {

	    Trip trip1 = new Trip();
	    trip1.setTripId("trip-1");
	    trip1.setGuideId("guide-1");
	    trip1.setDestination("Paris");
	    trip1.setBudget(100);
	    trip1.setMaxTimeMinutes(120);

	    Trip trip2 = new Trip();
	    trip2.setTripId("trip-2");
	    trip2.setGuideId("guide-2");
	    trip2.setDestination("London");
	    trip2.setBudget(200);
	    trip2.setMaxTimeMinutes(240);

	    tripService.createTrip(trip1);
	    tripService.createTrip(trip2);

	    List<Trip> trips = tripService.getAllTrips();

	    assertNotNull(trips);
	    assertTrue(trips.size() >= 2);

	    assertTrue(trips.stream().anyMatch(t -> "trip-1".equals(t.getTripId())));
	    assertTrue(trips.stream().anyMatch(t -> "trip-2".equals(t.getTripId())));

	    FirebaseDatabase.getInstance().getReference("trips").child("trip-1").removeValueAsync().get();
	    FirebaseDatabase.getInstance().getReference("trips").child("trip-2").removeValueAsync().get();
	}
	
}
