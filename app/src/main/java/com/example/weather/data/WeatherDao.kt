package com.example.weather.data

import androidx.room.*
import com.example.weather.model.WeatherEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for database interaction.
 */
@Dao
interface WeatherDao {



    //method to retrieve all Weather object statically
    @Query("SELECT zipCode FROM weather_database")
    fun getZipcodesFlow(): Flow<List<String>>

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
}
