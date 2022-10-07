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

data class HourlyForecastItemViewData(val hour: Hours) {
    val temp = hour.temp_f.toInt().toString()
    val wind = hour.wind_mph.toString()
    val feelsliketemp = hour.feelslike_f.toString()
    val precip = hour.precip_in.toString()
    val pressure = hour.pressure_in.toString()
    var precipUnit: String = "IN"
    var pressureUnit: String = "IN"
    var windUnit: String = "MPH"

}

/**
 * Use the Celsius temp for display if the setting is checked
 */

fun HourlyForecastItemViewData.withPreferenceConversion(
    sharedPreferences: SharedPreferences,
    resources: Resources
): HourlyForecastItemViewData {
    if (!GetSettings().getTemperatureFormatFromPreferences(sharedPreferences, resources)) {
        hour.temp_f = hour.temp_c
        hour.feelslike_f = hour.feelslike_c
        hour.windchill_f = hour.windchill_c
    }

    if (!GetSettings().getWindSpeedFormatFromPreferences(sharedPreferences, resources)) {
        hour.wind_mph = hour.wind_kph
        windUnit = "KPH"
    }

    if (!GetSettings().getMeasurementFormatFromPreferences(sharedPreferences, resources)) {
        hour.precip_in = hour.precip_mm
        hour.pressure_in = hour.pressure_mb
        precipUnit = "MM"
        pressureUnit = "MB"
    }


    return HourlyForecastItemViewData(
            hour = Hours(
                time_epoch = hour.time_epoch,
                time = hour.time,
                temp_f = hour.temp_f,
                temp_c = hour.temp_c,
                is_day = hour.is_day,
                condition = hour.condition,
                wind_mph = hour.wind_mph,
                wind_kph = hour.wind_kph,
                wind_dir = hour.wind_dir,
                chance_of_rain = hour.chance_of_rain,
                chance_of_snow = hour.chance_of_snow,
                feelslike_c = hour.feelslike_c,
                feelslike_f = hour.feelslike_f,
                precip_in = hour.precip_in,
                precip_mm = hour.precip_mm,
                pressure_in = hour.pressure_in,
                pressure_mb = hour.pressure_mb,
                will_it_rain = hour.will_it_rain,
                will_it_snow = hour.will_it_snow,
                windchill_c = hour.windchill_c,
                windchill_f = hour.windchill_f

            )
        )
}
