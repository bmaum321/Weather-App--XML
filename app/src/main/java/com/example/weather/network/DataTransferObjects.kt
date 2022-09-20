package com.example.weather.network

import com.example.weather.domain.WeatherDomainObject
import com.example.weather.model.CurrentWeatherData
import com.example.weather.model.LocationData
import com.example.weather.model.Weather
import com.squareup.moshi.JsonClass

/**
 * DataTransferObjects go in this file. These are responsible for parsing responses from the server
 * or formatting objects to send to the server. You should convert these to domain objects before
 * using them.
 *
 * @see domain package for
 */

/**
 * VideoHolder holds a list of Videos.
 *
 * This is to parse first level of our network result which looks like
 *
 * {
 *   "videos": []
 * }
 */
@JsonClass(generateAdapter = true)
data class WeatherContainer(
    val location: LocationData,
    val current: CurrentWeatherData
)

/**
 * Videos represent a devbyte that can be played.
 */
@JsonClass(generateAdapter = true)
data class WeatherObject(
    val location: String,
    val current: String,
)

/*
/**
 * Convert Network results to database objects
 */
fun WeatherContainer.asDomainModel(): List<WeatherDomainObject> {
    return WeatherContainer.map {
        WeatherDomainObject(
            location = it.location,
            current = it.current,
        )
    }
}




/**
 * Convert Network results to database objects
 */
fun WeatherContainer.asDatabaseModel(): List<Weather> {

    return location.map {
        Weather(
            cityName = it.name,
            zipCode = it.tz_id,
            notes = it.region)
    }
}
 */




