package com.example.weather.ui

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.weather.BaseApplication
import com.example.weather.R
import com.example.weather.databinding.FragmentWeatherListBinding
import com.example.weather.model.WeatherEntity
import com.example.weather.ui.adapter.WeatherListAdapter
import com.example.weather.ui.viewmodel.WeatherListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * A fragment to view the list of [Weathers]s stored in the database.
 * Clicking on a [Weather] list item launches the [WeatherDetailFragment].
 * Clicking the [FloatingActionButton] launched the [AddWeatherFragment]
 */
class WeatherListFragment : Fragment() {

    // TODO: Refactor the creation of the view model to take an instance of
    //  WeatherViewModelFactory. The factory should take an instance of the Database retrieved
    //  from BaseApplication
    private val viewModel: WeatherListViewModel by activityViewModels {
        WeatherListViewModel.WeatherViewModelFactory(
            (activity?.application as BaseApplication).database.weatherDao(),
            Application() //TODO change here passing application as paramater now
        )
    }


    private var _binding: FragmentWeatherListBinding? = null
    private lateinit var weatherEntity: WeatherEntity // NEW

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

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = WeatherListAdapter { weather ->
            val action = WeatherListFragmentDirections
                .actionWeatherListFragmentToWeatherDetailFragment(weather.zipcode)
            findNavController().navigate(action)
        }


        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAllWeather().collect {
                withContext(Dispatchers.Main) { //Data binding always done on main thread
                    adapter.submitList(it)
                }
            }
        }

/*
        // TODO: observe the list of weather objects from the view model and submit it the adapter
        //TODO: This should instead observe the repository
        viewModel.allWeatherEntity.observe(this.viewLifecycleOwner) { weathers ->
            weathers.let {
                adapter.submitList(it)
            }
        }
 */

        binding.apply {
            recyclerView.adapter = adapter
            addWeatherFab.setOnClickListener {
                findNavController().navigate(
                    R.id.action_weatherLocationListFragment_to_addWeatherLocationFragment
                )
            }
            swipeRefresh.setOnRefreshListener {
                refreshScreen()
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun refreshScreen() {
        viewModel.refresh()
    }

    /*
 * Listen for option item selections so that we receive a notification
 * when the user requests a refresh by selecting the refresh action bar item.
 *
 */
    /*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val adapter = WeatherListAdapter { weather ->
            val action = WeatherListFragmentDirections
                .actionWeatherListFragmentToWeatherDetailFragment(weather.zipcode)
            findNavController().navigate(action)
        }
        when (item.itemId) {


            // Check if user triggered a refresh:
            R.id.menu_refresh -> {

                // Signal SwipeRefreshLayout to start the progress indicator
                binding.swiperefresh.isRefreshing = true

                // Start the refresh background task.
                // This method calls setRefreshing(false) when it's finished.
                refreshScreen(viewModel, adapter = adapter)

                return true
            }
        }

        // User didn't trigger a refresh, let the superclass handle this action
        return super.onOptionsItemSelected(item)
    }

     */


}
