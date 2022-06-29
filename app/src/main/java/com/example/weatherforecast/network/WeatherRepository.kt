package com.example.weatherforecast.network

import com.example.weatherforecast.utils.Constants

class WeatherRepository {

    suspend fun getCurrentWeather(latitude: String, longitude: String) =
        ServiceBuilder.api.getWeather(latitude, longitude, Constants.KEY)


    suspend fun getCurrentCity(cityName: String) =
        ServiceBuilder.api.getCityName(cityName, Constants.KEY)
}