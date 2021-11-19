    package com.example.weather_sharapova

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.transition.Visibility
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.lang.Exception

    class MainActivity : AppCompatActivity()
{
    var counter = 0
    private var ready=false
    var cityName = ""
    var topTag = ""
    var subTag = ""
    var dt_txt = ""
    var description: String = ""
    var icon: String = ""
    var temp:Double=0.0
    var hum: Int = 0
    var sp: Double=0.0
    var deg: Int=0
    lateinit var textView: TextView
    lateinit var tempView: TextView
    lateinit var humView: TextView
    lateinit var spView: TextView
    private lateinit var dailyInfoRecyclerView: RecyclerView
    private lateinit var callback: (result: String?, error: String)->Unit
    lateinit var degView: TextView
    lateinit var desView: TextView
    private val token = "d4c9eea0d00fb43230b479793d6aa78f"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private val weatherList = ArrayList<Weather>()




    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        callback = { result, error ->
            if(result != null) {
// перед заполнением очищаем список
                try{
                weatherList.clear()

                val json = JSONObject(result)
                val list = json.getJSONArray("list")

// перебираем json массив
                for(i in 0 until list.length()){
                    val item = list.getJSONObject(i)
                    val weather = item.getJSONArray("weather").getJSONObject(0)

// добавляем в список новый элемент
                    weatherList.add(
                            Weather(
                                    item.getInt("dt"),
                                    item.getJSONObject("main").getDouble("temp"),
                                    item.getJSONObject("main").getInt("humidity"),
                                    weather.getString("icon"),
                                    weather.getString("description"),
                                    item.getJSONObject("wind").getDouble("speed"),
                                    item.getJSONObject("wind").getInt("deg"),
                                    item.getString("dt_txt")

                            )
                    )

                }

                runOnUiThread {
                    showDetailsInfo(weatherList[0])
// уведомляем визуальный элемент, что данные изменились
                    dailyInfoRecyclerView.adapter?.notifyDataSetChanged()
                    textView.text = json.getJSONObject("city").getString("name")
                    tempView.text = weatherList[0].mainTemp.toString()
                    desView.text = weatherList[0].weatherDescription
                    spView.text = weatherList[0].windSpeed.toString()
                }
            }
                catch (e:Exception){
                    AlertDialog.Builder(this)
                            .setTitle("Ошибка работы с Json")
                            .setMessage(e.toString())
                            .setPositiveButton("OK", null)
                            .create()
                            .show()
                }
            }
            else{
                AlertDialog.Builder(this)
                        .setTitle("Заголовок")
                        .setMessage(error)
                        .setPositiveButton("OK", null)
                        .create()
                        .show()
            }



        }
        val splash = findViewById<ImageView>(R.id.splash)
        object : CountDownTimer(5000,1000)
        {


            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onTick(millisUntilFinished: Long) {

                // заставляем пялиться на нашу заставку как минимум 3 секунды
                counter++
                if(counter>3 && ready)
                {
                    // данные получены - скрываем заставку
                    splash.elevation = 0F
                    this.cancel()
                }
            }


            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onFinish(){
                splash.elevation = 0F
            }
        }.start()

