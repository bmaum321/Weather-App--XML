package com.example.weather.domain


import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.weather.R
import com.example.weather.model.*
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

private const val HHMM = "hh:mm a"

data class WeatherDomainObject(
    val location: String,
    val tempf: String,
    val zipcode: String,
    val imgSrcUrl: String,
    val conditionText: String,
    val windMph: Double,
    val windDirection: String,
    val time: String,
    val backgroundColor: Int,
    val code: Int,
    val textColor: Int,
    val country: String
)

data class ForecastDomainObject(
    val days: List<Day>,
    val alerts: List<Alert>,
)

data class SearchDomainObject(
    val searchResults: List<Search>
)


fun WeatherContainer.asDomainModel(zipcode: String, resource: Resources): WeatherDomainObject {

    // Get local time for display
    location.localtime = Instant
        .ofEpochSecond(location.localtime_epoch)
        .atZone(ZoneId.of(location.tz_id))
        .format(
            DateTimeFormatter
                .ofPattern(HHMM)
        )
        .removePrefix("0")

    /**
     * Change background color of card based off current condition code from APU
     */
    var textColor = R.color.white
    val backgroudColor = when (current.condition.code) {
        1000 -> {
            if (current.condition.text == "Sunny") {
               R.drawable.sungradient// sunny
            } else R.color.purple_night // clear night
        }
        1003 -> if(current.is_day == 1) {
            R.drawable.day_partly_cloudy_gradient // partly cloudy day
        } else {R.drawable.night_partly_cloudy} // partly cloudy night
        in 1006..1030 -> R.color.gray // clouds/overcast
        in 1063..1117 -> R.drawable.raingradient // rain
        in 1150..1207 -> R.drawable.raingradient // rain
        in 1210..1237 -> R.color.white //snow
        in 1240..1282 -> R.drawable.raingradient // rain
        else -> R.color.white
    }

    if( backgroudColor == R.color.white ||  backgroudColor == R.drawable.sungradient) {
        textColor = R.color.light_black
    }

    /**
     * Country formatting
     */
    when(location.country){
        resource.getString(R.string.USA) -> location.country = "USA"
        resource.getString(R.string.UK) -> location.country = "UK"
    }

    return WeatherDomainObject(
        time = location.localtime,
        location = location.name,
        zipcode = zipcode,
        tempf = current.temp_f.toString().dropLast(2),
        imgSrcUrl = current.condition.icon,
        conditionText = current.condition.text,
        windMph = current.wind_mph,
        windDirection = current.wind_dir,
        backgroundColor =  backgroudColor,
        code = current.condition.code,
        textColor = textColor,
        country = location.country
    )
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun ForecastContainer.asDomainModel(resource: Resources): ForecastDomainObject {

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
                .first().date = resource.getString(R.string.today)
            day.hour
                .forEach { hour ->
                    hour.time = LocalTime.parse(
                        hour.time
                            .substring(11)
                    ) // Remove date stamp
                        .format(
                            DateTimeFormatter
                                .ofPattern(HHMM) // Add AM/PM postfix
                        )
                        .removePrefix("0") // Remove 0 prefix, Ex: Turn 01:00 PM into 1:00PM
                }
        }


    return ForecastDomainObject(
        days = forecast.forecastday,
        alerts = alerts.alert
    )
}

/* TODO
fun Search.asDomainModel(): SearchDomainObject {
    val searchList = mutableListOf<Search>()
    searchList.add(Search().copy())
    return SearchDomainObject(

        searchResults = searchList
    )
}

 */



