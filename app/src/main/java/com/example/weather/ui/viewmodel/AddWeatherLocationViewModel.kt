package com.example.weather.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.weather.data.WeatherDao
import com.example.weather.data.WeatherDatabase.Companion.getDatabase
import com.example.weather.model.WeatherEntity
import com.example.weather.network.ApiResponse
import com.example.weather.network.WeatherContainer
import com.example.weather.network.asDatabaseModel
import com.example.weather.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * [ViewModel] to provide data to the  [AddWeatherLocationFragment] and allow for interaction the the [WeatherDao]
 */

// Pass an application as a parameter to the viewmodel constructor which is the contect passed to the singleton database object
class AddWeatherLocationViewModel(private val weatherDao: WeatherDao, application: Application) :
    AndroidViewModel(application) {


    //The data source this viewmodel will fetch results from
    private val weatherRepository = WeatherRepository(getDatabase(application))

    // A list of weather results for the list screen
//    val weatherList =
   //     weatherRepository.weatherDomainObjects //TODO this should get populated anytime the database gets updated


    // create a property to set to a list of all weather objects from the DAO
    val allWeatherEntity: LiveData<List<WeatherEntity>> =
        weatherDao.getWeatherLocations().asLiveData() //TODO pull from repo?

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

    suspend fun storeNetworkDataInDatabase(zipcode: String): Boolean {
        var networkError = false

        /**
         * This runs on a background thread by default so any value modified within this scoupe cannot
         * be returned outside of the scope
         */

        when (val response = weatherRepository.getWeatherWithErrorHandling(zipcode)) {
            is ApiResponse.Success -> {
                weatherDao.insert(response.data.asDatabaseModel(zipcode))
                networkError = true
            }
            is ApiResponse.Failure -> networkError = false
            is ApiResponse.Exception -> networkError = false
        }
        return networkError

    }


    fun updateWeather(
        id: Long,
        name: String,
        zipcode: String,
        tempf: Double?,
        imgSrcUrl: String,
        conditonText: String,
        windMph: Double,
        windDirection: String,
        time: String
    ) {
        val weatherEntity = WeatherEntity(
            id = id,
            cityName = name,
            zipCode = zipcode,
            temp = tempf,
            imgSrcUrl = imgSrcUrl,
            conditionText = conditonText,
            windMph = windMph,
            windDirection = windDirection,
            time = time
        )
        viewModelScope.launch(Dispatchers.IO) {
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

    fun isValidEntry(zipcode: String): Boolean {
        return zipcode.isNotBlank()
    }


// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel

    class AddWeatherLocationViewModelFactory(
        private val weatherDao: WeatherDao,
        val app: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddWeatherLocationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddWeatherLocationViewModel(weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

