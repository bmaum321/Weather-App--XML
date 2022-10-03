package com.example.weather.ui.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.weather.data.WeatherDao
import com.example.weather.data.WeatherDatabase.Companion.getDatabase
import com.example.weather.domain.SearchDomainObject
import com.example.weather.domain.WeatherDomainObject
import com.example.weather.domain.asDomainModel
import com.example.weather.model.Search
import com.example.weather.model.WeatherEntity
import com.example.weather.network.ApiResponse
import com.example.weather.network.WeatherContainer
import com.example.weather.network.asDatabaseModel
import com.example.weather.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
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

    //The data source this viewmodel will fetch results from
    private val weatherRepository = WeatherRepository(getDatabase(application))

    // Method that takes id: Long as a parameter and retrieve a Weather from the
    //  database by id via the DAO.
    fun getWeatherById(id: Long): LiveData<WeatherEntity> {
        return weatherDao.getWeatherById(id).asLiveData()
    }


    suspend fun getSearchResults(location: String): Flow<SearchViewData> {
        val searchUrlList = mutableListOf<String>()
        return refreshFlow
            .flatMapLatest {
                flow {
                    when (val response = weatherRepository.getSearchResults(location)) {
                        is ApiResponse.Success -> {
                            response.data.forEach { search ->
                                searchUrlList.add(search.url) // Grab only the urls from the API and emit if success
                            }
                            emit(SearchViewData.Done(searchUrlList))
                        }
                        is ApiResponse.Failure -> {
                            emit(SearchViewData.Error(code = response.code, message = response.message))
                        }
                        is ApiResponse.Exception -> {
                            emit(SearchViewData.Error(code = response.e.hashCode(), message = response.e.message))
                        }
                    }
                }
            }
    }

    /*
    suspend fun getAutoCompleteResults(): Flow<List<String>> {

    }

     */

    suspend fun storeNetworkDataInDatabase(zipcode: String): Boolean {
        var networkError = false

        /**
         * This runs on a background thread by default so any value modified within this scoupe cannot
         * be returned outside of the scope
         */

        networkError = when (val response = weatherRepository.getWeatherWithErrorHandling(zipcode)) {
            is ApiResponse.Success -> {
                weatherDao.insert(response.data.asDatabaseModel(zipcode))
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
    ) {
        val weatherEntity = WeatherEntity(
            id = id,
            cityName = name,
            zipCode = zipcode,
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
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddWeatherLocationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddWeatherLocationViewModel(weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

sealed class SearchViewData() {
    class Loading() : SearchViewData()
    class Error(val code: Int, val message: String?) : SearchViewData()
    class Done(val searchDomainObject: List<String>) : SearchViewData() // TODO this is a response directly from the API, need to copy into domain data class
}

