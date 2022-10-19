package com.brian.weather.ui.settings

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.navigation.fragment.findNavController
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.weather.R
import com.brian.weather.util.Constants
import com.example.weather.BuildConfig
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : PreferenceFragmentCompat() {



    private fun showAboutDialog(){
        // Create the object of AlertDialog Builder class
        val builder = AlertDialog.Builder(context)

        // Set the message show for the Alert time
        builder.setMessage("Thanks for trying my app! \n bmaum1@gmail.com" )

        // Set Alert Title
        builder.setTitle(BuildConfig.VERSION_NAME)

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(true)

        // Create the Alert dialog
        val alertDialog = builder.create()
        // Show the Alert Dialog box
        alertDialog.show()

    }

    // Main preferences
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        findPreference<Preference>("units")?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_unitSettingsFragment)
            true
        }
        findPreference<Preference>("interface")?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_interfaceSettingsFragment)
            true
        }
        findPreference<Preference>("notifications")?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_notificationSettingsFragment)
            true
        }
        findPreference<Preference>("about")?.setOnPreferenceClickListener {
            showAboutDialog()
            true
        }


    }


}
