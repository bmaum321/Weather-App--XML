package com.example.weather.ui

import android.app.Application
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.Log.DEBUG
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.BaseApplication
import com.example.weather.R
import com.example.weather.databinding.FragmentWeatherListBinding
import com.example.weather.model.WeatherEntity
import com.example.weather.ui.adapter.WeatherListAdapter
import com.example.weather.ui.viewmodel.WeatherListViewModel
import com.example.weather.ui.viewmodel.WeatherViewDataList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * A fragment to view the list of [Weathers]s stored in the database.
 * Clicking on a [Weather] list item launches the [DailyForecastFragment].
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
        binding.lifecycleOwner = viewLifecycleOwner //TODO need layout tags in xml for this to work?
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(500)
            viewModel.refresh()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val adapter = WeatherListAdapter { weather ->
            val action = WeatherListFragmentDirections
                .actionWeatherListFragmentToWeatherDetailFragment(weather.zipcode)
            findNavController().navigate(action)
        }

        /**
         * Testing a swipe to delete from main page. Need to somehow pull an id that
         * correlates to the entity from the position
         */
        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                Toast.makeText(context, "on Move", Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {

                //Remove swiped item from list and notify the RecyclerView
                val position = viewHolder.adapterPosition
                val itemLocation = adapter.currentList[position].zipcode
                lifecycleScope.launch(Dispatchers.IO) {
                    weatherEntity = viewModel.getWeatherByZipcode(itemLocation)
                    deleteWeather(weatherEntity)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "${weatherEntity.cityName} deleted", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }

        // Attach the touch helper to the recycler view
        ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(binding.recyclerView)

        /**
         * Need to have an initial listener set here if the database is empty because none of
         * the code below is reached
         */
        binding.addWeatherFab.setOnClickListener {
            findNavController().navigate(
                R.id.action_weatherLocationListFragment_to_addWeatherLocationFragment
            )
        }


        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getAllWeatherWithErrorHandling(resources).collect {
                withContext(Dispatchers.Main) { //Data binding always done on main thread
                    when (it) {
                        is WeatherViewDataList.Done -> {
                            adapter.submitList(it.weatherDomainObjects)

                            binding.apply {
                                recyclerView.adapter = adapter
                                addWeatherFab.show()
                                addWeatherFab.setOnClickListener {
                                    findNavController().navigate(
                                        R.id.action_weatherLocationListFragment_to_addWeatherLocationFragment
                                    )
                                }
                                swipeRefresh.setOnRefreshListener {
                                    refreshScreen()
                                    binding.swipeRefresh.isRefreshing = false
                                }
                                statusImage.visibility = View.GONE
                            }
                        }
                        is WeatherViewDataList.Error -> {
                            binding.apply {
                                statusImage.setImageResource(R.drawable.ic_connection_error)
                                adapter.submitList(emptyList())
                                recyclerView.adapter = adapter
                                addWeatherFab.hide()
                                swipeRefresh.setOnRefreshListener {
                                    refreshScreen()
                                    binding.swipeRefresh.isRefreshing = false
                                }
                            }
                        }

                        is WeatherViewDataList.Loading -> {
                            binding.apply {
                                statusImage.setImageResource(R.drawable.loading_animation)
                            }
                        }
                    }
                }
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


    private fun refreshScreen() {
        viewModel.refresh()
    }

    private fun deleteWeather(weather: WeatherEntity) {
        viewModel.deleteWeather(weather)
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
