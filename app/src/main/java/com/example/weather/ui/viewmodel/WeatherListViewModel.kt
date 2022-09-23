package com.example.weather.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.weather.data.WeatherDao
import com.example.weather.data.WeatherDatabase.Companion.getDatabase
import com.example.weather.domain.WeatherDomainObject
import com.example.weather.model.WeatherEntity
import com.example.weather.network.WeatherContainer
import com.example.weather.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import okio.IOException

enum class WeatherApiStatus { LOADING, ERROR, DONE }

/**
 * Shared [ViewModel] to provide data to the [WeatherListFragment], [WeatherLocationDetailFragment],
 * and [AddWeatherLocationFragment] and allow for interaction the the [WeatherDao]
 */

// Pass an application as a parameter to the viewmodel constructor which is the contect passed to the singleton database object
class WeatherListViewModel(private val weatherDao: WeatherDao, application: Application) :
    AndroidViewModel(application) {


    //The data source this viewmodel will fetch results from
    private val weatherRepository = WeatherRepository(getDatabase(application))

    // A list of weather results for the list screen
    val weatherList = weatherRepository.weatherDomainObjects //TODO when does this get populated?

    /**
     * Event triggered for network error. This is private to avoid exposing a
     * way to set this value to observers.
     */
    private var _eventNetworkError = MutableLiveData<Boolean>(false)

    /**
     * Event triggered for network error. Views should use this to get access
     * to the data.
     */
    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError

    /**
     * Flag to display the error message. This is private to avoid exposing a
     * way to set this value to observers.
     */
    private var _isNetworkErrorShown = MutableLiveData<Boolean>(false)

    /**
     * Flag to display the error message. Views should use this to get access
     * to the data.
     */
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown


    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<WeatherApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<WeatherApiStatus> = _status

    // Internally, we use a MutableLiveData, because we will be updating the List of MarsPhoto
    // with new values
    private val _weatherData = MutableLiveData<WeatherContainer>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val weatherData: LiveData<WeatherContainer> = _weatherData

    // create a property to set to a list of all weather objects from the DAO
    val allWeatherEntity: LiveData<List<WeatherEntity>> =
        weatherDao.getWeatherLocations().asLiveData() //TODO pull from repo?

    // Method that takes id: Long as a parameter and retrieve a Weather from the
    //  database by id via the DAO.
    fun getWeatherById(id: Long): LiveData<WeatherEntity> {
        return weatherDao.getWeatherById(id).asLiveData()
    }

    //TODO need a method to collect all the zipcodes from the database and then pass to getAllWeather
    fun getZipCodesFromDatabase(): List<String> {
        return weatherDao.getZipcodes()
        //TODO might just be able to pass this directly into the below function, but how do
        // correlate the weather response with each list item
    }



    /**
     * Call getWeatherData to get the data immediately
     */
    init {
        //TODO right now the app is only calling the api once, when the viewmodel is created,
        // or when the app is first started
        // getWeatherData()
    }

    /**
     *     Method that takes a list of zipcodes as a parameter and retrieve a list of weather
     *     objects from the repository
     */

    fun getAllWeather(zipcodes: List<String>): Flow<List<WeatherDomainObject>> {
        return flow {
            emit(weatherRepository.getWeatherListForZipCodes(zipcodes))
        }
    }

// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel

    class WeatherViewModelFactory(private val weatherDao: WeatherDao, val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WeatherListViewModel(weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

