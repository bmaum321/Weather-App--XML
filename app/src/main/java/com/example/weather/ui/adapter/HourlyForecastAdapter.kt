package com.example.weather.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.HourlyForecastListItemBinding
import com.example.weather.model.Hours

/**
 * ListAdapter for the list of days in the forecast, retrieved from the Repository
 */
class HourlyForecastAdapter(
    private val clickListener: (Hours) -> Unit
) : ListAdapter<Hours, HourlyForecastAdapter.HourlyForecastViewHolder>(DiffCallback) {

    class HourlyForecastViewHolder(
        private var binding: HourlyForecastListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(hour: Hours) {
            binding.forecast = hour
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Hours>() {
        override fun areItemsTheSame(oldItem: Hours, newItem: Hours): Boolean {
            return oldItem == newItem //TODO
        }

        override fun areContentsTheSame(oldItem:Hours, newItem: Hours): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyForecastViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HourlyForecastViewHolder(
            HourlyForecastListItemBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: HourlyForecastViewHolder, position: Int) {
        val forecast = getItem(position)
       // holder.itemView.setOnClickListener {
       //     clickListener(forecast)
      //  }
        holder.bind(forecast)
    }
}
