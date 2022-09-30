package com.example.weather.model

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Search(
    val id: Int,
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val url: String,
)
