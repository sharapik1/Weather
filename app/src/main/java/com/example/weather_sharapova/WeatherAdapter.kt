package com.example.weather_sharapova

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherAdapter(
        private val values: ArrayList<Weather>,
        private val activity: Activity
): RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {

    // обработчик клика по элементу списка (лямбда выражение), может быть не задан
    private var itemClickListener: ((Weather) -> Unit)? = null

    fun setItemClickListener(itemClickListener: (Weather) -> Unit) {
        this.itemClickListener = itemClickListener
    }

    // Метод onCreateViewHolder вызывается при создании визуального элемента
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // грузим layout, который содержит вёрстку элемента списка (нарисуйте сами)
        val itemView = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.weather_item,
                        parent,
                        false)

        // создаем на его основе ViewHolder
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = values.size

    // заполняет визуальный элемент данными
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sdf_ = SimpleDateFormat("EEEE")
        var date : LocalDateTime? = null
        var dayName = ""

        val weekDays = arrayListOf<String>("Пн","Вт","Ср","Чт","Пт","Сб","Вс")

        try{
            date = LocalDateTime.parse(
                    values[position].dtTxt,
                    DateTimeFormatter.ofPattern("y-M-d H:m:s")
            )

            dayName = weekDays[date.dayOfWeek.ordinal]

        }
        catch (e: java.lang.Exception){
            Log.d("Kate", e.toString())
        }
        holder.timeView.text = ("" + dayName + "\n" + values[position].dtTxt.substring(11,16) + "")
        holder.tempTextView.text = "${values[position].mainTemp} C"

        holder.arrow.rotation = values[position].windDeg.toFloat()

        // onIconLoad.invoke(holder.iconImageView, values[position].weatherIcon)

        holder.container.setOnClickListener {
            //кликнули на элемент списка
            itemClickListener?.invoke(values[position])
        }

        HTTP.getImage("https://openweathermap.org/img/w/${values[position].weatherIcon}.png") { bitmap, error ->
            if (bitmap != null) {
                activity.runOnUiThread {
                    try {
                        holder.iconImageView.setImageBitmap(bitmap)
                    } catch (e: Exception) {

                    }
                }
            } else
                Log.d("KEILOG", error)
        }
    }

    //Реализация класса ViewHolder, хранящего ссылки на виджеты.
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var iconImageView: ImageView = itemView.findViewById(R.id.weather_icon)
        var tempTextView: TextView = itemView.findViewById(R.id.weather_temp)
        var container: LinearLayout = itemView.findViewById(R.id.container)
        var timeView: TextView = itemView.findViewById(R.id.weather_time)
        var arrow: ImageView = itemView.findViewById(R.id.arrow)

    }
}
