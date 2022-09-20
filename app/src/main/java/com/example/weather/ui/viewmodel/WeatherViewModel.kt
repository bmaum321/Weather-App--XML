package com.example.weather.ui.viewmodel

import androidx.lifecycle.*
import com.example.weather.data.WeatherDao
import com.example.weather.model.Weather
import com.example.weather.network.WeatherApi
import com.example.weather.network.WeatherContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class WeatherApiStatus { LOADING, ERROR, DONE }

/**
 * Shared [ViewModel] to provide data to the [WeatherListFragment], [WeatherLocationDetailFragment],
 * and [AddWeatherLocationFragment] and allow for interaction the the [WeatherDao]
 */

// Pass a WeatherDao value as a parameter to the view model constructor
class WeatherViewModel(
    // Pass dao here
private val weatherDao: WeatherDao
): ViewModel() {


    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<WeatherApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<WeatherApiStatus> = _status

    // Internally, we use a MutableLiveData, because we will be updating the List of MarsPhoto
    // with new values
    private val _weatherData = MutableLiveData<WeatherContainer?>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val weatherData: LiveData<WeatherContainer?> = _weatherData

    // create a property to set to a list of all weather objects from the DAO
    val allWeather: LiveData<List<Weather>> = weatherDao.getWeatherLocations().asLiveData()

    // Method that takes id: Long as a parameter and retrieve a Weather from the
    //  database by id via the DAO.
    fun getWeatherById(id: Long): LiveData<Weather> {
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

    fun getWeatherData(zipcode: String) {
        viewModelScope.launch {
            _status.value = WeatherApiStatus.LOADING
            try {
                _weatherData.value = WeatherApi.retrofitService.getWeather(zipcode)
                _status.value = WeatherApiStatus.DONE
            } catch (e: Exception) {
                _status.value = WeatherApiStatus.ERROR
               // _weatherData.value = WeatherContainer(current = null, location = null)
            }
        }
    }

    fun addWeather(
        name: String,
        zipcode: String,
        notes: String
    ) {
        val weather = Weather(
            cityName = name,
            zipCode = zipcode,
            notes = notes
        )

    // Launch a coroutine and call the DAO method to add a Weather to the database within it
        viewModelScope.launch {
            weatherDao.insert(weather)
            getWeatherData(zipcode) //TODO trying different calls
        }

    }

    fun updateWeather(
        id: Long,
        name: String,
        zipcode: String,
        notes: String
    ) {
        val weather = Weather(
            id = id,
            cityName = name,
            zipCode = zipcode,
            notes = notes
        )
        viewModelScope.launch(Dispatchers.IO) {
            // call the DAO method to update a weather object to the database here
            weatherDao.insert(weather)
        }
    }

    fun deleteWeather(weather: Weather) {
        viewModelScope.launch(Dispatchers.IO) {
            // call the DAO method to delete a weather object to the database here
            weatherDao.delete(weather)
        }
    }

    fun isValidEntry(name: String, address: String): Boolean {
        return name.isNotBlank() && address.isNotBlank()
    }

}

// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel
class WeatherViewModelFactory(private val weatherDao: WeatherDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(weatherDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}
