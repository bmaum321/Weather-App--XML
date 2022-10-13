package com.brian.weather.workers

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.preference.PreferenceManager
import androidx.work.*
import com.brian.weather.data.WeatherDao
import com.brian.weather.data.WeatherDatabase
import com.brian.weather.repository.WeatherRepository
import com.brian.weather.util.sendNotification
import com.example.weather.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class DailyWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    private val TAGOUTPUT = "Daily API Call"

    /**
     * Testing the sending of a notification through Work manager API
     * Goal is to call API here and send notfiictions based off local settings and precipitation values
     */

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

    override fun doWork(): Result {

        //Jobs will fail if I pass wewather dao in worker primary constructor, so not sure how I can
        //make API calls here without restructuring or moving  this into one of the view models
        // Do some work
        if (PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getBoolean(
                    applicationContext.getString(R.string.show_precipitation_notifications),
                    true
                )
        ) {
            val weatherRepository = WeatherRepository(WeatherDatabase.getDatabase(Application()))
            createChannel("WorkManager Channel", "WorkManager")
            sendNotification(applicationContext, "From Work Manager: Expect Rain Around 7:00PM")

            CoroutineScope(Dispatchers.IO).launch {
              //  val locations = weatherDao.getZipcodesStatic()
                val response = weatherRepository.getForecast("13088") //TODO test
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

        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(TAGOUTPUT)
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "nestedDailyApiCall",
                ExistingWorkPolicy.KEEP,
                dailyWorkRequest)
        return Result.success()
    }
}