package com.example.weatherforecast.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.model.Weather
import com.example.weatherforecast.network.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()
    private val weatherLiveData : MutableLiveData<Weather> = MutableLiveData()

    private val progressBar : MutableLiveData<Boolean> = MutableLiveData()

    fun getProgressBar() : LiveData<Boolean> {
        return progressBar
    }

    fun getWeatherForecast() : LiveData<Weather> {
        return weatherLiveData
    }

    fun searchWeatherLocation(latitude: String, longitude: String) {
        progressBar.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val weather = repository.getCurrentWeather(latitude, longitude).body()
            try {
                if (weather != null) {
                    this@WeatherViewModel.weatherLiveData.postValue(weather)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.d("TAG", ex.message.toString())
            }
        }
        progressBar.value = false
    }

    fun searchCity(city: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val city: Weather? = repository.getCurrentCity(city).body()
            try {
                if (city != null) {
                    this@WeatherViewModel.weatherLiveData.postValue(city)
                }
            } catch (ex : Exception) {
                ex.printStackTrace()
                Log.d("TAG",ex.message.toString())
            }
        }
    }
}