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
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.weather.BaseApplication
import com.example.weather.R
import com.example.weather.databinding.FragmentWeatherDetailBinding
import com.example.weather.domain.WeatherDomainObject
import com.example.weather.model.WeatherEntity
import com.example.weather.ui.viewmodel.WeatherDetailViewModel
import com.example.weather.ui.viewmodel.WeatherViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A fragment to display the details of a [WeatherEntity] currently stored in the database.
 * The [AddWeatherFragment] can be launched from this fragment to edit the [WeatherEntity]
 */
class WeatherDetailFragment : Fragment() {

    private val navigationArgs: WeatherDetailFragmentArgs by navArgs()

    // view model to take an instance of
    //  WeatherViewModelFactory. The factory should take an instance of the Database retrieved
    //  from BaseApplication
    private val viewModel: WeatherDetailViewModel by activityViewModels {
        WeatherDetailViewModel.WeatherDetailViewModelFactory(
            (activity?.application as BaseApplication).database.weatherDao(),
            Application()  //TODO passing application now
        )
    }


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
        binding.viewModel = viewModel // TODO TESTING HERE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        //TODO this is old code, need to try and pass something else as ID to the add fragment
        val id = navigationArgs.id
        viewModel.getWeatherById(id).observe(this.viewLifecycleOwner) { selectedWeather ->
            weatherEntity = selectedWeather
        }

         */


        val zipcode =
            navigationArgs.zipcode // TODO this was changed, how is this getting the zipcode???
        viewModel.getWeatherByZipcode(zipcode).observe(this.viewLifecycleOwner) { selectedWeather ->
            weatherEntity = selectedWeather
        }
        // Collect the flow and call the bind weather method
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getWeatherForZipcode(zipcode).collect {
                withContext(Dispatchers.Main) { //Data binding always done on main thread
                    bindWeather(it)
                }
            }
        }
    }

    private fun bindWeather(weatherViewData: WeatherViewData) {
        when (weatherViewData) {
            is WeatherViewData.Done -> {
                binding.apply {
                    name.text = weatherViewData.weatherDomainObject.location
                    location.text = weatherViewData.weatherDomainObject.zipcode
                    tempF.text = weatherViewData.weatherDomainObject.tempf.toString()
                    conditionText.text = weatherViewData.weatherDomainObject.conditionText
                    windMph.text = weatherViewData.weatherDomainObject.windMph.toString()
                    windDirection.text = weatherViewData.weatherDomainObject.windDirection
                    statusImage.visibility = View.GONE
                    editWeatherFab.setOnClickListener {
                        val action = WeatherDetailFragmentDirections
                            .actionWeatherLocationDetailFragmentToAddWeatherLocationFragment(
                                weatherEntity.id
                            )
                        findNavController().navigate(action)
                    }

                    location.setOnClickListener {
                        launchMap(weatherViewData.weatherDomainObject)
                    }
                }
            }
            is WeatherViewData.Error -> {
                binding.apply {
                    statusImage.setImageResource(R.drawable.ic_connection_error)
                    dividerConditionText.visibility = View.GONE
                    dividerLocation.visibility = View.GONE
                    dividerSeason.visibility = View.GONE
                    dividerWindMph.visibility = View.GONE
                    icCalendar.visibility = View.GONE
                    icLocation.visibility = View.GONE
                    icConditionText.visibility = View.GONE
                    icWindMph.visibility = View.GONE
                }
            }
            is WeatherViewData.Loading -> {
                binding.apply {
                    statusImage.setImageResource(R.drawable.loading_animation)
                }
            }
        }
    }

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
}

