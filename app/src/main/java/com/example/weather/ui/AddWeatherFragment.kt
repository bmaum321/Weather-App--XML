package com.example.weather.ui

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
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

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
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
                                            viewModel.getSearchResults(binding.autoCompleteTextView.text.toString()).collect{
                                                    searchResults ->

                                                        when (searchResults) {
                                                            is SearchViewData.Done -> {
                                                                withContext(Dispatchers.Main) {
                                                                    adapter?.setData(searchResults.searchDomainObject)
                                                                    adapter?.notifyDataSetChanged()
                                                                    binding.autoCompleteTextView.setOnItemClickListener { _, view, _, _ ->
                                                                        this@AddWeatherFragment.view?.hideKeyboard()
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
                                }
                                false
                            }

    }



    private fun deleteWeather(weather: WeatherEntity) {
        viewModel.deleteWeather(weather)
        findNavController().navigate(
            R.id.action_addWeatherFragment_to_WeatherListFragment
        )
    }

    private fun addWeather() {
       // if (isValidEntry()) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
              // if( !viewModel.storeNetworkDataInDatabase(binding.zipcodeInput.text.toString())) {
                if( !viewModel.storeNetworkDataInDatabase(binding.autoCompleteTextView.text.toString())) {
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
      //  }
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
               // zipcode = binding.zipcodeInput.text.toString(),
                zipcode = binding.autoCompleteTextView.text.toString(),
                sortOrder = weatherEntity.sortOrder

            )
            findNavController().navigate(
                R.id.action_addWeatherFragment_to_WeatherListFragment
            )
        }
    }

    private fun bindWeather(weatherEntity: WeatherEntity) {
        binding.apply{
         //   zipcodeInput.setText(weatherEntity.zipCode, TextView.BufferType.SPANNABLE)
            autoCompleteTextView.setText(weatherEntity.zipCode, TextView.BufferType.SPANNABLE)
            saveBtn.setOnClickListener {
                updateWeather()
            }
        }

    }

    private fun isValidEntry() = viewModel.isValidEntry(
       // binding.zipcodeInput.text.toString()
    binding.autoCompleteTextView.text.toString()
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




