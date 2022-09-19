package com.example.weather.model

data class CurrentWeatherData(
    val temp_c: Double,
    val temp_f: Double,
    val is_day: Int,
    val wind_mph: Double,
    val wind_dir: String,
    val uv: Double,
    val humidity: Int,
    val feelslike_f: Double,
    val condition: Condition
)