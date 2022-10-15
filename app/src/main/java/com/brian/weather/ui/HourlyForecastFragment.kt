package com.brian.weather.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.brian.weather.data.BaseApplication
import com.example.weather.R
import com.example.weather.databinding.FragmentHourlyForecastBinding
import com.brian.weather.model.WeatherEntity
import com.brian.weather.ui.adapter.HourlyForecastAdapter
import com.brian.weather.ui.adapter.HourlyForecastItemViewData
import com.brian.weather.ui.viewmodel.*
import com.brian.weather.util.sendNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A fragment to display the details of a [WeatherEntity] currently stored in the database.
 * The [AddWeatherFragment] can be launched from this fragment to edit the [WeatherEntity]
 */
class HourlyForecastFragment : Fragment() {

    private val navigationArgs: HourlyForecastFragmentArgs by navArgs()

    // view model to take an instance of
    //  WeatherViewModelFactory. The factory should take an instance of the Database retrieved
    //  from BaseApplication
    private val viewModel: HourlyForecastViewModel by activityViewModels {
        HourlyForecastViewModel.HourlyForecastViewModelFactory(
            (activity?.application as BaseApplication).database.weatherDao(),
            Application()  //TODO passing application now
        )
    }

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var weatherEntity: WeatherEntity
    private var _binding: FragmentHourlyForecastBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHourlyForecastBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val date = navigationArgs.date
        val zipcode =
            navigationArgs.zipcode
        viewModel.getWeatherByZipcode(zipcode).observe(this.viewLifecycleOwner) { selectedWeather ->
            weatherEntity = selectedWeather
        }
        val adapter = HourlyForecastAdapter {
            val action = DailyForecastFragmentDirections
                .actionWeatherLocationDetailFragmentToAddWeatherLocationFragment()
            findNavController().navigate(action)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getForecastForZipcode(zipcode,
                PreferenceManager.getDefaultSharedPreferences(requireContext()),
                resources).collect {
                withContext(Dispatchers.Main) { //Data binding always done on main thread
                    when (it) {
                        is HourlyForecastViewData.Done -> {
                            /**
                             * Find hour list matching the date passed from the previous fragment
                             * and submit to the list adapter for display
                             */
                            adapter.submitList(it.forecastDomainObject.days
                                .first { it.date == date }.hour.map {
                                HourlyForecastItemViewData(it)
                                    .withPreferenceConversion(PreferenceManager.getDefaultSharedPreferences(requireContext()),
                                        resources)
                            })
                            mainViewModel.updateActionBarTitle(it.forecastDomainObject.days
                                .first { it.date == date }.date) // Update title bar with day of week
                            binding.apply {
                                recyclerView.adapter = adapter
                                swipeRefresh.setOnRefreshListener {
                                    refreshScreen()
                                    binding.swipeRefresh.isRefreshing = false
                                }
                                statusImage.visibility = View.GONE
                            }

                        }
                        is HourlyForecastViewData.Error -> {
                            binding.apply {
                                statusImage.setImageResource(R.drawable.ic_connection_error)
                                adapter.submitList(emptyList())
                                recyclerView.adapter = adapter
                                swipeRefresh.setOnRefreshListener {
                                    refreshScreen()
                                    binding.swipeRefresh.isRefreshing = false
                                }
                            }
                        }

                        is HourlyForecastViewData.Loading -> {
                            binding.apply {
                                statusImage.setImageResource(R.drawable.loading_animation)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun refreshScreen() {
        viewModel.refresh()
    }
}



