package com.example.weather.ui.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.*
import com.example.weather.data.WeatherDao
import com.example.weather.data.WeatherDatabase.Companion.getDatabase
import com.example.weather.domain.ForecastDomainObject
import com.example.weather.domain.asDomainModel
import com.example.weather.model.WeatherEntity
import com.example.weather.network.ApiResponse
import com.example.weather.repository.WeatherRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow


sealed class HourlyForecastViewData() {
    class Done(val forecastDomainObject: ForecastDomainObject) : HourlyForecastViewData()
    class Error() : HourlyForecastViewData()
    class Loading() : HourlyForecastViewData()
}

/**
 * [ViewModel] to provide data to the [WeatherLocationDetailFragment]
 */

// Pass an application as a parameter to the viewmodel constructor which is the contect passed to the singleton database object
class HourlyForecastViewModel(private val weatherDao: WeatherDao, application: Application) :
    AndroidViewModel(application) {

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

    fun getForecastForZipcode(zipcode: String,
                              sharedPreferences: SharedPreferences,
                              resources: Resources): Flow<HourlyForecastViewData> {
        return refreshFlow
            .flatMapLatest {
                flow {
                    emit(HourlyForecastViewData.Loading())
                    when (val response = weatherRepository.getForecast(zipcode)) {
                        is ApiResponse.Success -> emit(
                            HourlyForecastViewData.Done(
                                response.data.asDomainModel(sharedPreferences, resources)
                            )
                        )
                        is ApiResponse.Failure -> emit(
                            HourlyForecastViewData.Error()
                        )
                        is ApiResponse.Exception -> emit(
                            HourlyForecastViewData.Error()
                        )
                    }
                }
            }
    }

// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel

    class HourlyForecastViewModelFactory(
        private val weatherDao: WeatherDao,
        val app: Application
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HourlyForecastViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HourlyForecastViewModel(weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


