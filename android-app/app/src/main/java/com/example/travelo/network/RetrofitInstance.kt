package com.example.travelo.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory // <-- NEW IMPORT

object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    val api: TraveloApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create()) // <-- ADD THIS FIRST to handle raw Strings
            .addConverterFactory(GsonConverterFactory.create())    // <-- Then handle JSON objects
            .build()
            .create(TraveloApi::class.java)
    }
}