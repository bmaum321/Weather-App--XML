package com.brian.weather.model

import com.squareup.moshi.JsonClass

/*
@JsonClass(generateAdapter = true)
data class ForecastContainer(
    val location: Location,
    val current: CurrentWeatherData,
    val forecast: Forecast)

 */


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
    val desc: String
)

@JsonClass(generateAdapter = true)
data class Day(
    var date: String,
    val day: ForecastForDay,
    val hour: MutableList<Hours> //trying to make mutable to manipulate api data
)

@JsonClass(generateAdapter = true)
data class ForecastForDay(
    val condition: Condition,
    val avgtemp_f: Double,
    var maxtemp_f: Double,
    var mintemp_f: Double,
    val avgtemp_c: Double,
    var maxtemp_c: Double,
    var mintemp_c: Double,
    val daily_chance_of_rain: Double
)

data class Hours(
    val time_epoch: Int,
    var time: String,
    var temp_f: Double,
    val temp_c: Double,
    val is_day: Int,
    val condition: Condition,
    var wind_mph: Double,
    val wind_kph: Double,
    val wind_dir: String,
    val chance_of_rain: Int,
    val pressure_mb: Double,
    var pressure_in: Double,
    val will_it_rain: Int,
    val chance_of_snow: Double,
    val will_it_snow: Int,
    val precip_mm: Double,
    var precip_in: Double,
    val feelslike_c: Double,
    var feelslike_f: Double,
    val windchill_c: Double,
    var windchill_f: Double
)