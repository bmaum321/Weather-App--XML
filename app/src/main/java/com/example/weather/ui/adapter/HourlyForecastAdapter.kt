package com.example.weather.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.HourlyForecastListItemBinding
import com.example.weather.model.Day
import com.example.weather.model.Hours

/**
 * ListAdapter for the list of days in the forecast, retrieved from the Repository
 */
class HourlyForecastAdapter(
    private val clickListener: (HourlyForecastItemViewData) -> Unit
) : ListAdapter<HourlyForecastItemViewData, HourlyForecastAdapter.HourlyForecastViewHolder>(DiffCallback) {

    class HourlyForecastViewHolder(
        private var binding: HourlyForecastListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(hour: HourlyForecastItemViewData) {
            /**
             * If no chance of rain, hide the view
             */
            if (hour.hour.chance_of_rain == 0) {
                binding.rainChance.visibility = View.GONE
            } else binding.rainChance.visibility = View.VISIBLE //this seems to be needed for some reason
            binding.forecast = hour
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<HourlyForecastItemViewData>() {
        override fun areItemsTheSame(oldItem: HourlyForecastItemViewData, newItem: HourlyForecastItemViewData): Boolean {
            return oldItem == newItem //TODO
        }

        override fun areContentsTheSame(oldItem: HourlyForecastItemViewData, newItem: HourlyForecastItemViewData): Boolean {
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
