package com.example.weather.model

import com.squareup.moshi.JsonClass

/*
@JsonClass(generateAdapter = true)
data class ForecastContainer(
    val location: Location,
    val current: CurrentWeatherData,
    val forecast: Forecast)

 */


@JsonClass(generateAdapter = true)
data class ForecastContainer(val forecast: ForecastDay)

@JsonClass(generateAdapter = true)
data class ForecastDay(val forecastday: List<Days>) // another list of "hour" under forecast day

@JsonClass(generateAdapter = true)
data class Days(
    val date: String,
    val day: ForecastForDay,
    val hour: List<Hour>
)

@JsonClass(generateAdapter = true)
data class ForecastForDay(
    val condition: Condition,
    val avgtemp_f: Double,
    val maxtemp_f: Double,
    val mintemp_f: Double,
    val daily_will_it_rain: Double
)

data class Hour(
    val time: String,
    val temp_f: Double,
    val is_day: Int,
    val condition: Condition,
    val wind_mph: Double,
    val wind_dir: String
)