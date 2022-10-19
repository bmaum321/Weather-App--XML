package com.brian.weather.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.preference.PreferenceManager
import androidx.work.*
import com.brian.weather.data.WeatherDatabase
import com.brian.weather.domain.asDomainModel
import com.brian.weather.network.ApiResponse
import com.brian.weather.repository.WeatherRepository
import com.brian.weather.ui.settings.GetSettings
import com.brian.weather.util.sendForecastNotification
import com.brian.weather.util.sendPrecipitationNotification
import com.example.weather.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyLocalWeatherWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    private val TAGOUTPUT = "Daily Local Weather Call"

    /**
     * Send a daily notification for the weather of the phone's current location
     */

    override fun doWork(): Result {
        var workerResult = Result.success() // worker result is success by default
        var city = ""
        var imgUrl = ""
        var notificationBuilder = ""
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val resources = applicationContext.resources

        // Do some work
        // Only execute and schedule next job if checked in preferences
            val location = inputData.getDoubleArray("location")

            // Only do work if location returned is not null
            if (location != null) {
                // API can take latitude and longitude values separated by comma
                val coordinates = location[0].toString() + "," + location[1].toString()

                val weatherRepository =
                    WeatherRepository(WeatherDatabase.getDatabase(applicationContext))

                CoroutineScope(Dispatchers.IO).launch {
                    when (val response =
                        weatherRepository.getForecast(coordinates)) {
                        is ApiResponse.Success -> {
                            val forecastDomainObject = response.data.asDomainModel(
                                preferences,
                                resources
                            )
                            city = weatherRepository.getWeather(
                                coordinates,
                                resources,
                                preferences
                            ).location
                            //TODO need to check unit settings here
                            var maxTemp = ""
                            var minTemp = ""
                            if (GetSettings().getTemperatureFormatFromPreferences
                                    (
                                    preferences,
                                    resources
                                )
                            ) {
                                maxTemp =
                                    forecastDomainObject.days[0].day.maxtemp_f.toInt().toString()
                                minTemp =
                                    forecastDomainObject.days[0].day.mintemp_f.toInt().toString()
                            } else {
                                maxTemp =
                                    forecastDomainObject.days[0].day.maxtemp_c.toInt().toString()
                                minTemp =
                                    forecastDomainObject.days[0].day.mintemp_c.toInt().toString()
                            }
                            val condition = forecastDomainObject.days[0].day.condition.text
                            imgUrl = forecastDomainObject.days[0].day.condition.icon
                            notificationBuilder += "$minTemp\u00B0 / $maxTemp\u00B0 \u00B7 $condition"


                        }
                        is ApiResponse.Failure -> workerResult =
                            Result.failure() // return worker failure if api call fails
                        is ApiResponse.Exception -> workerResult = Result.failure()
                    }


                    if (notificationBuilder.isNotEmpty()) {
                        createChannel(
                            applicationContext.getString(R.string.forecast_notification_channel_id),
                            applicationContext.getString(R.string.forecast_notification_channel_name)
                        )
                        sendNotification(
                            applicationContext,
                            notificationBuilder,
                            "Today's forecast for $city",
                            imgUrl
                        )
                    }
                }

            }

        return workerResult // can be success or failure depending on API call
    }

    private fun sendNotification(context: Context, text: String, title: String, imgUrl: String) {
        // New instance of notification manager
        val notificationManager = context.let {
            ContextCompat.getSystemService(
                it,
                NotificationManager::class.java
            )
        } as NotificationManager


        if (applicationContext.let {
                PermissionChecker.checkSelfPermission(
                    it,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } == PackageManager.PERMISSION_GRANTED) {
            notificationManager.sendForecastNotification(text, context, title, imgUrl)
        }

    }

    private fun createChannel(channelId: String, channelName: String) {
        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                setShowBadge(false)
            }

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description =
            applicationContext.getString(R.string.local_notification_channel_description)

        val notificationManager = applicationContext.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }

}
