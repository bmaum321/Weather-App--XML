package com.example.weather.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.ListItemForageableBinding
import com.example.weather.model.Weather

/**
 * ListAdapter for the list of [Weather]s retrieved from the database
 */
class WeatherListAdapter(
    private val clickListener: (Weather) -> Unit
) : ListAdapter<Weather, WeatherListAdapter.WeatherViewHolder>(DiffCallback) {

    class WeatherViewHolder(
        private var binding: ListItemForageableBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(weather: Weather) {
            binding.weather = weather
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<Weather>() {
        override fun areItemsTheSame(oldItem: Weather, newItem: Weather): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Weather, newItem: Weather): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return WeatherViewHolder(
            ListItemForageableBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val forageable = getItem(position)
        holder.itemView.setOnClickListener{
            clickListener(forageable)
        }
        holder.bind(forageable)
    }
}
