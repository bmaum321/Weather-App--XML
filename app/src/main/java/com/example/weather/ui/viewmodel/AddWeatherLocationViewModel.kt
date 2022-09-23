package com.example.weather.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.weather.data.WeatherDao
import com.example.weather.data.WeatherDatabase.Companion.getDatabase
import com.example.weather.model.WeatherEntity
import com.example.weather.network.WeatherContainer
import com.example.weather.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.IOException


/**
 * [ViewModel] to provide data to the  [AddWeatherLocationFragment] and allow for interaction the the [WeatherDao]
 */

// Pass an application as a parameter to the viewmodel constructor which is the contect passed to the singleton database object
class AddWeatherLocationViewModel(private val weatherDao: WeatherDao, application: Application) : AndroidViewModel(application) {


    //The data source this viewmodel will fetch results from
    private val weatherRepository = WeatherRepository(getDatabase(application))

    // A list of weather results for the list screen
    val weatherList = weatherRepository.weatherDomainObjects //TODO this should get populated anytime the database gets updated

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
    val allWeatherEntity: LiveData<List<WeatherEntity>> = weatherDao.getWeatherLocations().asLiveData() //TODO pull from repo?

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

    fun storeNetworkDataInDatabase(zipcode: String) {
        viewModelScope.launch {
            try {
                weatherRepository.storeNetworkWeatherInDatabase(zipcode)
                _eventNetworkError.value = false
                _isNetworkErrorShown.value = false
            } catch (networkError: IOException) {
                //If the weatherList pulled from the repository is empty, Show a Toast error message and hide the progress bar
                if (weatherList.value.isNullOrEmpty())
                    _eventNetworkError.value = true
            }
        }
    }
/*
    fun addWeather(
        name: String,
        zipcode: String,
        tempf: Double?,
        imgSrcUrl: String
    ) {
        val weatherEntity = WeatherEntity(
            cityName = name,
            zipCode = zipcode,
            temp = tempf,
            imgSrcUrl = imgSrcUrl,
            conditionText = conditionText,
        )

        // Launch a coroutine and call the DAO method to add a Weather to the database within it
        viewModelScope.launch {
            //getWeatherData(zipcode) //TODO trying different calls
            weatherDao.insert(weatherEntity)
        }

    }

 */

    fun updateWeather(
        id: Long,
        name: String,
        zipcode: String,
        tempf: Double?,
        imgSrcUrl: String,
        conditonText: String,
        windMph: Double,
        windDirection: String
    ) {
        val weatherEntity = WeatherEntity(
            id = id,
            cityName = name,
            zipCode = zipcode,
            temp = tempf,
            imgSrcUrl = imgSrcUrl,
            conditionText = conditonText,
            windMph = windMph,
            windDirection = windDirection
        )
        viewModelScope.launch(Dispatchers.IO) {
            //getWeatherData(zipcode) //TODO trying different calls
            // call the DAO method to update a weather object to the database here
            weatherDao.insert(weatherEntity)
        }
    }

    fun deleteWeather(weatherEntity: WeatherEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // call the DAO method to delete a weather object to the database here
            weatherDao.delete(weatherEntity)
        }
    }

    fun isValidEntry(name: String, address: String): Boolean {
        return name.isNotBlank() && address.isNotBlank()
    }


// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel

    class AddWeatherLocationViewModelFactory(private val weatherDao: WeatherDao, val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddWeatherLocationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddWeatherLocationViewModel(weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

