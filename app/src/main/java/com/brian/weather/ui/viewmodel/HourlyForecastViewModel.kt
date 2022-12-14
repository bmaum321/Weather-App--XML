package com.brian.weather.ui.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.lifecycle.*
import com.brian.weather.data.WeatherDao
import com.brian.weather.data.WeatherDatabase.Companion.getDatabase
import com.brian.weather.domain.ForecastDomainObject
import com.brian.weather.domain.asDomainModel
import com.brian.weather.model.Hours
import com.brian.weather.model.WeatherEntity
import com.brian.weather.network.ApiResponse
import com.brian.weather.repository.WeatherRepository
import com.brian.weather.ui.adapter.HourlyForecastItemViewData
import com.brian.weather.ui.settings.GetSettings
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow


sealed class HourlyForecastViewData {
    class Done(val forecastDomainObject: ForecastDomainObject) : HourlyForecastViewData()
    class Error : HourlyForecastViewData()
    class Loading : HourlyForecastViewData()
}

/**
 * [ViewModel] to provide data to the [WeatherLocationDetailFragment]
 */

// Pass an application as a parameter to the viewmodel constructor which is the context passed to the singleton database object
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

    fun getForecastForZipcode(
        zipcode: String,
        sharedPreferences: SharedPreferences,
        resources: Resources
    ): Flow<HourlyForecastViewData> {
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

/**
 * Get units from settings and format data for display
 */

fun HourlyForecastItemViewData.withPreferenceConversion(
    sharedPreferences: SharedPreferences,
    resources: Resources
): HourlyForecastItemViewData {
    val isFahrenheit =
        GetSettings().getTemperatureFormatFromPreferences(sharedPreferences, resources)
    val isMph = GetSettings().getWindSpeedFormatFromPreferences(sharedPreferences, resources)
    val isIn = GetSettings().getMeasurementFormatFromPreferences(sharedPreferences, resources)

    return HourlyForecastItemViewData(
        hour = Hours(
            time_epoch = hour.time_epoch,
            time = hour.time,
            temp_f = hour.temp_f,
            temp_c = hour.temp_c,
            is_day = hour.is_day,
            condition = hour.condition,
            wind_mph = hour.wind_mph,
            wind_kph = hour.wind_kph,
            wind_dir = hour.wind_dir,
            chance_of_rain = hour.chance_of_rain,
            chance_of_snow = hour.chance_of_snow,
            feelslike_c = hour.feelslike_c,
            feelslike_f = hour.feelslike_f,
            precip_in = hour.precip_in,
            precip_mm = hour.precip_mm,
            pressure_in = hour.pressure_in,
            pressure_mb = hour.pressure_mb,
            will_it_rain = hour.will_it_rain,
            will_it_snow = hour.will_it_snow,
            windchill_c = hour.windchill_c,
            windchill_f = hour.windchill_f

        ),

        // Format data for display
        hoursViewData = HoursViewData(
            temperature = if (isFahrenheit) {
                "${hour.temp_f.toInt()}??"
            } else "${hour.temp_c.toInt()}??",
            condition = hour.condition.text,
            icon = hour.condition.icon,
            windSpeed = if (isMph) {
                "${hour.wind_mph} MPH"
            } else "${hour.wind_kph} KPH",
            feelsLike = if (isFahrenheit) {
                "Feels Like: ${hour.feelslike_f}??"
            } else "Feels Like: ${hour.feelslike_c}??",
            pressure = if (isIn) {
                "${hour.precip_in} IN"
            } else "${hour.precip_mm} MM",
            precipAmount = if (isIn) {
                "${hour.precip_in} IN"
            } else "${hour.precip_mm} MM",
            time = hour.time,
            windDirection = hour.wind_dir
        )
    )
}

data class HoursViewData(
    val temperature: String,
    val condition: String,
    val icon: String,
    val windSpeed: String,
    val feelsLike: String,
    val pressure: String,
    val precipAmount: String,
    val time: String,
    val windDirection: String
)





