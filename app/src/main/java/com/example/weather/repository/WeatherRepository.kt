package com.example.weather.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.weather.data.WeatherDatabase
import com.example.weather.domain.WeatherDomainObject
import com.example.weather.network.WeatherApi
import com.example.weather.network.WeatherContainer
import com.example.weather.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WeatherRepository(private val database: WeatherDatabase) {
    suspend fun refreshWeather(zipcode: String){
        withContext(Dispatchers.IO){
            val weatherData = WeatherApi.retrofitService.getWeather(zipcode)
            database.weatherDao().insert(weatherData.asDatabaseModel(zipcode))
        }
    }

    /**
     * Note: LiveData is retained in this example for simplicity. In general, it's recommended
     * to use Flow with repositories as it's independent of the lifecycle.
     */
    //I think transformations.map only works with live data, need to transform flow
    //We need to change the data base structure to map the data in the database to the domain entity
    //which is just a regular kotlin class

    val weather: Flow<WeatherDomainObject> = Transformations.map(database.weatherDao().getWeatherLocations()) {
        it
    }


}