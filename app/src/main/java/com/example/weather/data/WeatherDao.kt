package com.example.weather.data

import androidx.room.*
import com.example.weather.model.Weather
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for database interaction.
 */
@Dao
interface WeatherDao {

    // method to retrieve all Weather objects from the database
    @Query("SELECT * FROM weather_database ORDER BY name ASC")
    fun getWeatherLocations(): Flow<List<Weather>>

    // method to retrieve a Weather from the database by id
    @Query("SELECT * FROM weather_database WHERE id = :id")
    fun getWeatherById(id: Long): Flow<Weather>

    // method to insert a Weather into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(forageable: Weather)

    // method to update a Weather that is already in the database
    @Update
    suspend fun update(forageable: Weather)

    // method to delete a Weather from the database.
    @Delete
    suspend fun delete(forageable: Weather)
}
