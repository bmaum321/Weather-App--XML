package com.brian.weather.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.weather.R

class NotificationSettingsFragment : PreferenceFragmentCompat() { // Notification Preferences
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.notification_preferences, rootKey)
    }
}