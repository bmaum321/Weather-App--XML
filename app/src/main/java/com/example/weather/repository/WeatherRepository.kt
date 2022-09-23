package com.example.weather.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asLiveData
import com.example.weather.data.WeatherDatabase
import com.example.weather.domain.WeatherDomainObject
import com.example.weather.domain.asDomainModel
import com.example.weather.network.WeatherApi
import com.example.weather.network.WeatherContainer
import com.example.weather.network.asDatabaseModel
import com.example.weather.model.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val database: WeatherDatabase) {

    // The only thing we should be storing into the database is zipcode and city name, everything
    // else is dynamic
    suspend fun storeNetworkWeatherInDatabase(zipcode: String) {
        withContext(Dispatchers.IO) {
            val weatherData = WeatherApi.retrofitService.getWeather(zipcode)
            database.weatherDao().insert(weatherData.asDatabaseModel(zipcode))
        }
    }

    suspend fun getWeather(zipcode: String): WeatherDomainObject {
        val weatherData: WeatherContainer = WeatherApi.retrofitService.getWeather(zipcode)
        return weatherData.asDomainModel(zipcode)
    }

    //TODO need to create a function that calls the API for each zip code and returns a list of
    // Weather domain objects for the main screen


    suspend fun getWeatherListForZipCodes(zipcodes: List<String>): List<WeatherDomainObject> {
        val weatherDomainObjects = mutableListOf<WeatherDomainObject>()
        zipcodes.forEach { zipcode ->
            weatherDomainObjects.add(getWeather(zipcode))
        }
        return weatherDomainObjects
    }


    /**
     * Note: LiveData is retained in this example for simplicity. In general, it's recommended
     * to use Flow with repositories as it's independent of the lifecycle.
     */
    //I think transformations.map only works with live data, need to transform flow
    //We need to change the data base structure to map the data in the database to the domain entity
    //which is just a regular kotlin class

    /**
     * This live data obkect should be updated automatically when the database is updated
     */

    val weatherDomainObjects: LiveData<List<WeatherDomainObject>> =
        Transformations.map(database.weatherDao().getWeatherLocations().asLiveData()) {
            it.asDomainModel()
        }

}