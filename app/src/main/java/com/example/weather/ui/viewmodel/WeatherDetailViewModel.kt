package com.example.weather.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.network.WeatherContainer

class WeatherDetailViewModel: ViewModel() {

    // Internally, we use a MutableLiveData, because we will be updating the List of MarsPhoto
    // with new values
    private val _weatherData = MutableLiveData<WeatherContainer>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val weatherData: LiveData<WeatherContainer> = _weatherData

    fun getWeather() {

    }
}