package com.example.weather.domain

import android.icu.text.DateFormat.getTimeInstance
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.weather.model.Day
import com.example.weather.model.ForecastContainer
import com.example.weather.model.Hours
import com.example.weather.network.WeatherContainer
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*


/**
 * Domain objects are plain Kotlin data classes that represent the things in our app. These are the
 * objects that should be displayed on screen, or manipulated by the app.
 *
 * @see database for objects that are mapped to the database
 * @see network for objects that parse or prepare network calls
 */


data class WeatherDomainObject(
    val location: String,
    val tempf: Double?,
    val zipcode: String,
    val imgSrcUrl: String,
    val conditionText: String,
    val windMph: Double,
    val windDirection: String,

    )

// TODO do I need to destructure this into further data class to use with the list adapter?
data class ForecastDomainObject(
    val days: List<Day>
)

data class HourlyForecastDomainObject(
    val hours: List<Hours>
)


fun WeatherContainer.asDomainModel(zipcode: String): WeatherDomainObject {
    return WeatherDomainObject(
        location = location.name,
        zipcode = zipcode,
        tempf = current.temp_f,
        imgSrcUrl = current.condition.icon,
        conditionText = current.condition.text,
        windMph = current.wind_mph,
        windDirection = current.wind_dir,
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun ForecastContainer.asDomainModel(): ForecastDomainObject {

    val date = getTimeInstance()
    /**
     * Convert daily timestamp from API into day of week for the daily forecast
     * Convert hourly timestamp from API from 24hr format to 12hr format
     * This should be converted to use kotlinx-datetime
     * https://github.com/Kotlin/kotlinx-datetime#using-in-your-projects
     */
    forecast.forecastday.forEach { day ->
        day.date = LocalDate.parse(day.date).dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        day.hour.forEach { hour ->
            hour.time = LocalTime.parse(hour.time.substring(11))
                .format(
                    DateTimeFormatter
                        .ofPattern("hh:mm a")
                )
                .removePrefix("0")


        }
    }
    return ForecastDomainObject(
        days = forecast.forecastday
    )
}

fun Day.asDomainModel(): HourlyForecastDomainObject {
    return HourlyForecastDomainObject(
        hours = hour
    )
}

