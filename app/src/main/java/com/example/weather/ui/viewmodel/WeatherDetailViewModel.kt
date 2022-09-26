package com.example.weather.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.weather.data.WeatherDao
import com.example.weather.data.WeatherDatabase.Companion.getDatabase
import com.example.weather.domain.WeatherDomainObject
import com.example.weather.domain.ForecastDomainObject
import com.example.weather.domain.asDomainModel
import com.example.weather.model.WeatherEntity
import com.example.weather.network.ApiResponse
import com.example.weather.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


sealed class WeatherViewData() {
    class Loading() : WeatherViewData()
    class Error() : WeatherViewData()
    class Done(val weatherDomainObject: WeatherDomainObject): WeatherViewData()
}

sealed class ForecastViewData() {
    class Loading() : ForecastViewData()
    class Error(val code: Int, val message: String?) : ForecastViewData()
    class Done(val forecastDomainObject: ForecastDomainObject): ForecastViewData()
}

/**
 * [ViewModel] to provide data to the [WeatherLocationDetailFragment]
 */

// Pass an application as a parameter to the viewmodel constructor which is the contect passed to the singleton database object
class WeatherDetailViewModel(private val weatherDao: WeatherDao, application: Application) :
    AndroidViewModel(application) {

    private val _status = MutableLiveData<WeatherViewData>()

    val status: LiveData<WeatherViewData> = _status


    //The data source this viewmodel will fetch results from
    private val weatherRepository = WeatherRepository(getDatabase(application))

    // A list of weather results for the list screen
    val weatherList = weatherRepository.weatherDomainObjects //TODO when does this get populated?


    // Method that takes id: Long as a parameter and retrieve a Weather from the
    //  database by id via the DAO.
    fun getWeatherById(id: Long): LiveData<WeatherEntity> {
        return weatherDao.getWeatherById(id).asLiveData()
    }

    fun getWeatherByZipcode(zipcode: String): LiveData<WeatherEntity> {
        return weatherDao.getWeatherByZipcode(zipcode).asLiveData()
    }

    // Method that takes zipcode as a parameter and retrieve a Weather from the
    //  repository
    fun getWeatherFromNetworkByZipCode(zipcode: String): Flow<WeatherDomainObject> {
        return flow {
            emit(weatherRepository.getWeather(zipcode))
        }
    }

    fun getWeatherForZipcode(zipcode: String): Flow<WeatherViewData> {
        return flow {
            emit(WeatherViewData.Loading())
            when (val response = weatherRepository.getWeatherWithErrorHandling(zipcode)) {
                is ApiResponse.Success -> emit(WeatherViewData.Done(response.data.asDomainModel(zipcode)))
                is ApiResponse.Failure -> emit(WeatherViewData.Error())
                is ApiResponse.Exception -> emit(WeatherViewData.Error())
            }
        }
    }

    fun getForecastForZipcode(zipcode: String): Flow<ForecastViewData> {
        return flow {
            emit(ForecastViewData.Loading()) //TODO bug here
            when (val response = weatherRepository.getForecast(zipcode)) {
                is ApiResponse.Success -> emit(ForecastViewData.Done(response.data.asDomainModel(zipcode)))
                is ApiResponse.Failure -> emit(ForecastViewData.Error(code = response.code, message = response.message))
                is ApiResponse.Exception -> emit(ForecastViewData.Error(code = 0, message=response.e.message))
            }
        }
    }



// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel

    class WeatherDetailViewModelFactory(private val weatherDao: WeatherDao, val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WeatherDetailViewModel(weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

