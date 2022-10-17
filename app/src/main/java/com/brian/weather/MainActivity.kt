package com.brian.weather

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
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
import com.brian.weather.util.Constants.TAG_OUTPUT
import com.brian.weather.workers.DailyLocalWeatherWorker
import com.brian.weather.workers.DailyPrecipitationWorker
import com.example.weather.R
import com.example.weather.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * A Main activity that hosts all [Fragment]s for this application and hosts the nav controller.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var hasNotificationPermissionGranted = false
    private var hasLocationPermissionCoarseGranted = false
    private var hasLocationPermissionFineGranted = false


    val permissions =  arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)


    // Request for notifications permission upon runtime
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            isGranted.forEach { entry ->
             //   hasNotificationPermissionGranted = isGranted
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

    private fun showSettingDialog(permission: String) {
        if(permission.contains("POST_NOTIFICATIONS")) {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.notification_permission_dialog_title))
                .setMessage(getString(R.string.notification_permission_required))
                .setPositiveButton("Ok") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.location_permission_dialog_title))
                .setMessage(getString(R.string.location_permission_required))
                .setPositiveButton("Ok") { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

    }

    private fun showNotificationPermissionRationale(permission:String) {
        if(permission.contains("POST_NOTIFICATIONS")) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Alert")
                .setMessage(getString(R.string.notification_permission_dialog))
                .setPositiveButton("Ok") { _, _ ->
                    if (Build.VERSION.SDK_INT >= 33) {
                        notificationPermissionLauncher.launch(permissions)
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
                        notificationPermissionLauncher.launch(permissions) // this may not be needed
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
            notificationPermissionLauncher.launch(permissions)
        }
         // } else {
        //      hasNotificationPermissionGranted = true
        //      hasLocationPermissionCoarseGranted = true
        //      hasLocationPermissionFineGranted = true
        //   }


        // Check location services
        if(!isLocationEnabled()) {
            // settings open here
            Toast.makeText(this, "Turn on location", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }




        setSupportActionBar(binding.toolbar)

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
        //TODO check for prefrences here instead of in the worker itself
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        // Set Execution around 06:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 6)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        val precipitationRequest = PeriodicWorkRequest.Builder(DailyPrecipitationWorker::class.java, 12, TimeUnit.HOURS)
            .setConstraints(constraints)
          //  .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag(TAG_OUTPUT)
            .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(
            "dailyApiCall",
            ExistingPeriodicWorkPolicy.REPLACE,
            precipitationRequest
        )

        /**
         * Daily worker for local weather forecast notifications
         */
        //TODO check for prefrences here instead of in the worker itself
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
            return
        }
        // Get phones location coordinates and pass to the worker as input data
        fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            val location = task.result
            val data = Data.Builder()
            data.putDoubleArray("location", doubleArrayOf(location?.latitude ?: 0.0, location?.longitude ?: 0.0))
            // Set Execution around 06:00:00 AM
            val forecastDueDate = Calendar.getInstance()
            forecastDueDate.set(Calendar.HOUR_OF_DAY, 6)
            forecastDueDate.set(Calendar.MINUTE, 0)
            forecastDueDate.set(Calendar.SECOND, 0)
            if (forecastDueDate.before(currentDate)) {
                forecastDueDate.add(Calendar.HOUR_OF_DAY, 24)
            }
            val timeDiffForecast = forecastDueDate.timeInMillis - currentDate.timeInMillis
            val forecastRequest = PeriodicWorkRequest.Builder(DailyLocalWeatherWorker::class.java, 24, TimeUnit.HOURS)
                .setConstraints(constraints)
                //  .setInitialDelay(timeDiffForecast, TimeUnit.MILLISECONDS)
                .addTag(TAG_OUTPUT)
                .setInputData(data.build())
                .build()
            WorkManager.getInstance().enqueueUniquePeriodicWork(
                "dailyForecast",
                ExistingPeriodicWorkPolicy.REPLACE,
                forecastRequest
            )
        }

    }

    private fun getCurrentLocation(): Pair<Double, Double>? {
        var location: Location? = null
        if (checkLocationPermissions()) {
            if (isLocationEnabled()) {
                // final latitude and longitude values retrieved here
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestLocationPermissions()
                }
                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    location = task.result
                    if (location == null) {
                        Toast.makeText(this, "Null Received", Toast.LENGTH_SHORT).show()
                    } else {

                        Toast.makeText(this, "Location Retrieved", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                // settings open here
                Toast.makeText(this, "Turn on location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

        } else {
            // request location permission here if not granted
            requestLocationPermissions()
        }
        return location?.let { Pair(it.latitude, it.longitude) }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            ),

            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    private fun checkLocationPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    /*

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(applicationContext, "Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

     */


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
}


