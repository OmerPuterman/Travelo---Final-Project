package com.example.travelo.model

import com.google.gson.annotations.SerializedName

data class MarketplaceOffer(
    @SerializedName("proposalId")
    val proposalId: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("price")
    val price: Double = 0.0,

    @SerializedName("location")
    val location: String = "",

    @SerializedName("status")
    val status: String = "",
    @SerializedName("lat")
    val lat: Double = 0.0,

    @SerializedName("lng")
    val lng: Double = 0.0,

    @SerializedName("startTime")
    val startTime: String = "",

    @SerializedName("endTime")
    val endTime: String = "",

    @SerializedName("openTime")
    val openTime: String = "",

    @SerializedName("closeTime")
    val closeTime: String = "",

    @SerializedName("durationMinutes")
    val durationMinutes: Double = 0.0
)