        textView = findViewById<TextView>(R.id.head)
        tempView = findViewById<TextView>(R.id.tempa)
        humView = findViewById<TextView>(R.id.humidity)
        spView = findViewById<TextView>(R.id.speed)
        degView = findViewById<TextView>(R.id.deg)
        desView = findViewById<TextView>(R.id.description)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkPermission()
        dailyInfoRecyclerView = findViewById(R.id.dailyInfoRecyclerView)
        dailyInfoRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val weatherAdapter = WeatherAdapter(weatherList, this)
        weatherAdapter.setItemClickListener {
            runOnUiThread {
                showDetailsInfo(it)
            }
        }
        dailyInfoRecyclerView.adapter = weatherAdapter

    }

    private fun showDetailsInfo(weather: Weather) {
        //TODO("Not yet implemented")
    }

    private fun checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) !=PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
        else{
            mLocationRequest = LocationRequest()
            mLocationRequest.interval = 10000
            mLocationRequest.fastestInterval = 100
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())


        }

    }

    private var mLocationCallback: LocationCallback = object : LocationCallback(){
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult.locations.isNotEmpty()){
                val locIndex = locationResult.locations.size - 1
                val lon = locationResult.locations[locIndex].longitude
                val lat = locationResult.locations[locIndex].latitude
                onGetCoordinates(lat, lon)
            }
        }
    }






    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun onGetCoordinates(lat: Double, lon: Double) {
        try {
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
        val url = "https://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&appid=${token}&lang=ru&units=metric"

        HTTP.requestGET(url, callback)
        }
        catch (e: Exception){
            AlertDialog.Builder(this)
                    .setTitle("Ошибка запроса по координатам")
                    .setMessage(e.toString())
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
        }
    }

    fun CityClick(view: View) {
        startActivityForResult(
                Intent(this, CityListActivity::class.java),
                1)
    }
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        val name = data.getStringExtra("cityName")

        val url = " https://api.openweathermap.org/data/2.5/forecast?q=${name}&units=metric&appid=${token}&lang=ru&mode=xml"

        HTTP.requestGET(url){result, error ->
            if(result != null) {
                try {
                    weatherList.clear()
                    val factory = XmlPullParserFactory.newInstance()
                    factory.isNamespaceAware = true
                    val parser = factory.newPullParser()
                    parser.setInput(StringReader(result))
                    var cityName = ""
                    var topTag = ""
                    var subTag = ""
                    var dt_txt = ""
                    var description: String = ""
                    var icon: String = ""
                    while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                        when (parser.eventType) {
                            XmlPullParser.START_TAG -> {
                                when (parser.name) {
                                    // в качестве начальных тегов нам интересны "location" и "forecast"
                                    "location", "forecast" -> topTag = parser.name
                                    "name" -> {
                                        // внутри "location" в теге "name" читаем название города
                                        if (topTag == "location") cityName = parser.nextText()

                                    }
                                    "time" -> {
                                        if (topTag == "forecast") {
                                            // внутри "forecast" нам интересно содержимое "time"
                                            subTag = parser.name
                                            // и сразу считываем СТРОКОВУЮ дату
                                            dt_txt = parser.getAttributeValue(null, "from").toString()
                                        }
                                    }
                                    "symbol" -> {
                                        if (subTag == "time") {
                                            description = parser.getAttributeValue(null, "name").toString()
                                            icon = parser.getAttributeValue(null, "var").toString()
                                        }
                                    }
                                    // тут мне как обычно лень стало расписывать остальные теги
                                    "temperature" -> {
                                        temp = parser.getAttributeValue(null, "value").toDouble()
                                    }

                                    "humidity" -> {
                                        hum = parser.getAttributeValue(null, "value").toInt()
                                    }

                                    "windSpeed" -> {
                                        sp = parser.getAttributeValue(null, "mps").toDouble()
                                    }

                                    "windDirection" -> {
                                        deg = parser.getAttributeValue(null, "deg").toInt()
                                    }

                                }
                            }
                            XmlPullParser.END_TAG -> {
                                when (parser.name) {
                                    "time" -> {
                                        subTag = ""
                                        weatherList.add(
                                                Weather(
                                                        0,
                                                        temp,
                                                        hum,
                                                        icon,
                                                        description,
                                                        sp,
                                                        deg,
                                                        dt_txt
                                                )
                                        )
                                    }
                                }
                            }

                        }
                        parser.next()
                    }

                    runOnUiThread {
                        showDetailsInfo(weatherList[0])
// уведомляем визуальный элемент, что данные изменились

                        textView.text = cityName
                        tempView.text = weatherList[0].mainTemp.toString()
                        desView.text = weatherList[0].weatherDescription
                        spView.text = weatherList[0].windSpeed.toString()
                        dailyInfoRecyclerView.adapter?.notifyDataSetChanged()
                    }

            }
                catch (e: Exception)
                {
                    AlertDialog.Builder(this)
                            .setTitle("Ошибка разбора XML")
                            .setMessage(e.toString())
                            .setPositiveButton("OK", null)
                            .create()
                            .show()
                }
            }
            else
            {
                AlertDialog.Builder(this)
                        .setTitle("Ошибка работы с интернетом")
                        .setMessage(error)
                        .setPositiveButton("OK", null)
                        .create()
                        .show()
            }

        }

    }
}