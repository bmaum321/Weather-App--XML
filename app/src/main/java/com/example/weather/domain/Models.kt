package com.example.weather.domain

import com.example.weather.model.CurrentWeatherData
import com.example.weather.model.LocationData


/**
 * Domain objects are plain Kotlin data classes that represent the things in our app. These are the
 * objects that should be displayed on screen, or manipulated by the app.
 *
 * @see database for objects that are mapped to the database
 * @see network for objects that parse or prepare network calls
 */


data class WeatherDomainObject(val location: LocationData,
                               val current: CurrentWeatherData,
                       )

