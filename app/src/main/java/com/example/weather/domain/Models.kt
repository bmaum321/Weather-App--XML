package com.example.weather.domain

import com.example.weather.model.Days
import com.example.weather.model.ForecastContainer
import com.example.weather.model.ForecastDay
import com.example.weather.model.Hours
import com.example.weather.network.WeatherContainer


/**
 * Domain objects are plain Kotlin data classes that represent the things in our app. These are the
 * objects that should be displayed on screen, or manipulated by the app.
 *
 * @see database for objects that are mapped to the database
 * @see network for objects that parse or prepare network calls
 */


data class WeatherDomainObject(
    val location: String,
    val tempf: Double?,
    val zipcode: String,
    val imgSrcUrl: String,
    val conditionText: String,
    val windMph: Double,
    val windDirection: String,

)

// TODO do I need to destructure this into further data class to use with the list adapter?
data class ForecastDomainObject(
    val days: List<Days>
)

data class HourlyForecastDomainObject(
    val hours: List<Hours>
)


fun WeatherContainer.asDomainModel(zipcode: String): WeatherDomainObject {
    return WeatherDomainObject(
        location = location.name,
        zipcode = zipcode,
        tempf = current.temp_f,
        imgSrcUrl = current.condition.icon,
        conditionText = current.condition.text,
        windMph = current.wind_mph,
        windDirection = current.wind_dir,
    )
}

fun ForecastContainer.asDomainModel(): ForecastDomainObject {
    return ForecastDomainObject(
        days = forecast.forecastday
    )
}

fun Days.asDomainModel(): HourlyForecastDomainObject {
    return HourlyForecastDomainObject(
            hours = hour
            )
}

