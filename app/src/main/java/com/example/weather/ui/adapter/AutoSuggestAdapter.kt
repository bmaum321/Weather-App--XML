package com.example.weather.ui.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.Nullable


class AutoSuggestAdapter(context: Context, resource: Int) :
    ArrayAdapter<String>(context, resource), Filterable {
    private val searchData: MutableList<String>

    init {
        searchData = ArrayList()
    }

    fun setData(list: List<String>?) {
        searchData.clear()
        searchData.addAll(list!!)
    }

    override fun getCount(): Int {
        return searchData.size
    }

    override fun getItem(position: Int): String {
        return searchData[position]
    }

    /**
     * Used to Return the full object directly from adapter.
     *
     * @param position
     * @return
     */
    fun getObject(position: Int): String {
        return searchData[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    filterResults.values = searchData
                    filterResults.count = searchData.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}