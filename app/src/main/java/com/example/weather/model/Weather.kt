package com.example.weather.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Weather entity to be stored in the forageable_database.
 */
// TODO: annotate this data class as an entity with a parameter for the table name
@Entity(tableName = "weather_database")
data class Weather(
    // TODO: declare the id to be an autogenerated primary key
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String,
    // TODO: make a custom column name for the inSeason variable that follows SQL column name
    //  conventions (the column name should be in_season)
    @ColumnInfo(name = "in_season")
    val inSeason: Boolean,
    val notes: String?
)
