package com.example.weather.domain


import android.os.Build
import androidx.annotation.RequiresApi
import com.example.weather.model.Day
import com.example.weather.model.ForecastContainer
import com.example.weather.model.Hours
import com.example.weather.network.WeatherContainer
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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
    val tempf: String,
    val zipcode: String,
    val imgSrcUrl: String,
    val conditionText: String,
    val windMph: Double,
    val windDirection: String,
    val time: String
)

data class ForecastDomainObject(
    val days: List<Day>
)

data class HourlyForecastDomainObject(
    val hours: List<Hours>
)


fun WeatherContainer.asDomainModel(zipcode: String): WeatherDomainObject {
    // Get local time for display
    location.localtime = Instant
        .ofEpochSecond(location.localtime_epoch)
        .atZone(ZoneId.of(location.tz_id))
        .format(
            DateTimeFormatter
                .ofPattern("hh:mm a")
        )
        .removePrefix("0")
    return WeatherDomainObject(
        time = location.localtime,
        location = location.name,
        zipcode = zipcode,
        tempf = current.temp_f.toString().dropLast(2),
        imgSrcUrl = current.condition.icon,
        conditionText = current.condition.text,
        windMph = current.wind_mph,
        windDirection = current.wind_dir,
    )
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun ForecastContainer.asDomainModel(): ForecastDomainObject {

    /**
     * Remove any hours that are in the past
     * Convert daily timestamp from API into day of week for the daily forecast
     * Convert hourly timestamp from API from 24hr format to 12hr format
     * This should be converted to use kotlinx-datetime
     * https://github.com/Kotlin/kotlinx-datetime#using-in-your-projects
     */

    val currentEpochTime = System
        .currentTimeMillis()
        .toString()
        .dropLast(3)
        .toInt() //get current epoch time in seconds
    forecast.forecastday
        .forEach { day ->
            /**
             * Remove all method is used to avoid concurrent modification error on collections. Lets you
             * delete items from a collection as you iterate through it
             */
            day.hour
                .removeAll { hours ->
                    hours.time_epoch < currentEpochTime
                }
            day.date = LocalDate
                .parse(day.date)
                .dayOfWeek
                .getDisplayName(
                TextStyle.FULL,
                Locale.ENGLISH
            ) // Convert to day of week
            forecast.forecastday
                .first().date = "Today"
            day.day.mintemp_f.toString().dropLast(2) // doesnt seem to work
            day.day.maxtemp_f.toString().dropLast(2)
            day.hour
                .forEach { hour ->
                    hour.time = LocalTime.parse(
                        hour.time
                            .substring(11)
                    ) // Remove date stamp
                        .format(
                            DateTimeFormatter
                                .ofPattern("hh:mm a") // Add AM/PM postfix
                        )
                        .removePrefix("0") // Remove 0 prefix, Ex: Turn 01:00 PM into 1:00PM
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

