package com.brian.weather.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Weather entity to be stored in the weather_database.
 */
@Entity(tableName = "weather_database")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cityName: String,
    val zipCode: String,
    val sortOrder: Int
)

