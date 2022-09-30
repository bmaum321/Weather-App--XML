/*
package com.example.weather.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.databinding.DailyForecastListItemBinding
import com.example.weather.domain.ForecastDomainObject
import com.example.weather.model.Day
import com.example.weather.ui.viewmodel.SearchViewData

/**
 * ListAdapter for the list of days in the forecast, retrieved from the Repository
 */
class AutoSuggestAdapter(
    private val clickListener: (SearchViewData) -> Unit
) : ListAdapter<SearchViewData, AutoSuggestAdapter.AutoSuggestViewHolder>(DiffCallback) {

    class AutoSuggestViewHolder(
        private var binding: DailyForecastListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(search: SearchViewData) {
            binding.forecast = search
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SearchViewData>() {
        override fun areItemsTheSame(oldItem: SearchViewData, newItem: SearchViewData): Boolean {
            return oldItem == newItem //TODO
        }

        override fun areContentsTheSame(oldItem:SearchViewData, newItem: SearchViewData): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoSuggestViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return AutoSuggestViewHolder(
            DailyForecastListItemBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AutoSuggestViewHolder, position: Int) {
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



 */