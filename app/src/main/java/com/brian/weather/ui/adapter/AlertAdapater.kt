package com.brian.weather.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.AlertListItemBinding
import com.brian.weather.model.Alert

/**
 * ListAdapter for the list of days in the forecast, retrieved from the Repository
 */
class AlertAdapter(
) : ListAdapter<AlertItemViewData, AlertAdapter.AlertViewHolder>(DiffCallback) {

    class AlertViewHolder(
         private var binding: AlertListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alert: AlertItemViewData) {
            binding.alert = alert
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AlertItemViewData>() {
        override fun areItemsTheSame(oldItem: AlertItemViewData, newItem: AlertItemViewData): Boolean {
            return oldItem == newItem //TODO
        }

        override fun areContentsTheSame(oldItem: AlertItemViewData, newItem: AlertItemViewData): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return AlertViewHolder(
            AlertListItemBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
      // TODO holder.binding.forecast
        val forecast = getItem(position)
        holder.bind(forecast)
    }
}

/**
 * Converting the data types before they are presented to the UI in order to convert the double from
 * the API into a string
 */

data class AlertItemViewData(val alert: Alert)




