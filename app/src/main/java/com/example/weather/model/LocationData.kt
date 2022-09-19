package com.example.weather.model

data class LocationData(
    var name: String,
    var region: String,
    var country: String,
    var lat: Double,
    var lon: Double,
    var tz_id: String,
    var localtime_epoch: Float,
    var localtime: String
)