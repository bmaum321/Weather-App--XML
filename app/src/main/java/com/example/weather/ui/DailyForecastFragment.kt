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
package com.example.weather.ui

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.weather.BaseApplication
import com.example.weather.R
import com.example.weather.databinding.FragmentWeatherDetailBinding
import com.example.weather.model.WeatherEntity
import com.example.weather.ui.adapter.ForecastAdapter
import com.example.weather.ui.viewmodel.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val zipcode = navigationArgs.zipcode
        viewModel.getWeatherByZipcode(zipcode).observe(this.viewLifecycleOwner) { selectedWeather ->
            weatherEntity = selectedWeather
        }
        val adapter = ForecastAdapter { day ->
            val action = DailyForecastFragmentDirections
                .actionWeatherLocationDetailFragmentToHourlyForecastFragment(zipcode, day.date) //TODO how do we pass the date from the clicked option...
            findNavController().navigate(action)
        }


        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getForecastForZipcode(zipcode).collect {
                withContext(Dispatchers.Main) { //Data binding always done on main thread
                    when (it) {
                        is ForecastViewData.Done -> {
                            adapter.submitList(it.forecastDomainObject.days)
                            mainViewModel.updateActionBarTitle(weatherEntity.cityName)
                            binding.apply {

                                recyclerView.adapter = adapter
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
                                adapter.submitList(emptyList())
                                recyclerView.adapter = adapter
                                editWeatherFab.hide()
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

        /*
    private fun launchMap(weatherDomainObject: WeatherDomainObject) {
        val address = weatherDomainObject.zipcode.let {
            it.replace(", ", ",")
            it.replace(". ", " ")
            it.replace(" ", "+")
        }
        val gmmIntentUri = Uri.parse("geo:0,0?q=$address")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

            */

    }

    private fun refreshScreen() {
        viewModel.refresh()
    }
}

