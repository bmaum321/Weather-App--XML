package com.example.weather.util

import android.view.View
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import coil.load
import com.example.weather.R
import com.example.weather.ui.viewmodel.WeatherViewData

/**
 * Binding adapter used to hide the spinner once data is available.
 */
@BindingAdapter("isNetworkError", "playlist")
fun hideIfNetworkError(view: View, isNetWorkError: Boolean, playlist: Any?) {
    view.visibility = if (playlist != null) View.GONE else View.VISIBLE
    if(isNetWorkError) {
        view.visibility = View.GONE
    }
}

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        imgView.load(imgUri)
    }
}

/*
@BindingAdapter("WeatherViewData")
fun bindStatus(statusImageView: ImageView,
               status: WeatherViewData?) {
    when (status) {
        WeatherViewData.Loading() -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.loading_animation)
        }
        WeatherViewData.Error() -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_connection_error)
        }
        WeatherViewData.Done() -> {
            statusImageView.visibility = View.GONE
        }
    }
}

 */
