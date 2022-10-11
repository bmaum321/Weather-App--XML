package com.brian.weather.ui.settings

import android.content.SharedPreferences
import android.content.res.Resources
import com.example.weather.R

private const val TWELVE_HOUR = "hh:mm a"
private const val TWENTY_FOUR_HOUR = "kk:mm"

class GetSettings() {
    fun getTimeFormatFromPreferences(
        sharedPreferences: SharedPreferences,
        resource: Resources
    ): String {
        var timeFormat = TWELVE_HOUR
        val clockFormatPreference = sharedPreferences.getString("clock_format", "") // TODO Should all of these string resources be extracted? Even key values in preferences?
        if (clockFormatPreference == resource.getString(R.string.twenty_four_hour)) {
            timeFormat = TWENTY_FOUR_HOUR
        }
        return timeFormat
    }

    fun getTemperatureFormatFromPreferences(
        sharedPreferences: SharedPreferences,
        resources: Resources
    ): Boolean {
        var tempFormat = true
        val tempFormatPreference = sharedPreferences.getString("temperature_unit", "")
        if (tempFormatPreference == "c") {
            tempFormat = false
        }
        return tempFormat
    }

    fun getWindSpeedFormatFromPreferences(
        sharedPreferences: SharedPreferences,
        resources: Resources
    ): Boolean {
        var windSpeedFormat = true
        val tempFormatPreference = sharedPreferences.getString("wind", "")
        if (tempFormatPreference == "kph") {
            windSpeedFormat = false
        }
        return windSpeedFormat
    }

    fun getMeasurementFormatFromPreferences(
        sharedPreferences: SharedPreferences,
        resources: Resources
    ): Boolean {
        var measurementFormat = true
        val measurementFormatPreference = sharedPreferences.getString("precipitation", "")
        if (measurementFormatPreference == "mm") {
            measurementFormat = false
        }
        return measurementFormat
    }

}

