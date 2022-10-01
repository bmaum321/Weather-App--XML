package com.example.weather.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.weather.R

class InterfaceSettingsFragment : PreferenceFragmentCompat() { // Unit Preferences
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.interface_preferences, rootKey)
    }
}