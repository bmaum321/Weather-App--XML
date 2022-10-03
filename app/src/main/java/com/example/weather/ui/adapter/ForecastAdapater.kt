package com.example.weather.ui.adapter

import android.content.SharedPreferences
import android.content.res.Resources
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.ResourceManagerInternal
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.databinding.DailyForecastListItemBinding
import com.example.weather.domain.ForecastDomainObject
import com.example.weather.model.Day
import com.example.weather.ui.settings.GetSettings

/**
 * ListAdapter for the list of days in the forecast, retrieved from the Repository
 */
class ForecastAdapter(
    private val clickListener: (ForecastItemViewData) -> Unit
) : ListAdapter<ForecastItemViewData, ForecastAdapter.ForecastViewHolder>(DiffCallback) {

    class ForecastViewHolder(
         private var binding: DailyForecastListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(day: ForecastItemViewData) {
            binding.forecast = day
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ForecastItemViewData>() {
        override fun areItemsTheSame(oldItem: ForecastItemViewData, newItem: ForecastItemViewData): Boolean {
            return oldItem == newItem //TODO
        }

        override fun areContentsTheSame(oldItem:ForecastItemViewData, newItem: ForecastItemViewData): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ForecastViewHolder(
            DailyForecastListItemBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
      // TODO holder.binding.forecast
        val forecast = getItem(position)
        holder.itemView.setOnClickListener {
            clickListener(forecast)
        }
        holder.bind(forecast)
    }
}

/**
 * Converting the data types before they are presented to the UI in order to convert the double from
 * the API into a string
 */

data class ForecastItemViewData(val day: Day) {
    var high: String = day.day.maxtemp_f.toInt().toString()

    var low: String = day.day.mintemp_f.toInt().toString()

}

/**
 * USe the Celsius temp for display if the setting is checked
 */

 fun ForecastItemViewData.withPreferenceConversion(sharedPreferences: SharedPreferences, resources: Resources): ForecastItemViewData {
     if(!GetSettings().getTemperatureFormatFromPreferences(sharedPreferences, resources)){
         day.day.maxtemp_f = day.day.maxtemp_c
         day.day.mintemp_f = day.day.mintemp_c
     }

    return ForecastItemViewData(
        day = Day(
            date = day.date,
            day = day.day,
            hour = day.hour
        )
    )
}