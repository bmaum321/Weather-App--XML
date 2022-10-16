package com.brian.weather.workers

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.preference.PreferenceManager
import androidx.work.*
import com.brian.weather.data.WeatherDatabase
import com.brian.weather.domain.asDomainModel
import com.brian.weather.network.ApiResponse
import com.brian.weather.repository.WeatherRepository
import com.brian.weather.util.sendNotification
import com.example.weather.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable
import com.google.android.gms.common.api.GoogleApiActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.AccessController.getContext
import java.util.*
import java.util.concurrent.TimeUnit

class DailyLocalWeatherWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    private val TAGOUTPUT = "Daily Local Weather Call"

    /**
     * Send a daily notification for the weather of the phone's current location
     */

    override fun doWork(): Result {
        var workerResult = Result.success() // worker result is success by default


        // Do some work
        // Only execute and schedule next job if checked in preferences
        if (PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getBoolean(
                    applicationContext.getString(R.string.show_local_forecast),
                    true
                )
        ) {

            val weatherRepository =
                WeatherRepository(WeatherDatabase.getDatabase(applicationContext))

            var notificationBuilder = ""


            val location = "13088"


            CoroutineScope(Dispatchers.IO).launch {
                            when (val response =
                                weatherRepository.getForecast(location)) {
                                is ApiResponse.Success -> {
                                    val forecastDomainObject = response.data.asDomainModel(
                                        PreferenceManager.getDefaultSharedPreferences(
                                            applicationContext
                                        ),
                                        applicationContext.resources
                                    )
                                }
                                is ApiResponse.Failure -> workerResult =
                                    Result.failure() // return worker failure if api call fails
                                is ApiResponse.Exception -> workerResult = Result.failure()
                            }


                if (notificationBuilder.isNotEmpty()) {
                    createChannel(applicationContext.getString(R.string.precipitation_notification_channel_id), "Precipitation Notifications")
                    sendNotification(
                        applicationContext,
                        notificationBuilder
                    )
                }
            }


            // The worker will enqueue the next execution of this work when we complete successfully
            // This is more time accurate than a periodic work request?
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance()
            // Set Execution around 08:00:00 PM
            dueDate.set(Calendar.HOUR_OF_DAY, 20)
            dueDate.set(Calendar.MINUTE, 0)
            dueDate.set(Calendar.SECOND, 0)
            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.HOUR_OF_DAY, 24)
            }
            val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyLocalWeatherWorker>()
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag(TAGOUTPUT)
                .build()
            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(
                    "nestedDailyApiCall",
                    ExistingWorkPolicy.KEEP,
                    dailyWorkRequest
                )

        }
        return workerResult // can be success or failure depending on API call
    }

    private fun sendNotification(context: Context, text: String) {
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
            notificationManager.sendNotification(text, context)
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
