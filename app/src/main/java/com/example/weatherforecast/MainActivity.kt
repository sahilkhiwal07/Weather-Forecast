package com.example.weatherforecast

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.ui.WeatherViewModel
import com.example.weatherforecast.utils.Constants
import com.example.weatherforecast.utils.showLongToast
import com.example.weatherforecast.utils.showShortToast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var viewModel: WeatherViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        getCurrentLocations()
        initViewModel()
        searchCity()
    }

    private fun searchCity() {
        iv_search.setOnClickListener {
            if (!et_city.text.isNullOrEmpty()) {
                viewModel.searchCity(et_city.text.toString().trim())
            } else {
                showShortToast(getString(R.string.enter_city_name))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun initViewModel() {
        viewModel.getProgressBar().observe(this) {
            if (it) showProgressBar()
            else hideProgressBar()
        }

        viewModel.getWeatherForecast().observe(this) {
            // top card
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            tv_date.text = date

            val temp = it.main.temp.toString()
            val newTEmp = kelvinToCelsius(temp.toDouble())
            tv_temperature.text = "$newTEmp°C"

            tv_weatherType.text = it.weather[0].main
            tv_city.text = it.name

            updateWeatherType(it.weather[0].id)

            // mid temp and wind
            val tempMin = it.main.temp_min.toString()
            val newTempMin = kelvinToCelsius(tempMin.toDouble())
            tv_tempMin.text = "$newTempMin°C"

            val tempMax = it.main.temp_max.toString()
            val newTempMax = kelvinToCelsius(tempMax.toDouble())
            tv_tempMax.text = "$newTempMax°C"

            tv_wind.text = it.wind.speed.toString() + " m/s"

            // time stamp
            val sunRise = it.sys.sunrise.toString()
            tv_sunrise.text = timeStampToLocalDate(sunRise.toLong())

            val sunSet = it.sys.sunset.toString()
            tv_sunset.text = timeStampToLocalDate(sunSet.toLong())

            // bottom card
            tv_pressure.text = it.main.pressure.toString()
            tv_humidity.text = it.main.humidity.toString() + " %"
            tv_visibility.text = it.visibility.toString()

            val feelsLike = it.main.feels_like.toString()
            val newFeelsLike = kelvinToCelsius(feelsLike.toDouble())
            tv_feelsLike.text = "$newFeelsLike°C"
        }
    }

    private fun updateWeatherType(id: Int) {
        when (id) {
            in 200..232 -> {
                iv_weatherImage.setImageResource(R.drawable.thunderstorm)
                bg_wallpaper.setBackgroundResource(R.drawable.bg_thunderstrom)
            }
            in 300..321 -> {
                iv_weatherImage.setImageResource(R.drawable.drizzle)
                bg_wallpaper.setBackgroundResource(R.drawable.bg_drizzle)
            }
            in 500..531 -> {
                iv_weatherImage.setImageResource(R.drawable.raining)
                bg_wallpaper.setBackgroundResource(R.drawable.bg_rain)
            }
            in 600..622 -> {
                iv_weatherImage.setImageResource(R.drawable.snow)
                bg_wallpaper.setBackgroundResource(R.drawable.bg_snow)
            }
            in 700..781 -> {
                iv_weatherImage.setImageResource(R.drawable.mist)
                bg_wallpaper.setBackgroundResource(R.drawable.bg_mist)
            }
            800 -> {
                iv_weatherImage.setImageResource(R.drawable.clear_sky)
                bg_wallpaper.setBackgroundResource(R.drawable.bg_sky)
            }
            in 800..804 -> {
                iv_weatherImage.setImageResource(R.drawable.cloud)
                bg_wallpaper.setBackgroundResource(R.drawable.bg_clouds)
            }
        }
    }

    private fun hideProgressBar() {
        progressBar?.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    private fun kelvinToCelsius(temp: Double): Double {
        var intTemp = temp
        intTemp = intTemp.minus(273)
        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeStampToLocalDate(timeStamp: Long): String {
        val localTime = timeStamp.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }
        return localTime.toString()
    }

    @SuppressLint("SetTextI18n")
    private fun getCurrentLocations() {
        if (checkPermissions()) {
            if (isLocationsEnabled()) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermission()
                    return
                }

                fusedLocationProvider.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        showLongToast(getString(R.string.not_find_any_location))
                    } else {
                        showLongToast(getString(R.string.success))
                        fetchCurrentLocation(location.latitude.toString(),
                            location.longitude.toString())
                    }
                }

            } else {
                showLongToast(getString(R.string.Turn_on_Location))
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    private fun fetchCurrentLocation(latitude: String, longitude: String) {
        viewModel.searchWeatherLocation(latitude, longitude)
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), Constants.REQUEST_LOCATION)
    }

    private fun isLocationsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                showLongToast(getString(R.string.Granted))
                getCurrentLocations()
            } else {
                showLongToast(getString(R.string.Permission_not_Granted))
            }
        }
    }
}