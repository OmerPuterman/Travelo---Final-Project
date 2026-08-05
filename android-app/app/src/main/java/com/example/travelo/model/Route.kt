package com.example.travelo.model

data class Route(
    val routeId: String,
    val tripId: String,
    val stops: List<Stop>,
    val totalCost: Double,
    val totalTime: Double
) {
    data class Stop(
        val order: Int,
        val proposalId: String,
        val description: String,
        val arrivalTime: String,
        val lat: Double, // <-- NEW
        val lon: Double  // <-- NEW
    )
}