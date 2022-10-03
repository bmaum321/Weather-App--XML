package com.example.weather.ui.adapter

import android.content.SharedPreferences
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.HourlyForecastListItemBinding
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
        private var binding: HourlyForecastListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(hour: HourlyForecastItemViewData) {
            /**
             * If no chance of rain, hide the view
             */
            if (hour.hour.chance_of_rain == 0) {
                binding.rainChance.visibility = View.GONE
            } else binding.rainChance.visibility =
                View.VISIBLE //this seems to be needed for some reason
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
            HourlyForecastListItemBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: HourlyForecastViewHolder, position: Int) {
        val forecast = getItem(position)
        // holder.itemView.setOnClickListener { // Disabling click listener on hour list
        //     clickListener(forecast)
        //  }
        holder.bind(forecast)
    }
}

data class HourlyForecastItemViewData(val hour: Hours) {
    val temp = hour.temp_f.toInt().toString()

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
    }

    if (!GetSettings().getWindSpeedFormatFromPreferences(sharedPreferences, resources)) {
        hour.wind_mph = hour.wind_kph
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
                chance_of_rain = hour.chance_of_rain
            )
        )
}
