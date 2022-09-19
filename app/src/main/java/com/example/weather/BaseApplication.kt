package com.example.weather

import android.app.Application
import com.example.weather.data.WeatherDatabase

/**
 * An application class that inherits from [Application], allows for the creation of a singleton
 * instance of the [WeatherDatabase]
 */
class BaseApplication : Application() {

    // TODO: provide a WeatherDatabase value by lazy here
    val database: WeatherDatabase by lazy { WeatherDatabase.getDatabase(this) }
}