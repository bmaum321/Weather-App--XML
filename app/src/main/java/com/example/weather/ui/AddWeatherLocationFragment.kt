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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.weather.BaseApplication
import com.example.weather.R
import com.example.weather.databinding.FragmentAddWeatherLocationBinding
import com.example.weather.model.Weather
import com.example.weather.ui.viewmodel.WeatherViewModel
import com.example.weather.ui.viewmodel.WeatherViewModelFactory


/**
 * A fragment to enter data for a new [Weather] or edit data for an existing [Weather].
 * [Weather]s can be saved or deleted from this fragment.
 */
class AddWeatherLocationFragment : Fragment() {

    private val navigationArgs: AddWeatherLocationFragmentArgs by navArgs()

    private var _binding: FragmentAddWeatherLocationBinding? = null

    private lateinit var weather: Weather

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // TODO: Refactor the creation of the view model to take an instance of
    //  WeatherViewModelFactory. The factory should take an instance of the Database retrieved
    //  from BaseApplication
    private val viewModel: WeatherViewModel by activityViewModels{
        WeatherViewModelFactory(
            (activity?.application as BaseApplication).database.weatherDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddWeatherLocationBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = navigationArgs.id
        if (id > 0) {

            // TODO: Observe a Weather that is retrieved by id, set the forageable variable,
            //  and call the bindForageable method
            viewModel.getWeatherById(id).observe(this.viewLifecycleOwner) { selectedWeather ->
               //forageable = selectedForageable
              //  bindForageable(forageable)
                if (selectedWeather != null) {
                    weather = selectedWeather
                    bindWeather(weather)
                }
            }

            binding.deleteBtn.visibility = View.VISIBLE
            binding.deleteBtn.setOnClickListener {
                deleteWeather(weather)
            }
        } else {
            binding.saveBtn.setOnClickListener {
                addWeather()
            }
        }
    }

    private fun deleteWeather(forageable: Weather) {
        viewModel.deleteWeather(forageable)
        findNavController().navigate(
            R.id.action_addWeatherFragment_to_WeatherListFragment
        )
    }

    private fun addWeather() {
        if (isValidEntry()) {
            viewModel.addWeather(
                binding.nameInput.text.toString(),
                binding.locationAddressInput.text.toString(),
                binding.inSeasonCheckbox.isChecked,
                binding.notesInput.text.toString()
            )
            findNavController().navigate(
                R.id.action_addWeatherFragment_to_WeatherListFragment
            )
        }
    }

    private fun updateWeather() {
        if (isValidEntry()) {
            viewModel.updateWeather(
                id = navigationArgs.id,
                name = binding.nameInput.text.toString(),
                address = binding.locationAddressInput.text.toString(),
                inSeason = binding.inSeasonCheckbox.isChecked,
                notes = binding.notesInput.text.toString()
            )
            findNavController().navigate(
                R.id.action_addWeatherFragment_to_WeatherListFragment
            )
        }
    }

    private fun bindWeather(forageable: Weather) {
        binding.apply{
            nameInput.setText(forageable.name, TextView.BufferType.SPANNABLE)
            locationAddressInput.setText(forageable.address, TextView.BufferType.SPANNABLE)
            inSeasonCheckbox.isChecked = forageable.inSeason
            notesInput.setText(forageable.notes, TextView.BufferType.SPANNABLE)
            saveBtn.setOnClickListener {
                updateWeather()
            }
        }

    }

    private fun isValidEntry() = viewModel.isValidEntry(
        binding.nameInput.text.toString(),
        binding.locationAddressInput.text.toString()
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
