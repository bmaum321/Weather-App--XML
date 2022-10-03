package com.example.weather.repository

import android.content.SharedPreferences
import android.content.res.Resources
import com.example.weather.data.WeatherDatabase
import com.example.weather.domain.WeatherDomainObject
import com.example.weather.domain.asDomainModel
import com.example.weather.model.ForecastContainer
import com.example.weather.model.Search
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

    suspend fun getWeatherWithErrorHandling(zipcode: String): ApiResponse<WeatherContainer> = handleApi {
        WeatherApi.retrofitService.getWeatherWithErrorHandling(zipcode)
    }

    suspend fun getForecast(zipcode: String): ApiResponse<ForecastContainer> = handleApi {
        WeatherApi.retrofitService.getForecast(zipcode)
    }

    suspend fun getSearchResults(location: String): ApiResponse<List<Search>> = handleApi {
        WeatherApi.retrofitService.locationSearch(location)
    }


    suspend fun getWeather(zipcode: String, resources: Resources, sharedPreferences: SharedPreferences): WeatherDomainObject {
        val weatherData: WeatherContainer = WeatherApi.retrofitService.getWeather(zipcode)
        return weatherData
            .asDomainModel(zipcode, resources, sharedPreferences)
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

    suspend fun getWeatherListForZipCodes(zipcodes: List<String>, resources: Resources, sharedPreferences: SharedPreferences): List<WeatherDomainObject> {
        val weatherDomainObjects = mutableListOf<WeatherDomainObject>()
        zipcodes.forEach { zipcode ->
            weatherDomainObjects.add(getWeather(zipcode, resources, sharedPreferences))
        }
        return weatherDomainObjects
    }

}