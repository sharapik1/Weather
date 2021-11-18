package com.example.weather_sharapova

data class Weather (
        val dt: Int,
        val mainTemp: Double,
        val mainHumidity: Int,
        val weatherIcon: String,
        val weatherDescription: String,
        val windSpeed: Double,
        val windDeg: Int,
        val dtTxt: String
)