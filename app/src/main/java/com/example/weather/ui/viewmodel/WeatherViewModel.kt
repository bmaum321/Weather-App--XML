package com.example.weather.ui.viewmodel

import androidx.lifecycle.*
import com.example.weather.data.WeatherDao
import com.example.weather.model.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Shared [ViewModel] to provide data to the [WeatherListFragment], [WeatherLocationDetailFragment],
 * and [AddWeatherLocationFragment] and allow for interaction the the [WeatherDao]
 */

// Pass a WeatherDao value as a parameter to the view model constructor
class WeatherViewModel(
    // Pass dao here
private val weatherDao: WeatherDao
): ViewModel() {

    // TODO: create a property to set to a list of all weather objects from the DAO
    val allWeather: LiveData<List<Weather>> = weatherDao.getWeatherLocations().asLiveData()

    // TODO : create method that takes id: Long as a parameter and retrieve a Weather from the
    //  database by id via the DAO.
    fun getWeatherById(id: Long): LiveData<Weather> {
        return weatherDao.getWeatherById(id).asLiveData()
    }

    fun addWeather(
        name: String,
        address: String,
        inSeason: Boolean,
        notes: String
    ) {
        val weather = Weather(
            name = name,
            address = address,
            inSeason = inSeason,
            notes = notes
        )

    // TODO: launch a coroutine and call the DAO method to add a Weather to the database within it
        viewModelScope.launch {
            weatherDao.insert(weather)
        }

    }

    fun updateWeather(
        id: Long,
        name: String,
        address: String,
        inSeason: Boolean,
        notes: String
    ) {
        val weather = Weather(
            id = id,
            name = name,
            address = address,
            inSeason = inSeason,
            notes = notes
        )
        viewModelScope.launch(Dispatchers.IO) {
            // TODO: call the DAO method to update a forageable to the database here
            weatherDao.insert(weather)
        }
    }

    fun deleteWeather(weather: Weather) {
        viewModelScope.launch(Dispatchers.IO) {
            // TODO: call the DAO method to delete a forageable to the database here
            weatherDao.delete(weather)
        }
    }

    fun isValidEntry(name: String, address: String): Boolean {
        return name.isNotBlank() && address.isNotBlank()
    }
}

// TODO: create a view model factory that takes a WeatherDao as a property and
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
