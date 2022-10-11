package com.brian.weather.data

import androidx.room.*
import com.brian.weather.model.WeatherEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for database interaction.
 */
@Dao
interface WeatherDao {

    // method to retrieve all zipcodes from database and order them by sort order ascending
    @Query("SELECT zipCode FROM weather_database ORDER BY sortOrder ASC")
    fun getZipcodesFlow(): Flow<List<String>>

    // method to retrieve all weather entities from database
    @Query("SELECT * FROM weather_database ORDER BY sortOrder ASC")
    fun getAllWeatherEntities(): List<WeatherEntity>

    // method to retrieve a Weather from the database by id
    @Query("SELECT * FROM weather_database WHERE id = :id")
    fun getWeatherById(id: Long): Flow<WeatherEntity>

    // method to retrieve a Weather from the database by zipcode
    @Query("SELECT * FROM weather_database WHERE zipCode = :zipcode")
    fun getWeatherByZipcode(zipcode: String): Flow<WeatherEntity>

    // method to retrieve a Weather from the database by location and return as object
    @Query("SELECT * FROM weather_database WHERE zipCode = :location")
    fun getWeatherByLocation(location: String): WeatherEntity

    // method to insert a Weather into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weatherEntity: WeatherEntity)

    // method to update a Weather that is already in the database
    @Update
    suspend fun update(weatherEntity: WeatherEntity)

    // method to delete a Weather from the database.
    @Delete
    suspend fun delete(weatherEntity: WeatherEntity)

    // method to retrieve last entry in the table
    @Query("SELECT * FROM weather_database ORDER BY ID DESC LIMIT 1")
    fun selectLastEntry(): WeatherEntity

    //Delete all table entries
    @Query("DELETE FROM weather_database")
    suspend fun deleteAll()

    //Insert all objects into database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(weatherEntityList: List<WeatherEntity>)

    // Check if database is empty
    @Query("SELECT (SELECT COUNT(*) FROM weather_database) == 0")
    fun isEmpty(): Boolean
}
