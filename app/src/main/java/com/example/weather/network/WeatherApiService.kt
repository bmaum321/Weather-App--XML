package com.example.weather.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

/**
 * A retrofit service to fetch the weather data from the API
 */

private const val BASE_URL ="https://api.weatherapi.com/v1/"

/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// Configure retrofit to parse JSON and use coroutines
private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

interface WeatherApiService {
    // TODO this needs to be updated to take paramaters
    @GET("current.json?key=759618142cff4efb89d192409221909 &q=13088&aqi=no")
    suspend fun getWeather(): WeatherContainer
}

/**
 * Main entry point for network access
 */

object WeatherApi {
    val retrofitService: WeatherApiService by lazy  { retrofit.create(WeatherApiService::class.java) }
}