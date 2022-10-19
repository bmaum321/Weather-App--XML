package com.brian.weather.domain


import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import com.example.weather.R
import com.brian.weather.model.*
import com.brian.weather.network.WeatherContainer
import com.brian.weather.ui.settings.GetSettings
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
 */


data class WeatherDomainObject(
    val location: String,
    val temp: String,
    val zipcode: String,
    val imgSrcUrl: String,
    val conditionText: String,
    val windSpeed: Double,
    val windDirection: String,
    val time: String,
    val backgroundColor: Int,
    val code: Int,
    val textColor: Int,
    val country: String,
    val feelsLikeTemp: String
)

data class ForecastDomainObject(
    val days: List<Day>,
    val alerts: List<Alert>,
)

fun WeatherContainer.asDomainModel(
    zipcode: String,
    resources: Resources,
    sharedPreferences: SharedPreferences
): WeatherDomainObject {

    // Get local time for display

    location.localtime = Instant
        .ofEpochSecond(location.localtime_epoch)
        .atZone(ZoneId.of(location.tz_id))
        .format(
            DateTimeFormatter
                .ofPattern(GetSettings().getTimeFormatFromPreferences(sharedPreferences, resources))
        )
        .removePrefix("0")


    /**
     * Change background color of card based off current condition code from API if setting is checked
     * otherwise, the background is transparent
     */

    var backgroundColor: Int = R.color.material_dynamic_neutral_variant30
    var textColor: Int = R.color.material_dynamic_neutral_variant80

    // Dynamic Material colors not supported on < API 31
    if(Build.VERSION.SDK_INT <= 31) {
        textColor = R.color.black
        backgroundColor = R.color.transparent
    }
    if (sharedPreferences.getBoolean(
            resources.getString(R.string.show_current_condition_color),
            true
        )
    ) {
        backgroundColor = when (current.condition.code) {
            1000 -> {
                if (current.condition.text == resources.getString(R.string.Sunny)) {
                    R.drawable.sungradient// sunny
                } else R.color.purple_night // clear night
            }
            1003 -> if (current.is_day == 1) {
                R.drawable.day_partly_cloudy_gradient // partly cloudy day
            } else {
                R.drawable.night_partly_cloudy
            } // partly cloudy night
            in 1006..1030 -> R.color.gray // clouds/overcast
            in 1063..1117 -> R.drawable.raingradient // rain
            in 1150..1207 -> R.drawable.raingradient // rain
            in 1210..1237 -> R.color.white //snow
            in 1240..1282 -> R.drawable.raingradient // rain
            else -> R.color.white
        }

        // Change text color to black for certain gradients for easier reading
        if (backgroundColor == R.color.white ||
            backgroundColor == R.drawable.sungradient ||
            backgroundColor == R.drawable.day_partly_cloudy_gradient
        ) {
            textColor = R.color.light_black
        }
    }

    /**
     * Country formatting
     */
    when (location.country) {
        resources.getString(R.string.USA) -> location.country =
            resources.getString(R.string.USA_Acronym)
        resources.getString(R.string.USA2) -> location.country =
            resources.getString(R.string.USA_Acronym)
        resources.getString(R.string.UK) -> location.country =
            resources.getString(R.string.UK_Acronym)
    }

    return WeatherDomainObject(
        time = location.localtime,
        location = location.name,
        zipcode = zipcode,
        temp =
        if (GetSettings().getTemperatureFormatFromPreferences(sharedPreferences, resources)) {
            current.temp_f.toInt().toString()
        } else current.temp_c.toInt().toString(),
        imgSrcUrl = current.condition.icon,
        conditionText = current.condition.text,
        windSpeed = if (GetSettings().getWindSpeedFormatFromPreferences(
                sharedPreferences,
                resources
            )
        ) {
            current.wind_mph
        } else current.wind_kph,
        windDirection = current.wind_dir,
        backgroundColor = backgroundColor,
        code = current.condition.code,
        textColor = textColor,
        country = location.country,
        feelsLikeTemp =
        if (GetSettings().getTemperatureFormatFromPreferences(sharedPreferences, resources)) {
            current.feelslike_f.toInt().toString()
        } else current.feelslike_c.toInt().toString()
    )
}

fun ForecastContainer.asDomainModel(
    sharedPreferences: SharedPreferences,
    resources: Resources
): ForecastDomainObject {

    /**
     * Remove any hours that are in the past
     * Convert daily timestamp from API into day of week for the daily forecast
     * Convert hourly timestamp from API from 24hr format to 12hr format
     * This should be converted to use kotlinx-datetime
     * https://github.com/Kotlin/kotlinx-datetime#using-in-your-projects
     */

    // Subtracting an hour from current time to see the current hour in the forecast
    val currentEpochTime = System
        .currentTimeMillis() / 1000 - 3600
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
                .first().date = resources.getString(R.string.today)
            day.hour
                .forEach { hour ->
                    hour.time = LocalTime.parse(
                        hour.time
                            .substring(11) // Remove date from time
                    ) // Remove date stamp
                        .format(
                            DateTimeFormatter
                                .ofPattern(
                                    GetSettings().getTimeFormatFromPreferences(
                                        sharedPreferences,
                                        resources
                                    )
                                ) // Add AM/PM postfix
                        )
                        .removePrefix("0") // Remove 0 prefix, Ex: Turn 01:00 PM into 1:00PM
                }
        }

    // Format the alert text from API
    alerts.alert.forEach { alert ->
        alert.desc = alert.desc.replace("\n", " ").replace("*", "\n**")
    }


    return ForecastDomainObject(
        days = forecast.forecastday,
        alerts = alerts.alert
    )
}







