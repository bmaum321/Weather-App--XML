package com.example.weather.ui.adapter

import android.content.SharedPreferences
import android.content.res.Resources
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.databinding.HourlyForecastListItemBinding
import com.example.weather.databinding.HourlyForecastListItemNewBinding
import com.example.weather.model.Condition
import com.example.weather.model.Day
import com.example.weather.model.Hours
import com.example.weather.ui.settings.GetSettings

/**
 * ListAdapter for the list of days in the forecast, retrieved from the Repository
 */
class HourlyForecastAdapter(
    private val clickListener: (HourlyForecastItemViewData) -> Unit
) : ListAdapter<HourlyForecastItemViewData, HourlyForecastAdapter.HourlyForecastViewHolder>(
    DiffCallback
) {

    class HourlyForecastViewHolder(
        private var binding: HourlyForecastListItemNewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(hour: HourlyForecastItemViewData) {
            /**
             * If no chance of rain, hide the view
             */
            if (hour.hour.chance_of_rain == 0) {
                binding.rainChance.visibility = View.GONE
            } else binding.rainChance.visibility =
                View.VISIBLE //this seems to be needed for some reason
            binding.arrowButton.setOnClickListener {
                if(binding.hiddenView.visibility == View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(binding.baseCardview, AutoTransition())
                    binding.hiddenView.visibility = View.GONE
                    binding.arrowButton.setImageResource(R.drawable.ic_baseline_expand_more_24)
                } else {
                    TransitionManager.beginDelayedTransition(binding.baseCardview, AutoTransition())
                    binding.hiddenView.visibility = View.VISIBLE
                    binding.arrowButton.setImageResource(R.drawable.ic_baseline_expand_less_24)
                }
            }
            binding.forecast = hour
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<HourlyForecastItemViewData>() {
        override fun areItemsTheSame(
            oldItem: HourlyForecastItemViewData,
            newItem: HourlyForecastItemViewData
        ): Boolean {
            return oldItem == newItem //TODO
        }

        override fun areContentsTheSame(
            oldItem: HourlyForecastItemViewData,
            newItem: HourlyForecastItemViewData
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HourlyForecastAdapter.HourlyForecastViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HourlyForecastAdapter.HourlyForecastViewHolder(
            HourlyForecastListItemNewBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: HourlyForecastAdapter.HourlyForecastViewHolder, position: Int) {
        val forecast = getItem(position)
        // holder.itemView.setOnClickListener { // Disabling click listener on hour list
        //     clickListener(forecast)
        //  }
        holder.bind(forecast)
    }
}

/**
 * TODO pass in units to primary constructor to modify the unit values
 */

data class HourlyForecastItemViewData(
    val hour: Hours,
    var precipUnit: String = "IN"
) {
    val temp = hour.temp_f.toInt().toString()
    val wind = hour.wind_mph.toString()
    val feelsliketemp = hour.feelslike_f.toString()
    val precip = hour.precip_in.toString()
    val pressure = hour.pressure_in.toString()
   // var precipUnit: String = "IN"
    var pressureUnit: String = "IN"
    var windUnit: String = "MPH"

}

