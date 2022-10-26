/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.brian.weather.data.BaseApplication
import com.example.weather.R
import com.example.weather.databinding.FragmentAlertListBinding
import com.brian.weather.model.WeatherEntity
import com.brian.weather.ui.adapter.AlertAdapter
import com.brian.weather.ui.adapter.AlertItemViewData
import com.brian.weather.ui.viewmodel.AlertViewModel
import com.brian.weather.ui.viewmodel.ForecastViewData
import com.brian.weather.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A fragment to display the details of a [WeatherEntity] currently stored in the database.
 * The [AddWeatherFragment] can be launched from this fragment to edit the [WeatherEntity]
 */
class AlertFragment : Fragment() {

    private val navigationArgs: AlertFragmentArgs by navArgs()

    // view model to take an instance of
    //  WeatherViewModelFactory. The factory should take an instance of the Database retrieved
    //  from BaseApplication
    private val viewModel: AlertViewModel by activityViewModels {
        AlertViewModel.AlertViewModelFactory(
            (activity?.application as BaseApplication).database.weatherDao(),
            Application()  //TODO passing application now
        )
    }

    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var weatherEntity: WeatherEntity

    private var _binding: FragmentAlertListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlertListBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val zipcode = navigationArgs.zipcode
        viewModel.getWeatherByZipcode(zipcode).observe(this.viewLifecycleOwner) { selectedWeather ->
            weatherEntity = selectedWeather
        }
        val adapter = AlertAdapter()
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
                                it.forecastDomainObject.alerts.map {  // take items in list and submit as new list
                                    AlertItemViewData(it)
                                }
                            )
                            mainViewModel.updateActionBarTitle("${weatherEntity.cityName} Alerts")
                            binding.apply {

                                recyclerView.adapter = adapter
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

