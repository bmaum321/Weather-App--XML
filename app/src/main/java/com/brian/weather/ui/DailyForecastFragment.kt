package com.brian.weather.ui

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.brian.weather.data.BaseApplication
import com.example.weather.R
import com.example.weather.databinding.FragmentWeatherDetailBinding
import com.brian.weather.model.WeatherEntity
import com.brian.weather.ui.adapter.DaysViewData
import com.brian.weather.ui.viewmodel.ForecastViewData
import com.brian.weather.ui.viewmodel.MainViewModel
import com.brian.weather.ui.viewmodel.WeatherDetailViewModel
import com.brian.weather.ui.adapter.ForecastAdapter
import com.brian.weather.ui.adapter.ForecastItemViewData
import com.brian.weather.ui.viewmodel.withPreferenceConversion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections.emptyList

/**
 * A fragment to display the details of a [WeatherEntity] currently stored in the database.
 * The [AddWeatherFragment] can be launched from this fragment to edit the [WeatherEntity]
 */
class DailyForecastFragment : Fragment() {

    private val navigationArgs: DailyForecastFragmentArgs by navArgs()

    // view model to take an instance of
    //  WeatherViewModelFactory. The factory should take an instance of the Database retrieved
    //  from BaseApplication
    private val viewModel: WeatherDetailViewModel by activityViewModels {
        WeatherDetailViewModel.WeatherDetailViewModelFactory(
            (activity?.application as BaseApplication).database.weatherDao(),
            Application()  //TODO passing application now
        )
    }

    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var weatherEntity: WeatherEntity

    private var _binding: FragmentWeatherDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWeatherDetailBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        binding.lifecycleOwner = viewLifecycleOwner
        binding.alertFab.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val zipcode = navigationArgs.zipcode
        viewModel.getWeatherByZipcode(zipcode).observe(this.viewLifecycleOwner) { selectedWeather ->
            weatherEntity = selectedWeather
        }
        val adapter = ForecastAdapter { viewData ->
            val action = DailyForecastFragmentDirections
                .actionWeatherLocationDetailFragmentToHourlyForecastFragment(
                    zipcode,
                    viewData.day.date
                )
            findNavController().navigate(action)
        }


        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getForecastForZipcode(
                zipcode,
                PreferenceManager.getDefaultSharedPreferences(requireContext()),
                resources
            ).collect {
                withContext(Dispatchers.Main) { //Data binding always done on main thread
                    when (it) {
                        is ForecastViewData.Done -> {
                            adapter.submitList(
                                it.forecastDomainObject.days.map {  // take items in list and submit as new list
                                    ForecastItemViewData(
                                        it,
                                    daysViewData = DaysViewData(
                                        minTemp = "",
                                        maxTemp = ""
                                    )
                                    ).withPreferenceConversion(
                                        PreferenceManager.getDefaultSharedPreferences(requireContext()),
                                        resources
                                    )
                                }
                            )
                            mainViewModel.updateActionBarTitle(weatherEntity.cityName)
                            binding.apply {

                                recyclerView.adapter = adapter
                                // If alerts list is not empty, and show alerts settings is checked show the FAB
                                if(it.forecastDomainObject.alerts.isNotEmpty() &&
                                    PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("alerts", true)) {
                                    alertFab.visibility = View.VISIBLE
                                }
                                alertFab.setOnClickListener {
                                    val action = DailyForecastFragmentDirections
                                        .actionWeatherLocationDetailFragmentToAlertFragment(zipcode)
                                    findNavController().navigate(action)
                                }
                                editWeatherFab.show()
                                editWeatherFab.setOnClickListener {
                                    val action = DailyForecastFragmentDirections
                                        .actionWeatherLocationDetailFragmentToAddWeatherLocationFragment(
                                            weatherEntity.id
                                        )
                                    findNavController().navigate(action)
                                }
                                swipeRefresh.setOnRefreshListener {
                                    refreshScreen()
                                    binding.swipeRefresh.isRefreshing = false
                                }
                                statusImage.visibility = View.GONE
                            }
                        }
                        is ForecastViewData.Error -> {
                            binding.apply {
                                statusImage.setImageResource(R.drawable.ic_connection_error)
                                Log.e("API", "${it.message}")
                                Log.e("API", "${it.code}")
                                adapter.submitList(emptyList())
                                recyclerView.adapter = adapter
                                editWeatherFab.hide()
                                alertFab.hide()
                                swipeRefresh.setOnRefreshListener {
                                    refreshScreen()
                                    binding.swipeRefresh.isRefreshing = false
                                }
                            }
                        }

                        is ForecastViewData.Loading -> {
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

