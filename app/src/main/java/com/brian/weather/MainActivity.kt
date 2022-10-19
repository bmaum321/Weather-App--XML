package com.brian.weather

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import androidx.work.*
import com.brian.weather.ui.viewmodel.MainViewModel
import com.brian.weather.util.Constants
import com.brian.weather.util.Constants.TAG_OUTPUT
import com.brian.weather.workers.DailyLocalWeatherWorker
import com.brian.weather.workers.DailyPrecipitationWorker
import com.brian.weather.workers.JobScheduler
import com.example.weather.R
import com.example.weather.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * A Main activity that hosts all [Fragment]s for this application and hosts the nav controller.
 * Prompts for permissions at runtime
 * Schedules work manager jobs if settings are enabled
 * Adds listeners to shared preferences to prompt for background location
 */
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val permissions =  arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACCESS_COARSE_LOCATION)


    // Request for notifications permission upon runtime
     val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            isGranted.forEach { entry ->
                if (!entry.value) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (shouldShowRequestPermissionRationale(entry.key)) {
                            showNotificationPermissionRationale(entry.key)
                        } else {
                            showSettingDialog(entry.key)
                        }
                    }
                }
            }

        }

    /**
     * Ask for background location if user checks show local forecast setting, registered in on resume
     * and un registered in on pause. This is to avoid multiple instances getting created everytime the
     * activity is restarted. SharedPreferences keeps listeners in a WeakHashMap.
     * This means that you cannot use an anonymous inner class as a listener, as it will become the
     * target of garbage collection as soon as you leave the current scope.
     */
    //
    private val localForecastPreferenceListener =
        OnSharedPreferenceChangeListener { prefs, key ->
            if (key == this.getString(R.string.show_local_forecast)) {
                if (!checkBackgroundLocationPermissions()) {
                    if(prefs.getBoolean(this.getString(R.string.show_local_forecast), false)) {
                        JobScheduler().scheduleForecastJob(this)
                        showAlertDialog(
                            getString(R.string.background_location_permission_dialog_title)
                            ,getString(R.string.background_location_permission_required),
                        )
                    }
                } else if (!isLocationEnabled()) {
                    showAlertDialog(
                        getString(R.string.location_services_required_dialog_title)
                        ,getString(R.string.location_services_required_dialog),
                    )
                }
            }
        }

    // If build >= API 33, check to see if notifications enabled when setting checked
    private val notificationPreferenceListener =
        OnSharedPreferenceChangeListener { prefs, key ->
            if (key == this.getString(R.string.show_notifications)) {
                if (Build.VERSION.SDK_INT >= 33) {
                    if (!checkNotificationPermissions()) {
                        if (prefs.getBoolean(this.getString(R.string.show_notifications), false)) {
                            permissionLauncher.launch(permissions)
                        }
                    }
                }
            }
        }

    // Schedule new precipitation notification job if new locations added from preferences
    private val precipitationPreferenceListener =
        OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "locations") {
             // schedulePrecipitationJob()
                JobScheduler().schedulePrecipitationJob(this)
            }
        }


    private fun showSettingDialog(permission: String) {
        if(permission.contains("POST_NOTIFICATIONS")) {
            showAlertDialog(
                getString(R.string.notification_permission_dialog_title),
                (getString(R.string.notification_permission_required))
            )

        } else {
            showAlertDialog(
                getString(R.string.location_permission_dialog_title),
                getString(R.string.location_permission_required),
            )
        }
    }

    private fun showNotificationPermissionRationale(permission:String) {
        if(permission.contains("POST_NOTIFICATIONS")) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Alert")
                .setMessage(getString(R.string.notification_permission_dialog))
                .setPositiveButton("Ok") { _, _ ->
                    if (Build.VERSION.SDK_INT >= 33) {
                        permissionLauncher.launch(permissions)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle("Alert")
                .setMessage(getString(R.string.location_permission_dialog))
                .setPositiveButton("Ok") { _, _ ->
                    if (Build.VERSION.SDK_INT >= 33) {
                        permissionLauncher.launch(permissions) // this may not be needed
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Display dialog to allow permissions on launch
        //  if (Build.VERSION.SDK_INT >= 33) {
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("show_notifications", true)) {
            permissionLauncher.launch(permissions)
        }


        // Setup action bar
        setSupportActionBar(binding.toolbar)

        // Setup nav controller
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        /**
         * Main view model with title value and method to update action bar title
         */

        val mainViewModel: MainViewModel by viewModels()
        mainViewModel.title.observe(this) {
            supportActionBar?.title = it
        }

        /**
         * Daily worker for precipitation notifications
         */
        JobScheduler().schedulePrecipitationJob(this)

        /**
         * Daily worker for forecast notifications
         */
        JobScheduler().scheduleForecastJob(this)

    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // Check if background location is enabled for forecast alerts
    private fun checkBackgroundLocationPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED)  {
            return true
        }
        return false
    }

    // Check if notifications is enabled for notifications setting
    private fun checkNotificationPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            == PackageManager.PERMISSION_GRANTED)  {
            return true
        }
        return false
    }

    private fun showAlertDialog(
        title: String,
        message: String,
        ) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                /**
                 * The open_settings_fragment is a global action defined in nav_graph.xml
                 */
                Navigation.findNavController(
                    this,
                    R.id.nav_host_fragment_content_main
                ).navigate(R.id.open_settings_fragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(localForecastPreferenceListener)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(notificationPreferenceListener)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(precipitationPreferenceListener)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(localForecastPreferenceListener)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(notificationPreferenceListener)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(precipitationPreferenceListener)
    }
}


