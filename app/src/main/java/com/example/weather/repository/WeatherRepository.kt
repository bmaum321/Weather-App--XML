package com.example.weather.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asLiveData
import com.example.weather.data.WeatherDatabase
import com.example.weather.domain.WeatherDomainObject
import com.example.weather.domain.asDomainModel
import com.example.weather.model.ForecastContainer
import com.example.weather.model.asDomainModel
import com.example.weather.network.*
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

    suspend fun getWeatherWithErrorHandling(zipcode: String): ApiResponse<WeatherContainer> = handleApi { WeatherApi.retrofitService.getWeatherWithErrorHandling(zipcode) }

    suspend fun getForecast(zipcode: String): ApiResponse<ForecastContainer> = handleApi { WeatherApi.retrofitService.getForecast(zipcode) }

    suspend fun getWeather(zipcode: String): WeatherDomainObject {
        val weatherData: WeatherContainer = WeatherApi.retrofitService.getWeather(zipcode)
        return weatherData
            .asDomainModel(zipcode)
    }

    //TODO need to create a function that calls the API for each zip code and returns a list of
    // Weather domain objects for the main screen

    suspend fun getWeatherListForZipCodesWithErrorHandling(zipcodes: List<String>): List<ApiResponse<WeatherContainer>> {
        val weatherApiResponses = mutableListOf<ApiResponse<WeatherContainer>>()
        zipcodes.forEach { zipcode ->
            weatherApiResponses.add(getWeatherWithErrorHandling(zipcode))
        }
        return weatherApiResponses
    }



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

    /**
     * This live data obkect should be updated automatically when the database is updated
     */

    val weatherDomainObjects: LiveData<List<WeatherDomainObject>> =
        Transformations.map(database.weatherDao().getWeatherLocations().asLiveData()) {
            it.asDomainModel()
        }

}