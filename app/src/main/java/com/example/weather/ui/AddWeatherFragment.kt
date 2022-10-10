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
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.weather.BaseApplication
import com.example.weather.R
import com.example.weather.databinding.FragmentAddWeatherLocationBinding
import com.example.weather.model.WeatherEntity
import com.example.weather.ui.adapter.AutoSuggestAdapter
import com.example.weather.ui.viewmodel.AddWeatherLocationViewModel
import com.example.weather.ui.viewmodel.SearchViewData
import com.example.weather.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ERRORTEXT = "Cannot add location, check network connection or zipcode"
/**
 * A fragment to enter data for a new [WeatherEntity] or edit data for an existing [WeatherEntity].
 * [WeatherEntity]s can be saved or deleted from this fragment.
 */
class AddWeatherFragment : Fragment() {

    private val navigationArgs: AddWeatherFragmentArgs by navArgs()

    private var _binding: FragmentAddWeatherLocationBinding? = null

    private lateinit var weatherEntity: WeatherEntity

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //  view model to take an instance of
    //  WeatherViewModelFactory. The factory should take an instance of the Database retrieved
    //  from BaseApplication
    private val viewModel: AddWeatherLocationViewModel by activityViewModels{
        AddWeatherLocationViewModel.AddWeatherLocationViewModelFactory(
            (activity?.application as BaseApplication).database.weatherDao(), Application()
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
        var handler: Handler? = null
        val id = navigationArgs.id
        if (id > 0) {

            // Observe a Weather that is retrieved by id, set the weather variable,
            //  and call the bindWeather method
            viewModel.getWeatherById(id).observe(this.viewLifecycleOwner) { selectedWeather ->
                if (selectedWeather != null) {
                    weatherEntity = selectedWeather
                    bindWeather(weatherEntity)
                }
            }

            binding.deleteBtn.visibility = View.VISIBLE
            binding.deleteBtn.setOnClickListener {
                deleteWeather(weatherEntity)
            }
        } else {
            binding.saveBtn.setOnClickListener {
                addWeather()
                //addWeatherFromSearch()
            }
        }

        /**
         *
         */

        val adapter = context?.let { AutoSuggestAdapter(it, R.layout.support_simple_spinner_dropdown_item) }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getSearchResults(binding.autoCompleteTextView.text.toString()).collect { searchResults ->

                when (searchResults) {
                    is SearchViewData.Done -> {
                        withContext(Dispatchers.Main) {
                            binding.autoCompleteTextView.setAdapter(adapter)
                            binding.autoCompleteTextView.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(
                                    s: CharSequence,
                                    start: Int,
                                    count: Int,
                                    after: Int
                                ) {
                                }

                                override fun onTextChanged(
                                    s: CharSequence, start: Int, before: Int,
                                    count: Int
                                ) {
                                    handler?.removeMessages(Constants.TRIGGER_AUTO_COMPLETE)
                                    handler?.sendEmptyMessageDelayed(
                                        Constants.TRIGGER_AUTO_COMPLETE,
                                        Constants.AUTO_COMPLETE_DELAY
                                    )
                                }

                                override fun afterTextChanged(s: Editable) {}
                            })

                            handler = Handler { msg ->
                                if (msg.what == Constants.TRIGGER_AUTO_COMPLETE) {
                                    if (!TextUtils.isEmpty(binding.autoCompleteTextView.text)) {
                                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                            viewModel.getSearchResults(binding.autoCompleteTextView.text.toString()).collect()
                                            val x = listOf<String>("This, That, Time, Table, Apple, timber, timblwe, timple")
                                            adapter?.setData(x)
                                            adapter?.notifyDataSetChanged()
                                        }
                                    }
                                }
                                false
                            }
                        }
                    }

                    is SearchViewData.Error -> {
                        Log.d("API", "${searchResults.message}")
                    }
                    is SearchViewData.Loading -> {
                        Log.d("API", "$searchResults")
                    }
                }
            }
        }
    }


    private fun deleteWeather(weather: WeatherEntity) {
        viewModel.deleteWeather(weather)
        findNavController().navigate(
            R.id.action_addWeatherFragment_to_WeatherListFragment
        )
    }

    private fun addWeather() {
        if (isValidEntry()) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
               if( !viewModel.storeNetworkDataInDatabase(binding.zipcodeInput.text.toString())) {
                   withContext(Dispatchers.Main) { //Lets you launch a new coroutine on a different thread within an existing coroutine
                       showToast(ERRORTEXT)
                   }
               }
                withContext(Dispatchers.Main) { //Navigation must be run on main thread
                   // findNavController().popBackStack()
                    /**
                     * IF we pop the back stack, the item touch helper does not get notified of the new object being
                     * added to the list
                     */
                    findNavController().navigate(R.id.action_addWeatherFragment_to_WeatherListFragment)
                }
            }
        }
    }

    private fun showToast(text: String?) {
        val duration = Toast.LENGTH_LONG
        val toast = Toast.makeText(context, text, duration)
        toast.show()
    }

    private fun updateWeather() {
        if (isValidEntry()) {
            viewModel.updateWeather(
                id = navigationArgs.id,
                name = weatherEntity.cityName,
                zipcode = binding.zipcodeInput.text.toString(),
                sortOrder = weatherEntity.sortOrder

            )
            findNavController().navigate(
                R.id.action_addWeatherFragment_to_WeatherListFragment
            )
        }
    }

    private fun bindWeather(weatherEntity: WeatherEntity) {
        binding.apply{
            zipcodeInput.setText(weatherEntity.zipCode, TextView.BufferType.SPANNABLE)
            saveBtn.setOnClickListener {
                updateWeather()
            }
        }

    }

    private fun isValidEntry() = viewModel.isValidEntry(
        binding.zipcodeInput.text.toString()
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




