package com.example.weather.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.ForecastListItemBinding
import com.example.weather.model.Day

/**
 * ListAdapter for the list of days in the forecast, retrieved from the Repository
 */
class ForecastAdapter(
    private val clickListener: (Day) -> Unit
) : ListAdapter<Day, ForecastAdapter.ForecastViewHolder>(DiffCallback) {

    class ForecastViewHolder(
         private var binding: ForecastListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(day: Day) {
            binding.forecast = day
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Day>() {
        override fun areItemsTheSame(oldItem: Day, newItem: Day): Boolean {
            return oldItem == newItem //TODO
        }

        override fun areContentsTheSame(oldItem:Day, newItem: Day): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ForecastViewHolder(
            ForecastListItemBinding.inflate(layoutInflater, parent, false)
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
