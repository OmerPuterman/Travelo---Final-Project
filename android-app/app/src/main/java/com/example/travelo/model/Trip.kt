package com.example.travelo.model

data class Trip(
    val tripId: String? = null,
    val guideId: String? = null,
    val destination: String,
    val startDate: String? = null,
    val budget: Double,
    val maxTimeMinutes: Int,
    val startLocation: String,
    val endLocation: String,
    val startTime: String,
    val numberOfTravelers: Int = 1
)