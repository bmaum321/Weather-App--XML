package com.brian.weather.ui

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.brian.weather.data.BaseApplication
import com.example.weather.R
import com.example.weather.databinding.FragmentAddWeatherLocationBinding
import com.brian.weather.model.WeatherEntity
import com.brian.weather.ui.adapter.AutoSuggestAdapter
import com.brian.weather.ui.viewmodel.AddWeatherLocationViewModel
import com.brian.weather.ui.viewmodel.SearchViewData
import com.brian.weather.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val viewModel: AddWeatherLocationViewModel by activityViewModels {
        AddWeatherLocationViewModel.AddWeatherLocationViewModelFactory(
            (activity?.application as BaseApplication).database.weatherDao(), Application()
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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
            }
        }

        val adapter =
            context?.let { AutoSuggestAdapter(it, R.layout.support_simple_spinner_dropdown_item) }
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
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                handler?.removeMessages(Constants.TRIGGER_AUTO_COMPLETE)
                handler?.sendEmptyMessageDelayed( //TODO can use a mutable state flow here
                    Constants.TRIGGER_AUTO_COMPLETE,
                    Constants.AUTO_COMPLETE_DELAY
                )
            }

            override fun afterTextChanged(s: Editable) {}
        })

        handler = Handler { msg ->
            if (msg.what == Constants.TRIGGER_AUTO_COMPLETE) {
                if ((binding.autoCompleteTextView.text).isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.getSearchResults(binding.autoCompleteTextView.text.toString())
                            .collect { searchResults ->
                                when (searchResults) {
                                    is SearchViewData.Done -> {
                                        withContext(Dispatchers.Main) {
                                            adapter?.setData(searchResults.searchDomainObject)
                                            adapter?.notifyDataSetChanged()
                                            // Hide keyboard on item click
                                            binding.autoCompleteTextView.setOnItemClickListener { _, _, _, _ ->
                                                this@AddWeatherFragment.view
                                                ?.hideKeyboard()
                                            }
                                        }
                                    }
                                    is SearchViewData.Error -> {
                                        withContext(Dispatchers.Main) {
                                            adapter?.setData(listOf("${searchResults.message}"))
                                            adapter?.notifyDataSetChanged()
                                            binding.autoCompleteTextView.setOnItemClickListener { _, _, _, _ ->
                                                this@AddWeatherFragment.view
                                                    ?.hideKeyboard()
                                            }
                                        }

                                    }
                                    is SearchViewData.Loading -> {
                                        withContext(Dispatchers.Main) {
                                            adapter?.setData(listOf("Loading...."))
                                            adapter?.notifyDataSetChanged()
                                        }
                                    }
                                }
                            }
                    }
                }
            }
            false
        }

    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun deleteWeather(weather: WeatherEntity) {
        viewModel.deleteWeather(weather)
        // Delete the location from the shared preferences set
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val editor = sharedPref.edit()
        val location = weather.zipCode
        val setFromSharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext()).getStringSet(
                "locations",
                null
            )
        // get a copy of shared preferences
        val copyOfSet = setFromSharedPreferences?.toMutableSet()
        // remove the location from shared preferences
        copyOfSet?.remove(location)
        // commit back to preferences
        editor.putStringSet("locations", copyOfSet)
        editor.apply()
        findNavController().navigate(
            R.id.action_addWeatherFragment_to_WeatherListFragment
        )
    }

    private fun addWeather() {
        if (isValidEntry()) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                if (!viewModel.storeNetworkDataInDatabase(binding.autoCompleteTextView.text.toString())) {
                    withContext(Dispatchers.Main) { //Lets you launch a new coroutine on a different thread within an existing coroutine
                        showToast(Constants.ERRORTEXT)
                    }
                }
                withContext(Dispatchers.Main) { //Navigation must be run on main thread
                     findNavController().popBackStack()
                    /**
                     * IF we pop the back stack, the item touch helper does not get notified of the new object being
                     * added to the list
                     */
                   // findNavController().navigate(R.id.action_addWeatherFragment_to_WeatherListFragment)
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
                zipcode = binding.autoCompleteTextView.text.toString(),
                sortOrder = weatherEntity.sortOrder

            )
            findNavController().navigate(
                R.id.action_addWeatherFragment_to_WeatherListFragment
            )
        }
    }

    private fun bindWeather(weatherEntity: WeatherEntity) {
        binding.apply {
            autoCompleteTextView.setText(weatherEntity.zipCode, TextView.BufferType.SPANNABLE)
            saveBtn.setOnClickListener {
                updateWeather()
            }
        }

    }

    private fun isValidEntry() = viewModel.isValidEntry(
        binding.autoCompleteTextView.text.toString()
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




