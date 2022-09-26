package com.example.weather.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.ForecastListItemBinding
import com.example.weather.domain.ForecastDomainObject

/**
 * ListAdapter for the list of days in the forecast, retrieved from the Repository
 */
class ForecastAdapter(
    private val clickListener: (ForecastDomainObject) -> Unit
) : ListAdapter<ForecastDomainObject, ForecastAdapter.ForecastViewHolder>(DiffCallback) {

    class ForecastViewHolder(
        private var binding: ForecastListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(forecastDomainObject: ForecastDomainObject) {
            binding.forecast = forecastDomainObject
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ForecastDomainObject>() {
        override fun areItemsTheSame(oldItem: ForecastDomainObject, newItem: ForecastDomainObject): Boolean {
            return oldItem == newItem //TODO
        }

        override fun areContentsTheSame(oldItem: ForecastDomainObject, newItem: ForecastDomainObject): Boolean {
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
        val forecast = getItem(position)
        holder.itemView.setOnClickListener {
            clickListener(forecast)
        }
        holder.bind(forecast)
    }
}
