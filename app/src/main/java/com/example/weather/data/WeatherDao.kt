package com.example.weather.data

import androidx.room.*
import com.example.weather.model.Weather
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for database interaction.
 */
@Dao
interface WeatherDao {

    // TODO: implement a method to retrieve all Weather objects from the database
    @Query("SELECT * FROM weather_database ORDER BY name ASC")
    fun getWeatherLocations(): Flow<List<Weather>>

    // TODO: implement a method to retrieve a Weather from the database by id
    @Query("SELECT * FROM weather_database WHERE id = :id")
    fun getWeatherById(id: Long): Flow<Weather>

    // TODO: implement a method to insert a Weather into the database
    //  (use OnConflictStrategy.REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(forageable: Weather)

    // TODO: implement a method to update a Weather that is already in the database
    @Update
    suspend fun update(forageable: Weather)

    // TODO: implement a method to delete a Weather from the database.
    @Delete
    suspend fun delete(forageable: Weather)
}
