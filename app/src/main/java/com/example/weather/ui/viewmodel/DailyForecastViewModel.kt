package com.example.weather.ui.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.weather.data.WeatherDao
import com.example.weather.data.WeatherDatabase.Companion.getDatabase
import com.example.weather.domain.WeatherDomainObject
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

sealed class ForecastViewData() {
    class Loading() : ForecastViewData()
    class Error(val code: Int, val message: String?) : ForecastViewData()
    class Done(val forecastDomainObject: ForecastDomainObject) : ForecastViewData()
}

/**
 * [ViewModel] to provide data to the [WeatherLocationDetailFragment]
 */

// Pass an application as a parameter to the viewmodel constructor which is the contect passed to the singleton database object
class WeatherDetailViewModel(private val weatherDao: WeatherDao, application: Application) :
    AndroidViewModel(application) {

    //The data source this viewmodel will fetch results from
    private val weatherRepository = WeatherRepository(getDatabase(application))

    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST)
        .apply {
            tryEmit(Unit)
        }

    fun refresh() {
        refreshFlow.tryEmit(Unit)
    }

    fun getWeatherByZipcode(zipcode: String): LiveData<WeatherEntity> {
        return weatherDao.getWeatherByZipcode(zipcode)
            .asLiveData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getForecastForZipcode(zipcode: String,
                              sharedPreferences: SharedPreferences,
                              resources: Resources)
    : Flow<ForecastViewData> {
        return refreshFlow
            .flatMapLatest {
                flow {
                    emit(ForecastViewData.Loading())
                    when (val response = weatherRepository.getForecast(zipcode)) {
                        is ApiResponse.Success -> emit(
                            ForecastViewData.Done(
                                response.data.asDomainModel(sharedPreferences, resources)
                            )
                        )
                        is ApiResponse.Failure -> emit(
                            ForecastViewData.Error(
                                code = response.code,
                                message = response.message
                            )
                        )
                        is ApiResponse.Exception -> emit(
                            ForecastViewData.Error(
                                code = 0,
                                message = response.e.message
                            )
                        )
                    }
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

