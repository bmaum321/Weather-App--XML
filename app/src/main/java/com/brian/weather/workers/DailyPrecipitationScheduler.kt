package com.brian.weather.workers

import android.app.Activity
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.work.*
import com.brian.weather.util.Constants
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Daily worker for precipitation notifications
 */
//TODO need to enque a new worker if preference is changed

// Only execute and schedule next job if show notifications is checked in preferences
class JobScheduler(): AppCompatActivity() {
    fun schedulePrecipitationJob() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean(resources.getString(com.example.weather.R.string.show_notifications), true) &&
            preferences.getBoolean(resources.getString(com.example.weather.R.string.show_precipitation_notifications), true)
        ) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance()
            // Set Execution around 06:00:00 AM
            dueDate.set(Calendar.HOUR_OF_DAY, 6)
            dueDate.set(Calendar.MINUTE, 0)
            dueDate.set(Calendar.SECOND, 0)
            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.HOUR_OF_DAY, 24)
            }
            val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
            val precipitationRequest = PeriodicWorkRequest.Builder(
                DailyPrecipitationWorker::class.java,
                12,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag(Constants.TAG_OUTPUT)
                .build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "dailyApiCall",
                ExistingPeriodicWorkPolicy.REPLACE,
                precipitationRequest
            )
        }
    }
}

