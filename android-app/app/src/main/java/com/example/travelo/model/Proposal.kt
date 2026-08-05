package com.example.travelo.model

data class Proposal(
    val id: String,
    val type: String,           // <-- NEW: e.g., "BUSINESS" or "PUBLIC"
    val lat: Double,
    val lon: Double,
    val cost: Double,
    val distanceMeters: Double,
    val activityTime: Double,
    val profit: Double
)