package com.example.weather.ui

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.weather.BaseApplication
import com.example.weather.R
import com.example.weather.databinding.FragmentWeatherListBinding
import com.example.weather.ui.adapter.WeatherListAdapter
import com.example.weather.ui.viewmodel.WeatherViewModel


/**
 * A fragment to view the list of [Forageable]s stored in the database.
 * Clicking on a [Forageable] list item launches the [WeatherLocationDetailFragment].
 * Clicking the [FloatingActionButton] launched the [AddWeatherLocationFragment]
 */
class WeatherListFragment : Fragment() {

    // TODO: Refactor the creation of the view model to take an instance of
    //  WeatherViewModelFactory. The factory should take an instance of the Database retrieved
    //  from BaseApplication
    private val viewModel: WeatherViewModel by activityViewModels {
        WeatherViewModel.WeatherViewModelFactory(
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
                .actionForageableListFragmentToForageableDetailFragment(weather.zipCode, weather.id)
            findNavController().navigate(action)
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
