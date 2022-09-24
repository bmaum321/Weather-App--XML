package com.example.weather.network

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


@JsonClass(generateAdapter = true)
data class WeatherContainer(
    val location: LocationData,
    val current: CurrentWeatherData
)

/**
 * Convert Network results to database objects
 */
fun WeatherContainer.asDatabaseModel(zipcode: String): WeatherEntity {

    return WeatherEntity(
        zipCode = zipcode,
        cityName = location.name,
        temp = current.temp_f,
        imgSrcUrl = current.condition.icon,
        windDirection = current.wind_dir,
        windMph = current.wind_mph,
        conditionText = current.condition.text
    )

}







