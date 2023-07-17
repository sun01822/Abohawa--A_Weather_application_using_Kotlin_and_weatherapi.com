package com.sun.abohawa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.sun.abohawa.databinding.ActivityMainBinding
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import com.sun.abohawa.adapter.WeatherAdapter
import com.sun.abohawa.model.WeatherModel
import org.json.JSONException




class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var list : ArrayList<WeatherModel>
    private lateinit var weatheradapter : WeatherAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        list = ArrayList()
        binding.searchButton.setOnClickListener {
            val city = binding.editTextCityName.text.toString()
            if (city.isEmpty()) {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            } else {
                binding.cityName.text = city
                getWeatherInfo(city)
            }
        }
        val cityName = intent.getStringExtra("cityName")
        getWeatherInfo(cityName!!)
    }
    private fun getWeatherInfo(cityName: String) {
        val url =
            "https://api.weatherapi.com/v1/forecast.json?key=315755977d28461e99b30338231507&q=$cityName&days=1"
        binding.cityName.text = cityName
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                binding.progressBar.visibility = View.INVISIBLE
                binding.home.visibility = View.VISIBLE
                try {
                    val temperature: String = response.getJSONObject("current").getString("temp_c")
                    binding.temperature.text = "$temperature°c"
                    val isDay: Int = response.getJSONObject("current").getInt("is_day")
                    val condition: String = response.getJSONObject("current").getJSONObject("condition").getString("text")
                    val conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon")
                    binding.conditonTextView.text = condition
                    Picasso.get().load("https:$conditionIcon").into(binding.tempIcon)
                    if (isDay == 1) {
                        Picasso.get().load(R.drawable.day).into(binding.imageView)
                    } else {
                        Picasso.get().load(R.drawable.night).into(binding.imageView)
                    }
                    val forecastArray = response.getJSONObject("forecast").getJSONArray("forecastday")
                    if (forecastArray.length() > 0) {
                        val hourArray = forecastArray.getJSONObject(0).getJSONArray("hour")
                        for (i in 0 until hourArray.length()) {
                            val hourObj = hourArray.getJSONObject(i)
                            val time = hourObj.getString("time")
                            val temp = hourObj.getDouble("temp_c")
                            val windSpeed = hourObj.getDouble("wind_kph")
                            val iconUrl = hourObj.getJSONObject("condition").getString("icon")

                            val weatherData = WeatherModel(time, temp.toString(), iconUrl, windSpeed.toString() )
                            list.add(weatherData)
                        }
                        weatheradapter = WeatherAdapter(this, list)
                        binding.recyclerviewWeather.adapter = weatheradapter
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }
}
