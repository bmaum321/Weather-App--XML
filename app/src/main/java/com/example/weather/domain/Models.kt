package com.example.weather.domain

import androidx.lifecycle.Transformations.map
import com.example.weather.model.CurrentWeatherData
import com.example.weather.model.LocationData
import com.example.weather.model.WeatherEntity
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
    val code: Int?, //TDOO new
    val message: String?
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
        code = null,
        message = null
    )
}

