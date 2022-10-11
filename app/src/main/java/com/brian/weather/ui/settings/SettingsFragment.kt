package com.brian.weather.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.weather.R
import com.brian.weather.util.Constants

class SettingsFragment : PreferenceFragmentCompat() {



    private fun showAboutDialog(){
        // Create the object of AlertDialog Builder class
        val builder = AlertDialog.Builder(context)

        // Set the message show for the Alert time
        builder.setMessage("Thanks for trying my app! \n bmaum1@gmail.com" )

        // Set Alert Title
        builder.setTitle(Constants.VERSION)

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
        findPreference<Preference>("about")?.setOnPreferenceClickListener {
            showAboutDialog()
            true
        }


    }


}
