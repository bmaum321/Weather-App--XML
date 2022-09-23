package com.example.weather.ui

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.weather.BaseApplication
import com.example.weather.R
import com.example.weather.databinding.FragmentWeatherListBinding
import com.example.weather.domain.WeatherDomainObject
import com.example.weather.ui.adapter.WeatherListAdapter
import com.example.weather.ui.viewmodel.WeatherListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * A fragment to view the list of [Weathers]s stored in the database.
 * Clicking on a [Weather] list item launches the [WeatherLocationDetailFragment].
 * Clicking the [FloatingActionButton] launched the [AddWeatherLocationFragment]
 */
class WeatherListFragment : Fragment() {

    // TODO: Refactor the creation of the view model to take an instance of
    //  WeatherViewModelFactory. The factory should take an instance of the Database retrieved
    //  from BaseApplication
    private val viewModel: WeatherListViewModel by activityViewModels {
        WeatherListViewModel.WeatherViewModelFactory(
            (activity?.application as BaseApplication).database.weatherDao(), Application() //TODO change here passing application as paramater now
        )
    }

    private var _binding: FragmentWeatherListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWeatherListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = WeatherListAdapter { weather ->
            val action = WeatherListFragmentDirections
                .actionWeatherListFragmentToWeatherDetailFragment(weather.zipCode, weather.id)
            findNavController().navigate(action)
        }

        val zipcodes = viewModel.getZipCodesFromDatabase() //TODO how do we pass the list of zip codes to this function
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAllWeather(zipcodes).collect {
                withContext(Dispatchers.Main) { //Data binding always done on main thread

                }

            }

        }



        // TODO: observe the list of weather objects from the view model and submit it the adapter
        //TODO: This should instead observe the repository
        viewModel.allWeatherEntity.observe(this.viewLifecycleOwner) { weathers ->
            weathers.let {
                adapter.submitList(it)
            }
        }

        binding.apply {
            recyclerView.adapter = adapter
            addWeatherFab.setOnClickListener {
                findNavController().navigate(
                    R.id.action_weatherLocationListFragment_to_addWeatherLocationFragment
                )
            }
        }
    }

}
