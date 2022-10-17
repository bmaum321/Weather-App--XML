package com.brian.weather.repository

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.brian.weather.data.WeatherDatabase
import com.brian.weather.domain.WeatherDomainObject
import com.brian.weather.domain.asDomainModel
import com.brian.weather.model.ForecastContainer
import com.brian.weather.model.Search
import com.brian.weather.network.ApiResponse
import com.brian.weather.network.WeatherApi
import com.brian.weather.network.WeatherContainer
import com.brian.weather.network.handleApi

class WeatherRepository(private val database: WeatherDatabase) {

    // The only thing we should be storing into the database is zipcode and city name, everything
    // else is dynamic

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


    suspend fun getWeatherListForZipCodes(zipcodes: List<String>, resources: Resources, sharedPreferences: SharedPreferences): List<WeatherDomainObject> {
        val weatherDomainObjects = mutableListOf<WeatherDomainObject>()
        zipcodes.forEach { zipcode ->
            weatherDomainObjects.add(getWeather(zipcode, resources, sharedPreferences))
        }
        return weatherDomainObjects
    }
}