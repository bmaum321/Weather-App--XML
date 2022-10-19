package com.brian.weather.model

import com.squareup.moshi.JsonClass


//TODO everything in data class should be vals, need to clean this up and do formatting
// at the last presentation layer

@JsonClass(generateAdapter = true)
data class ForecastContainer(val forecast: ForecastDay,
                             val alerts: AlertList )

@JsonClass(generateAdapter = true)
data class ForecastDay(
    val forecastday: List<Day>
)

data class AlertList(
    val alert: List<Alert>
)

@JsonClass(generateAdapter = true)
data class Alert(
    val headline: String,
    val category: String,
    val severity: String,
    val event: String,
    val effective: String,
    val expires: String,
    var desc: String
)

@JsonClass(generateAdapter = true)
data class Day(
    var date: String,
    val day: ForecastForDay,
    val hour: MutableList<Hours> //needed to delete hours in past in Models.kt
)

@JsonClass(generateAdapter = true)
data class ForecastForDay(
    val condition: Condition,
    val avgtemp_f: Double,
    val maxtemp_f: Double,
    val mintemp_f: Double,
    val avgtemp_c: Double,
    val maxtemp_c: Double,
    val mintemp_c: Double,
    val daily_chance_of_rain: Double
)

data class Hours(
    val time_epoch: Int,
    var time: String, //TODO clean this up
    val temp_f: Double,
    val temp_c: Double,
    val is_day: Int,
    val condition: Condition,
    val wind_mph: Double,
    val wind_kph: Double,
    val wind_dir: String,
    val chance_of_rain: Int,
    val pressure_mb: Double,
    val pressure_in: Double,
    val will_it_rain: Int,
    val chance_of_snow: Double,
    val will_it_snow: Int,
    val precip_mm: Double,
    val precip_in: Double,
    val feelslike_c: Double,
    val feelslike_f: Double,
    val windchill_c: Double,
    val windchill_f: Double
)