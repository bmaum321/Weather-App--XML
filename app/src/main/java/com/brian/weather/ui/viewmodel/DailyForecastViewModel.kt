package com.brian.weather.ui.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.brian.weather.data.WeatherDao
import com.brian.weather.data.WeatherDatabase.Companion.getDatabase
import com.brian.weather.domain.ForecastDomainObject
import com.brian.weather.domain.asDomainModel
import com.brian.weather.model.Day
import com.brian.weather.model.WeatherEntity
import com.brian.weather.network.ApiResponse
import com.brian.weather.repository.WeatherRepository
import com.brian.weather.ui.adapter.DaysViewData
import com.brian.weather.ui.adapter.ForecastItemViewData
import com.brian.weather.ui.settings.GetSettings
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
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WeatherDetailViewModel(weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Use the Celsius temp for display if the setting is checked
 */

fun ForecastItemViewData.withPreferenceConversion(sharedPreferences: SharedPreferences, resources: Resources): ForecastItemViewData {
    val isFahrenheit =
        GetSettings().getTemperatureFormatFromPreferences(sharedPreferences, resources)

    return ForecastItemViewData(
        day = Day(
            date = day.date,
            day = day.day,
            hour = day.hour
        ),
        daysViewData = DaysViewData(
            maxTemp = if(isFahrenheit) {
                "${day.day.maxtemp_f.toInt()}°"
            } else "${day.day.maxtemp_c.toInt()}°",
            minTemp = if(isFahrenheit) {
                "${day.day.mintemp_f.toInt()}°"
            } else "${day.day.mintemp_c.toInt()}°"
        )
    )
}

