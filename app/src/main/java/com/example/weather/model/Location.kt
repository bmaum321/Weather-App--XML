package com.example.weather.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Location(val location: LocationData)

@JsonClass(generateAdapter = true)
data class LocationData(
    val name: String,
    val region: String,
    var country: String,
    val lat: Double,
    val lon: Double,
    val tz_id: String,
    val localtime_epoch: Long,
    var localtime: String
)