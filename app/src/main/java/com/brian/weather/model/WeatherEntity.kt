package com.brian.weather.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Weather entity to be stored in the weather_database.
 */
// TODO: annotate this data class as an entity with a parameter for the table name
@Entity(tableName = "weather_database")
data class WeatherEntity(
    // TODO: declare the id to be an autogenerated primary key
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cityName: String,
    val zipCode: String,
    val sortOrder: Int
)
