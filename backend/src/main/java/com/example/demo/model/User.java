package com.example.demo.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String userId;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    // Algorithm preferences
    private double weightDistance = 0.0001;
    private double weightTime = 0.01;
    private double weightCost = 0.1;
    private double weightProfit = 5.0;

    private Role role; // GUIDE, BUSINESS, ADMIN

    public enum Role {
        GUIDE,
        BUSINESS,
        ADMIN,
        TRAVELER
    }
}