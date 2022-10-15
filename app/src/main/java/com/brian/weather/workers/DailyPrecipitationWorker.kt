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
import com.brian.weather.util.sendNotification
import com.example.weather.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.AccessController.getContext
import java.util.*
import java.util.concurrent.TimeUnit

class DailyPrecipitationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    private val TAGOUTPUT = "Daily API Call"

    /**
     * Testing the sending of a notification through Work manager API
     * Goal is to call API here and send notifications based off local settings and precipitation values
     */

    override fun doWork(): Result {
        var workerResult = Result.success() // worker result is success by default

        // Should this be a coroutine worker instead?
        // Should this live in a viewmodel? I want this to get intansiated at runtime (mainviewmodel?)


        // Do some work
        // Only execute and schedule next job if show notifications is checked in preferences
        if (PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getBoolean(
                    applicationContext.getString(R.string.show_precipitation_notifications),
                    true
                )
        ) {
            // Make copy of preferences
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val editor = sharedPref.edit()
            // Get list of selected locations to track from preferences
            val setFromSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(applicationContext).getStringSet(
                    "locations",
                    null
                )
            val copyOfSet = setFromSharedPreferences?.toMutableSet()
            val selectedLocations =
                setFromSharedPreferences//?.toList()?.drop(2) // the getstringset adds 2 numbers to the beginning of the collection? now it isnt?

            //TODO there is a bug here, becasue it can add locations that arent checked in preferences. The main issue
            // here seems to be when a location is deleted from the database, it isn't deleted from the preferences
            // Put copy back in preferences
            val newLocationsFromDB = WeatherDatabase.getDatabase(applicationContext).weatherDao().getZipcodesStatic().toMutableSet()
            editor.putStringSet("locations", newLocationsFromDB)
            editor.commit()


            /**
             * seems like I cant modify collection without creating issues. I need to make a copy of it
             * Issue documented here:
             * http://developer.android.com/reference/android/content/SharedPreferences.html#getStringSet%28java.lang.String,%20java.util.Set%3Cjava.lang.String%3E%29
             */

            val weatherRepository =
                WeatherRepository(WeatherDatabase.getDatabase(applicationContext))

            var notificationBuilder = ""


            CoroutineScope(Dispatchers.IO).launch {

                // Get locations from database
                //  val locations =
                //     WeatherDatabase.getDatabase(applicationContext).weatherDao().getZipcodesStatic()
                //    val weatherDomainObjects = mutableListOf<WeatherDomainObject>()
                // Check if database is empty
                if (selectedLocations != null) {
                    if (selectedLocations.isNotEmpty()) {
                        selectedLocations.forEach { println(it) }
                        selectedLocations.forEach { location -> // for each selected notification send a precipitation notification
                            when (val response =
                                weatherRepository.getForecast(location)) {
                                is ApiResponse.Success -> {
                                    val forecastDomainObject = response.data.asDomainModel(
                                        PreferenceManager.getDefaultSharedPreferences(
                                            applicationContext
                                        ),
                                        applicationContext.resources
                                    )
                                    val willItRainToday = mutableListOf<Int>()
                                    forecastDomainObject.days.first().hour.forEach { hour ->
                                        willItRainToday.add(hour.will_it_rain)
                                    }
                                    val willItSnowToday = mutableListOf<Int>()
                                    forecastDomainObject.days.first().hour.forEach { hour ->
                                        willItRainToday.add(hour.will_it_snow)
                                    }

                                    /**
                                     * If it will rain today, send notification for first matching time
                                     * in hourly forecast. If it will snow more than rain, send notification
                                     * for first matching time in hourly forecast.
                                     */
                                    if (willItRainToday.contains(1) || willItSnowToday.contains(1)) {
                                        val timeOfRain =
                                            forecastDomainObject.days.first().hour.firstOrNull { it.will_it_rain == 1 }?.time
                                        val hoursWithRain = willItRainToday.count { it == 1 }
                                        val timeOfSnow =
                                            forecastDomainObject.days.first().hour.firstOrNull { it.will_it_snow == 1 }?.time
                                        val hoursWithSnow = willItSnowToday.count { it == 1 }

                                        notificationBuilder += if (hoursWithRain > hoursWithSnow) {
                                            "Expect Rain for $location around $timeOfRain\n "
                                        } else {
                                            "Expect Snow for $location around $timeOfSnow\n"
                                        }
                                    }
                                }
                                is ApiResponse.Failure -> workerResult =
                                    Result.failure() // return worker failure if api call fails
                                is ApiResponse.Exception -> workerResult = Result.failure()
                            }

                        }
                    }
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
            // Set Execution around 05:00:00 AM
            dueDate.set(Calendar.HOUR_OF_DAY, 5)
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

            val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyPrecipitationWorker>()
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
            applicationContext.getString(R.string.precipitation_notification_channel_description)

        val notificationManager = applicationContext.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }

}
