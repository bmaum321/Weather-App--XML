package com.example.weather.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET

/**
 * A retrofit service to fetch the weather data from the API
 */

interface WeatherService {
    // TODO this needs to be updated to take paramaters
    @GET("/current.json?key=759618142cff4efb89d192409221909 &q=13088&aqi=no")
    suspend fun getWeather(): WeatherContainer
}

/**
 * Main entry point for network access
 */

object WeatherNetwork {

    // Configure retrofit to parse JSON and use coroutines
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.weatherapi.com/v1")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val weather = retrofit.create(WeatherService::class.java)
}