package com.example.weather_sharapova

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView

class CityListActivity : AppCompatActivity() {
    lateinit var list:ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_list)
         var cityNames = arrayOf(
            "Moscow",
            "Yoshkar-Ola",
            "Kazan"
        )
        list = findViewById<ListView>(R.id.citis)
        list.adapter = ArrayAdapter(
            this,
            R.layout.city_list_item, cityNames
        )

        list.setOnItemClickListener { parent, view, position, id ->
            val cityName = cityNames[position]

// запоминаем выбранное название города в параметрах
            val newIntent = Intent()
            newIntent.putExtra("cityName", cityName)
            setResult(RESULT_OK, newIntent)

// заверщаем текущий activity
            finish();
        }
    }
}