package com.brian.weather.ui.settings

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import android.util.AttributeSet
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.preference.MultiSelectListPreference
import com.brian.weather.data.BaseApplication
import com.brian.weather.data.WeatherDao
import com.brian.weather.data.WeatherDatabase
import com.brian.weather.ui.viewmodel.MainViewModel
import com.brian.weather.ui.viewmodel.WeatherListViewModel
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