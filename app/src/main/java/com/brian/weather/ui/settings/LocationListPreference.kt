package com.brian.weather.ui.settings


import android.content.Context
import android.util.AttributeSet
import androidx.preference.MultiSelectListPreference
import com.brian.weather.data.WeatherDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LocationListPreference(context: Context, attrs: AttributeSet?) :
    MultiSelectListPreference(context, attrs) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val locations = WeatherDatabase.getDatabase(context).weatherDao().getZipcodesStatic().toTypedArray()
            entries = locations
            entryValues = locations
        }
    }
}