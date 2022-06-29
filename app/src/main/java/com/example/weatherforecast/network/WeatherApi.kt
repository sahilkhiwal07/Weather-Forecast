package com.example.weatherforecast.network

import com.example.weatherforecast.model.Weather
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("weather")
    suspend fun getWeather(
        @Query("lat") latitude : String,
        @Query("lon") longitude: String,
        @Query("appid") key : String
    ): Response<Weather>

    @GET("weather")
    suspend fun getCityName(
        @Query("q") cityName: String,
        @Query("appid") key : String
    ): Response<Weather>
}