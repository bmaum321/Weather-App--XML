package com.example.weather.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.weather.R

class UnitSettingsFragment : PreferenceFragmentCompat() { // Unit Preferences
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.unit_preferences, rootKey)
    }
}