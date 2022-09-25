package com.example.weather.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Location(val location: LocationData)

@JsonClass(generateAdapter = true)
data class LocationData(
    var name: String,
    var region: String,
    var country: String,
    var lat: Double,
    var lon: Double,
    var tz_id: String,
    var localtime_epoch: Float,
    var localtime: String
)