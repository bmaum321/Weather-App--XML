package com.example.weather.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.weather.data.WeatherDao
import com.example.weather.data.WeatherDatabase.Companion.getDatabase
import com.example.weather.model.WeatherEntity
import com.example.weather.repository.WeatherRepository


/**
 * [ViewModel] to provide data to the [WeatherLocationDetailFragment]
 */

// Pass an application as a parameter to the viewmodel constructor which is the contect passed to the singleton database object
class WeatherDetailViewModel(private val weatherDao: WeatherDao, application: Application) : AndroidViewModel(application) {

    //The data source this viewmodel will fetch results from
    private val weatherRepository = WeatherRepository(getDatabase(application))

    // A list of weather results for the list screen
    val weatherList = weatherRepository.weatherDomainObjects //TODO when does this get populated?

    // Method that takes id: Long as a parameter and retrieve a Weather from the
    //  database by id via the DAO.
    fun getWeatherById(id: Long): LiveData<WeatherEntity> {
        return weatherDao.getWeatherById(id).asLiveData()
    }

    /**
     * Call getWeatherData to get the data immediately
     */
    init {
        //TODO right now the app is only calling the api once, when the viewmodel is created,
        // or when the app is first started
        // getWeatherData()
    }


// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel

    class WeatherDetailViewModelFactory(private val weatherDao: WeatherDao, val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WeatherDetailViewModel(weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

