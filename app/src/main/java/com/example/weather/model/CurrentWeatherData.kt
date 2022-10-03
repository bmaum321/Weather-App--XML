package com.example.weather.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CurrentWeatherData(
    val temp_c: Double,
    val temp_f: Double,
    val is_day: Int,
    val wind_mph: Double,
    val wind_kph: Double,
    val wind_dir: String,
    val uv: Double,
    val humidity: Int,
    val feelslike_f: Double,
    val condition: Condition
)

@JsonClass(generateAdapter = true)
data class Condition(
    val text: String,
    val icon: String,
    val code: Int
)