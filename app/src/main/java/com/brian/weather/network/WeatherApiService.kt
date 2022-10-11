package com.brian.weather.network

import com.brian.weather.model.ForecastContainer
import com.brian.weather.model.Search
import com.brian.weather.util.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * A retrofit service to fetch the weather data from the API
 */

private const val BASE_URL = "https://api.weatherapi.com/v1/"
private const val CURRENT = "current.json?key=${Constants.APIKEY}"
private const val FORECAST = "forecast.json?key=${Constants.APIKEY}"
private const val SEARCH = "search.json?key=${Constants.APIKEY}"



/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// Configure retrofit to parse JSON and use coroutines
private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

interface WeatherApiService {
    @GET(CURRENT)
    suspend fun getWeather(
        @Query("q") zipcode: String
    ): WeatherContainer

    @GET(CURRENT)
    suspend fun getWeatherWithErrorHandling(
        @Query("q") zipcode: String
    ): Response<WeatherContainer>

    @GET(SEARCH)
    suspend fun locationSearch(
        @Query("Q") location: String
    ): Response<List<Search>>

    @GET(FORECAST)
    suspend fun getForecast(
        @Query("q") zipcode: String,
        @Query("days") days: Int = 7, // Maximum forecast days for free API is 3 days, have paid plan with 7 days
        @Query("alerts") alerts: String = "yes"
    ): Response<ForecastContainer>
}


/**
 * Main entry point for network access
 */

object WeatherApi {
    val retrofitService: WeatherApiService by lazy { retrofit.create(WeatherApiService::class.java) }
}


/**
 * Sealed class to handle API responses
 */
sealed class ApiResponse<T : Any> {
    class Success<T : Any>(val data: T) : ApiResponse<T>() //TODO trying to change to var to manipulate data after retrieveing from api
    class Failure<T : Any>(val code: Int, val message: String?) : ApiResponse<T>()
    class Exception<T : Any>(val e: Throwable) : ApiResponse<T>()
}

/**
 * The handleApi function receives an executable lambda function, which returns a Retrofit response.
 * After executing the lambda function, the handleApi function returns ApiResponse.Success if the
 * response is successful and the body data is a non-null value.
 */

suspend fun <T : Any> handleApi(
    execute: suspend () -> Response<T>
): ApiResponse<T> {
    return try {
        val response = execute()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            ApiResponse.Success(body)
        } else {
            ApiResponse.Failure(code = response.code(), message = response.message())
        }
    } catch (e: HttpException) {
        ApiResponse.Failure(code = e.code(), message = e.message())
    } catch (e: Throwable) {
        ApiResponse.Exception(e)
    }
}
