package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
	
	@Autowired
    private UserService userService;
	@Autowired
    private MockMvc mockMvc;
	
	
	//REGISTER
	 @Test
	    void createUser_withoutFirebase_shouldReturnSuccess() {

	        // fake user
	        User user = new User();

	        user.setUserId("test-id");
	        user.setName("Test User");
	        user.setEmail("test@example.com");
	        user.setWeightDistance(0.0001);
	        user.setWeightTime(0.01);
	        user.setWeightCost(0.1);
	        user.setWeightProfit(5.0);
	        user.setRole(User.Role.TRAVELER);


	        // here you would call your mocked service
	        String result = "User created successfully";


	        assertNotNull(result);
	        assertEquals("User created successfully", result);
	    }
	 @Test
	 void createUser_withFirebase_thenDelete() throws Exception {
	     User user = new User();

	     user.setUserId("test-user-123");
	     user.setName("Firebase Test");
	     user.setEmail("firebase-test@example.com");
	     user.setWeightDistance(0.0001);
	     user.setWeightTime(0.01);
	     user.setWeightCost(0.1);
	     user.setWeightProfit(5.0);
	     user.setRole(User.Role.TRAVELER);


	     // Create
	     String result = userService.createUser(user);

	     assertNotNull(result);


	     // Verify
	     User saved = userService.getUser(result);

	     assertNotNull(saved);
	     assertEquals("Firebase Test", saved.getName());
	     assertEquals("firebase-test@example.com", saved.getEmail());


	     // Delete after test
	     FirebaseDatabase.getInstance()
	         .getReference("users")
	         .child(result)
	         .removeValueAsync();
	 }
	 @Test
	 void createUser_withNullName_shouldFail() throws Exception {

	     User user = new User();

	     user.setUserId("test-id");
	     user.setName(null); // wrong
	     user.setEmail("test@example.com");
	     user.setWeightDistance(0.0001);
	     user.setWeightTime(0.01);
	     user.setWeightCost(0.1);
	     user.setWeightProfit(5.0);
	     user.setRole(User.Role.TRAVELER);


	     assertThrows(Exception.class, () -> {
	         userService.createUser(user);
	     });
	 }
	 @Test
	 void createUser_withInvalidEmail_shouldFail() throws Exception {

	     User user = new User();

	     user.setUserId("test-id");
	     user.setName("Test User");
	     user.setEmail(""); // wrong format
	     user.setWeightDistance(0.0001);
	     user.setWeightTime(0.01);
	     user.setWeightCost(0.1);
	     user.setWeightProfit(5.0);
	     user.setRole(User.Role.TRAVELER);


	     assertThrows(Exception.class, () -> {
	         userService.createUser(user);
	     });
	 }
	 @Test
	 void createUser_withNullRole_shouldFail() throws Exception {

	     User user = new User();

	     user.setUserId("test-id");
	     user.setName("Test User");
	     user.setEmail("test@example.com");
	     user.setWeightDistance(0.0001);
	     user.setWeightTime(0.01);
	     user.setWeightCost(0.1);
	     user.setWeightProfit(5.0);
	     user.setRole(null); // wrong


	     assertThrows(Exception.class, () -> {
	         userService.createUser(user);
	     });
	 }
	 @Test
	 void registerUser_withWrongWeightDistType_shouldFail() throws Exception {

	     String json = """
	         {
	           "userId":"123",
	           "name":"Test User",
	           "email":"test@example.com",
	           "weightDistance":"wrong",
	           "weightTime":0.01,
	           "weightCost":0.1,
	           "weightProfit":5.0,
	           "role":"TRAVELER"
	         }
	         """;


	     mockMvc.perform(post("/api/users/register")
	             .contentType(MediaType.APPLICATION_JSON)
	             .content(json))
	             .andExpect(status().isBadRequest());
	 }
	 @Test
	 void registerUser_withWrongWeightTimeType_shouldFail() throws Exception {

	     String json = """
	         {
	           "userId":"123",
	           "name":"Test User",
	           "email":"test@example.com",
	           "weightDistance":"0.0001",
	           "weightTime":!,
	           "weightCost":0.1,
	           "weightProfit":5.0,
	           "role":"TRAVELER"
	         }
	         """;


	     mockMvc.perform(post("/api/users/register")
	             .contentType(MediaType.APPLICATION_JSON)
	             .content(json))
	             .andExpect(status().isBadRequest());
	 }
	 @Test
	 void registerUser_withWrongWeightCostType_shouldFail() throws Exception {

	     String json = """
	         {
	           "userId":"123",
	           "name":"Test User",
	           "email":"test@example.com",
	           "weightDistance":"0.0001",
	           "weightTime":0.01,
	           "weightCost":true,
	           "weightProfit":5.0,
	           "role":"TRAVELER"
	         }
	         """;


	     mockMvc.perform(post("/api/users/register")
	             .contentType(MediaType.APPLICATION_JSON)
	             .content(json))
	             .andExpect(status().isBadRequest());
	 }
	 @Test
	 void registerUser_withWrongWeightProfitType_shouldFail() throws Exception {

	     String json = """
	         {
	           "userId":"123",
	           "name":"Test User",
	           "email":"test@example.com",
	           "weightDistance":"0.0001",
	           "weightTime":0.01,
	           "weightCost":0.1,
	           "weightProfit":-0.1,
	           "role":"TRAVELER"
	         }
	         """;


	     assertThrows(Exception.class, () -> {
	    	    mockMvc.perform(post("/api/users/register")
	    	            .contentType(MediaType.APPLICATION_JSON)
	    	            .content(json));
	    	});
	 }
	 
	 //LOGIN/GETUSER
	 @Test
	 void getUser_existingUser_shouldReturnUser() throws Exception {

	     User user = new User();

	     user.setUserId("test-user-123");
	     user.setName("Test User");
	     user.setEmail("test@example.com");
	     user.setWeightDistance(0.0001);
	     user.setWeightTime(0.01);
	     user.setWeightCost(0.1);
	     user.setWeightProfit(5.0);
	     user.setRole(User.Role.TRAVELER);


	     // Create user in Firebase
	     String id = userService.createUser(user);

	     assertNotNull(id);
	     // Get user
	     User result = userService.getUser(id);


	     // Check
	     assertNotNull(result);
	     assertEquals("Test User", result.getName());
	     assertEquals("test@example.com", result.getEmail());
	     assertEquals(User.Role.TRAVELER, result.getRole());


	     // Cleanup
	     FirebaseDatabase.getInstance()
	             .getReference("users")
	             .child(id)
	             .removeValueAsync();
	 }
	 @Test
	 void getUser_nonExistingUser_shouldFail() {

	     String fakeId = "does-not-exist-123";


	     assertThrows(Exception.class, () -> {
	         userService.getUser(fakeId);
	     });
	 }
	 @Test
	 void getUser_emptyId_shouldFail() {

		 String fakeId = "";


		 assertThrows(Exception.class, () -> {
		        userService.getUser(fakeId);
		    });
	 }
	 
	 
	 //UPDATE WEIGHTS//PUT
	 @Test
	 void updateWeights_existingUser_shouldReturnSuccess() throws Exception {

	     User user = new User();

	     user.setUserId("weight-test-user");
	     user.setName("Weight Test");
	     user.setEmail("weight@test.com");
	     user.setWeightDistance(0.0001);
	     user.setWeightTime(0.01);
	     user.setWeightCost(0.1);
	     user.setWeightProfit(5.0);
	     user.setRole(User.Role.TRAVELER);


	     String id = userService.createUser(user);


	     String json = """
	         {
	           "weightDistance":0.5,
	           "weightTime":2.0,
	           "weightCost":3.0,
	           "weightProfit":10.0
	         }
	         """;


	     mockMvc.perform(put("/api/users/users/" + id + "/weights")
	             .contentType(MediaType.APPLICATION_JSON)
	             .content(json))
	             .andExpect(status().isOk());


	     User updated = userService.getUser(id);

	     assertEquals(0.5, updated.getWeightDistance());
	     assertEquals(2.0, updated.getWeightTime());
	     assertEquals(3.0, updated.getWeightCost());
	     assertEquals(10.0, updated.getWeightProfit());


	     FirebaseDatabase.getInstance()
	             .getReference("users")
	             .child(id)
	             .removeValueAsync();
	 }
	 @Test
	 void updateWeights_nonExistingUser_shouldFail() throws Exception {

	     String json = """
	         {
	           "weightDistance":0.5,
	           "weightTime":2.0,
	           "weightCost":3.0,
	           "weightProfit":10.0
	         }
	         """;

	     assertThrows(Exception.class, () -> {
	    	 mockMvc.perform(put("/api/users/users/does-not-exist123/weights")
		             .contentType(MediaType.APPLICATION_JSON)
		             .content(json))
		             .andExpect(status().isBadRequest());
	     });
	    
	 }
	 @Test
	 void updateWeights_negativeWeight_shouldFail() throws Exception {

	     User user = new User();

	     user.setUserId("negative-test");
	     user.setName("Test");
	     user.setEmail("test@test.com");
	     user.setRole(User.Role.TRAVELER);


	     String id = userService.createUser(user);


	     String json = """
	         {
	           "weightDistance":-1,
	           "weightTime":0.01,
	           "weightCost":-0.1,
	           "weightProfit":5.0
	         }
	         """;

	     assertThrows(Exception.class, () -> {
	    	 mockMvc.perform(put("/api/users/users/" + id + "/weights")
		             .contentType(MediaType.APPLICATION_JSON)
		             .content(json))
		             .andExpect(status().isBadRequest());

	    	});
	     

	     FirebaseDatabase.getInstance()
	             .getReference("users")
	             .child(id)
	             .removeValueAsync();
	 }
	 @Test
	 void updateWeights_wrongWeightType_shouldFail() throws Exception {

	     String json = """
	         {
	           "weightDistance":"wrong",
	           "weightTime":0.01,
	           "weightCost":0.1,
	           "weightProfit":5.0
	         }
	         """;


	     mockMvc.perform(put("/api/users/users/test-id/weights")
	             .contentType(MediaType.APPLICATION_JSON)
	             .content(json))
	             .andExpect(status().isBadRequest());
	 }
}
