package com.example.weather.model

import com.squareup.moshi.JsonClass

data class Forecast(val forecast: ForecastDay)

@JsonClass(generateAdapter = true)
data class ForecastDay(val forecastday: List<Days>) // another list of "hour" under forecast day

@JsonClass(generateAdapter = true)
data class Days(
    val date: String,
    val day: ForecastForDay,
    val hour: List<CurrentWeatherData>
)

@JsonClass(generateAdapter = true)
data class ForecastForDay(
    val condition: Condition,
    val avgtemp_f: Double
)