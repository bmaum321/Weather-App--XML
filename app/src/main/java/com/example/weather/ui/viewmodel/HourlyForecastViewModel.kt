package com.example.weather.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.weather.data.WeatherDao
import com.example.weather.data.WeatherDatabase.Companion.getDatabase
import com.example.weather.domain.WeatherDomainObject
import com.example.weather.domain.ForecastDomainObject
import com.example.weather.domain.HourlyForecastDomainObject
import com.example.weather.domain.asDomainModel
import com.example.weather.model.Hours
import com.example.weather.model.WeatherEntity
import com.example.weather.network.ApiResponse
import com.example.weather.repository.WeatherRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch


sealed class HourlyForecastViewData() {
    class Done(val hourlyForecastdomainObject: HourlyForecastDomainObject): HourlyForecastViewData()
    class Error(): HourlyForecastViewData()
    class Loading(): HourlyForecastViewData()
}

/**
 * [ViewModel] to provide data to the [WeatherLocationDetailFragment]
 */

// Pass an application as a parameter to the viewmodel constructor which is the contect passed to the singleton database object
class HourlyForecastViewModel(private val weatherDao: WeatherDao, application: Application) :
    AndroidViewModel(application) {

    private val _status = MutableLiveData<WeatherViewData>()

    val status: LiveData<WeatherViewData> = _status

    //The data source this viewmodel will fetch results from
    private val weatherRepository = WeatherRepository(getDatabase(application))

    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST).apply {
        tryEmit(Unit)
    }

    fun refresh() {
        refreshFlow.tryEmit(Unit)
    }

    fun getWeatherByZipcode(zipcode: String): LiveData<WeatherEntity> {
        return weatherDao.getWeatherByZipcode(zipcode).asLiveData()
    }

    fun getForecastForZipcode(zipcode: String): Flow<HourlyForecastViewData> {
        val hours = mutableListOf<Hours>()
        viewModelScope.launch {
            val response = weatherRepository.getForecast(zipcode)
            if (response is ApiResponse.Success) {
                response.data.forecast.forecastday.forEach { day ->
                    hours.add(day.hour)
                }
            }
        }




        return refreshFlow
            .flatMapLatest {
                flow {
                    emit(HourlyForecastViewData.Loading())
                    when (response) {
                        is ApiResponse.Success -> emit(
                            HourlyForecastViewData.Done(


                            )
                        )
                        is ApiResponse.Failure -> emit(
                            HourlyForecastViewData.Error(

                            )
                        )
                        is ApiResponse.Exception -> emit(
                            HourlyForecastViewData.Error(
                            )
                        )
                    }
                }
            }
    }


// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel

    class HourlyForecastViewModelFactory(private val weatherDao: WeatherDao, val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HourlyForecastViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HourlyForecastViewModel(weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

