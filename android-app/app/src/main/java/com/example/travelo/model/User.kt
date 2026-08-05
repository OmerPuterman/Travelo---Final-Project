package com.example.travelo.model

data class User(
    val userId: String,
    val name: String,
    val email: String,
    val role: String // MUST be "GUIDE", "BUSINESS", or "TRAVELER"
)