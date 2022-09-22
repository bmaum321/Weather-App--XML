package com.example.weather.network

import androidx.lifecycle.Transformations.map
import com.example.weather.domain.WeatherDomainObject
import com.example.weather.model.CurrentWeatherData
import com.example.weather.model.LocationData
import com.example.weather.model.WeatherEntity
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
 *
 */
@JsonClass(generateAdapter = true)
data class WeatherObject(
    val location: String,
    val current: String,
)

/**
 * Convert Network results to domain objects
 */


/**
 * Convert Network results to database objects
 */
fun WeatherContainer.asDatabaseModel(zipcode: String): WeatherEntity {

    return WeatherEntity(
        cityName = location.name,
        tempf = current.temp_f,
        notes = null, //TODO need to get rid of this from database and views
        zipCode = zipcode
    )


}







