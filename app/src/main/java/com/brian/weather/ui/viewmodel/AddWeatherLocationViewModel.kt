package com.brian.weather.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.brian.weather.data.WeatherDao
import com.brian.weather.data.WeatherDatabase.Companion.getDatabase
import com.brian.weather.model.WeatherEntity
import com.brian.weather.network.ApiResponse
import com.brian.weather.network.asDatabaseModel
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch


/**
 * [ViewModel] to provide data to the  [AddWeatherLocationFragment] and allow for interaction the the [WeatherDao]
 */

// Pass an application as a parameter to the viewmodel constructor which is the contect passed to the singleton database object
class AddWeatherLocationViewModel(private val weatherDao: WeatherDao, application: Application) :
    AndroidViewModel(application) {

    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST)
        .apply {
            tryEmit(Unit)
        }

    // sort counter for database entries

    /**
     * If database is empty, initial sort value is 1
     * If not empty, find last entry in database, increment sort value by 1
     */
    private fun getLastEntrySortValue(): Int {
        var dbSortOrderValue = 1
        if (!weatherDao.isEmpty()) {
            dbSortOrderValue = weatherDao.selectLastEntry().sortOrder + 1
        }
        return dbSortOrderValue
    }

    //The data source this viewmodel will fetch results from
    private val weatherRepository = WeatherRepository(getDatabase(application))

    // Method that takes id: Long as a parameter and retrieve a Weather from the
    //  database by id via the DAO.
    fun getWeatherById(id: Long): LiveData<WeatherEntity> {
        return weatherDao.getWeatherById(id).asLiveData()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getSearchResults(location: String): Flow<SearchViewData> {
        val searchResults = mutableListOf<String>()
        return refreshFlow
            .flatMapLatest {
                flow {
                    when (val response = weatherRepository.getSearchResults(location)) {
                        is ApiResponse.Success -> {
                            response.data.forEach { search ->
                                searchResults.add(search.name + "," + " " + search.region) // Grab from the API and emit if success
                            }
                            emit(SearchViewData.Done(searchResults))
                        }
                        is ApiResponse.Failure -> {
                            emit(
                                SearchViewData.Error(
                                    code = response.code,
                                    message = response.message
                                )
                            )
                        }
                        is ApiResponse.Exception -> {
                            emit(
                                SearchViewData.Error(
                                    code = response.e.hashCode(),
                                    message = response.e.message
                                )
                            )
                        }
                    }
                }
            }
    }

    suspend fun storeNetworkDataInDatabase(zipcode: String): Boolean {
        /**
         * This runs on a background thread by default so any value modified within this scoupe cannot
         * be returned outside of the scope
         */

        val networkError: Boolean = when (val response = weatherRepository.getWeatherWithErrorHandling(zipcode)) {
            is ApiResponse.Success -> {
                weatherDao.insert(response.data.asDatabaseModel(zipcode, getLastEntrySortValue()))
                true
            }
            is ApiResponse.Failure -> false
            is ApiResponse.Exception -> false
        }
        return networkError

    }

    fun updateWeather(
        id: Long,
        name: String,
        zipcode: String,
        sortOrder: Int
    ) {
        val weatherEntity = WeatherEntity(
            id = id,
            cityName = name,
            zipCode = zipcode,
            sortOrder = sortOrder
        )
        viewModelScope.launch(Dispatchers.IO) {
            // call the DAO method to update a weather object to the database here
            weatherDao.insert(weatherEntity)
        }
    }

    fun deleteWeather(weatherEntity: WeatherEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // call the DAO method to delete a weather object to the database here
            weatherDao.delete(weatherEntity)
        }
    }

    fun isValidEntry(zipcode: String): Boolean {
        return zipcode.isNotBlank()
    }


// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel

    class AddWeatherLocationViewModelFactory(
        private val weatherDao: WeatherDao,
        val app: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddWeatherLocationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddWeatherLocationViewModel(weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

sealed class SearchViewData {
    class Loading : SearchViewData()
    class Error(val code: Int, val message: String?) : SearchViewData()
    class Done(val searchDomainObject: List<String>) : SearchViewData() // TODO this is a response directly from the API, need to copy into domain data class
}

