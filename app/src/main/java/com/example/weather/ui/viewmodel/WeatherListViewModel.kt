package com.example.weather.ui.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.*
import com.example.weather.data.WeatherDao
import com.example.weather.data.WeatherDatabase.Companion.getDatabase
import com.example.weather.domain.ForecastDomainObject
import com.example.weather.domain.WeatherDomainObject
import com.example.weather.domain.asDomainModel
import com.example.weather.model.WeatherEntity
import com.example.weather.network.ApiResponse
import com.example.weather.network.WeatherContainer
import com.example.weather.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

import kotlinx.coroutines.launch



sealed class WeatherViewDataList() {
    class Loading() : WeatherViewDataList()
    class Error() : WeatherViewDataList()
    class Done(val weatherDomainObjects: MutableList<WeatherDomainObject>) : WeatherViewDataList()
}

/**
 * Shared [ViewModel] to provide data to the [WeatherListFragment], [WeatherLocationDetailFragment],
 * and [AddWeatherLocationFragment] and allow for interaction the the [WeatherDao]
 */

// Pass an application as a parameter to the viewmodel constructor which is the contect passed to the singleton database object
class WeatherListViewModel(private val weatherDao: WeatherDao, application: Application) :
    AndroidViewModel(application) {

    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST).apply {
        tryEmit(Unit)
    }

    //The data source this viewmodel will fetch results from
    private val weatherRepository = WeatherRepository(getDatabase(application))


    fun getWeatherByZipcode(location: String): WeatherEntity {
        return weatherDao.getWeatherByLocation(location)
    }

    //TODO need a method to collect all the zipcodes with sorting from the database and then pass to getAllWeather
    private fun getZipCodesFromDatabase(): Flow<List<String>> {
        return weatherDao.getZipcodesFlow()
    }

    fun getAllWeatherEntities(): List<WeatherEntity> {
        return weatherDao.getAllWeatherEntities()
    }

    suspend fun insertAllWeatherEntities(weatherEntities: List<WeatherEntity>) {
        weatherDao.insertAll(weatherEntities)
    }

    suspend fun deleteAll() {
        weatherDao.deleteAll()
    }



    /**
     *     Method that takes a list of zipcodes as a parameter and retrieve a list of weathera
     *     objects from the repository
     */

    fun getAllWeatherWithErrorHandling(
        resources: Resources,
        sharedPreferences: SharedPreferences)
    : Flow<WeatherViewDataList> {
        return refreshFlow
            .flatMapLatest {
                getZipCodesFromDatabase()
                    .flatMapLatest { zipcodes ->
                        flow {
                            if (zipcodes.isNotEmpty()) {
                                emit(WeatherViewDataList.Loading()) // Was a bug here, stuck in loading if database is empty, we did it before the empty check and had no listerner set on FAB
                                when (weatherRepository.getWeatherWithErrorHandling(zipcodes.first())) {
                                    is ApiResponse.Success -> emit(
                                        WeatherViewDataList.Done(
                                            weatherRepository.getWeatherListForZipCodes(zipcodes, resources, sharedPreferences).toMutableList()
                                        )
                                    )
                                    is ApiResponse.Failure -> emit(WeatherViewDataList.Error())
                                    is ApiResponse.Exception -> emit(WeatherViewDataList.Error())
                                }
                            }
                        }

                    }
            }
    }

    fun refresh() {
        refreshFlow.tryEmit(Unit)
    }

    fun updateWeather(
        id: Long,
        name: String,
        zipcode: String,
        sortOrder: Int
    ) {
        val weatherEntity = WeatherEntity(
            id = id,
            cityName = name,
            zipCode = zipcode,
            sortOrder = sortOrder
        )
        viewModelScope.launch(Dispatchers.IO) {
            // call the DAO method to update a weather object to the database here
            weatherDao.update(weatherEntity)
        }
    }

    fun deleteWeather(weatherEntity: WeatherEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // call the DAO method to delete a weather object to the database here
            weatherDao.delete(weatherEntity)
        }
    }

// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel

    class WeatherViewModelFactory(private val weatherDao: WeatherDao, val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WeatherListViewModel(weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

