package com.brian.weather.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.databinding.BindingAdapter
import coil.load
import com.brian.weather.MainActivity
import com.example.weather.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL


// Notification ID.
private val NOTIFICATION_ID = 0
private val FORECAST_NOTIFICATION_ID = 1
private val REQUEST_CODE = 0
private val FLAGS = 0

// extension function to send messages
/**
 * Builds and delivers the notification.
 *
 * @param context, activity context.
 */
fun NotificationManager.sendPrecipitationNotification(
    messageBody: String,
    applicationContext: Context
) {
    // Create the content intent for the notification, which launches
    // this activity
    // create intent
    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    // create PendingIntent
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    // add style
    val rainImage = BitmapFactory.decodeResource(
        applicationContext.resources,
        R.drawable.ic_rain_svgrepo_com
    )
    val bigTextStyle = NotificationCompat.BigTextStyle()

    // get an instance of NotificationCompat.Builder
    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.precipitation_notification_channel_id)
    )


        // set title, text and icon to builder
        .setSmallIcon(R.drawable.ic_rain_svgrepo_com)
        .setContentTitle(
            applicationContext
                .getString(R.string.notification_title)
        )
        .setContentText(messageBody)

        // set content intent
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)

        // add style to builder
        .setStyle(bigTextStyle)
        .setLargeIcon(rainImage)

        // set priority
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    // call notify
    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.sendForecastNotification(
    messageBody: String,
    applicationContext: Context,
    title: String, imageUrl: String
) {
    // Create the content intent for the notification, which launches
    // this activity
    // create intent
    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    // create PendingIntent
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        FORECAST_NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    // add style
    val rainImage = BitmapFactory.decodeResource(
        applicationContext.resources,
        R.mipmap.ic_launcher_round

    )

    val bigTextStyle = NotificationCompat.BigTextStyle()

    // get an instance of NotificationCompat.Builder
    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.forecast_notification_channel_id)
    )


        // set title, text and icon to builder
        .setSmallIcon(R.drawable.ic_rain_svgrepo_com)
        .setContentTitle(title)
        .setContentText(messageBody)

        // set content intent
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)

        // add style to builder
        .setStyle(bigTextStyle)
        //  .setLargeIcon(rainImage)


        // set priority
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    applyImageUrl(builder, imageUrl)
    // call notify
    notify(FORECAST_NOTIFICATION_ID, builder.build())

}

// Function to load image from URL and apply as notification icon

fun applyImageUrl(
    builder: NotificationCompat.Builder,
    imageUrl: String
) = runBlocking {
    val url = URL("https:$imageUrl")

    withContext(Dispatchers.IO) {
        try {
            val input = url.openStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            null
        }
    }?.let { bitmap ->
        builder.setLargeIcon(bitmap)
    }
}
