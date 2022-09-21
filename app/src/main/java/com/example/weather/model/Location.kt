package com.example.weather.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Location(val location: LocationData)