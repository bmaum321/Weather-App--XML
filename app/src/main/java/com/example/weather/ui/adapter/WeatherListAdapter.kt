package com.example.weather.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.ListItemWeatherBinding
import com.example.weather.model.WeatherEntity

/**
 * ListAdapter for the list of [WeatherEntity]s retrieved from the database
 */
class WeatherListAdapter(
    private val clickListener: (WeatherEntity) -> Unit
) : ListAdapter<WeatherEntity, WeatherListAdapter.WeatherViewHolder>(DiffCallback) {

    class WeatherViewHolder(
        private var binding: ListItemWeatherBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(weatherEntity: WeatherEntity) {
            binding.weather = weatherEntity
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<WeatherEntity>() {
        override fun areItemsTheSame(oldItem: WeatherEntity, newItem: WeatherEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WeatherEntity, newItem: WeatherEntity): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return WeatherViewHolder(
            ListItemWeatherBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weather = getItem(position)
        holder.itemView.setOnClickListener {
            clickListener(weather)
        }
        holder.bind(weather)
    }
}
