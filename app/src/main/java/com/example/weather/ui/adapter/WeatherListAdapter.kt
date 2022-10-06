package com.example.weather.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.ListItemWeatherBinding
import com.example.weather.domain.WeatherDomainObject
import com.example.weather.model.WeatherEntity
import com.example.weather.ui.WeatherListFragment
import java.util.*

/**
 * ListAdapter for the list of [WeatherDomainObject]s retrieved from the repository
 */
class WeatherListAdapter(
    private val clickListener: (WeatherDomainObject) -> Unit
) : ListAdapter<WeatherDomainObject, WeatherListAdapter.WeatherViewHolder>(DiffCallback) {

    class WeatherViewHolder(
        private var binding: ListItemWeatherBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(weatherDomainObject: WeatherDomainObject) {
            binding.weather = weatherDomainObject
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<WeatherDomainObject>() {
        override fun areItemsTheSame(oldItem: WeatherDomainObject, newItem: WeatherDomainObject): Boolean {
            return oldItem.zipcode == newItem.zipcode
        }

        override fun areContentsTheSame(oldItem: WeatherDomainObject, newItem: WeatherDomainObject): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return WeatherViewHolder(
            ListItemWeatherBinding.inflate(layoutInflater, parent, false)
        )
    }

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(currentList.toMutableList(), fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weather = getItem(position)
        holder.itemView.setOnClickListener {
            clickListener(weather)
        }
        holder.bind(weather)
    }
}


