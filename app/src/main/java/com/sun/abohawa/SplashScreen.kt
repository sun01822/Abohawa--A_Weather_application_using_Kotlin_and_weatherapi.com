package com.sun.abohawa

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.squareup.picasso.Picasso
import com.sun.abohawa.databinding.ActivitySplashScreenBinding
import java.util.*

class SplashScreen : AppCompatActivity() {
    private lateinit var leftAnim : Animation
    private lateinit var rightAnim : Animation
    private lateinit var mfusedlocation : FusedLocationProviderClient
    private lateinit var binding : ActivitySplashScreenBinding
    private var myRequestCode = 1010
    private lateinit var cityName : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setContentView(binding.root)
        Glide.with(this).load(R.drawable.cloud).into(binding.imageView)
        leftAnim = AnimationUtils.loadAnimation(this, R.anim.left_to_right)
        rightAnim = AnimationUtils.loadAnimation(this, R.anim.right_to_left)
        binding.imageView.startAnimation(leftAnim)
        binding.textView.startAnimation(rightAnim)
        mfusedlocation = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
        delayTimer()

    }
    private fun delayTimer() {
        Handler(Looper.getMainLooper()).postDelayed({
            goToNext()
        },3500)
    }
    private fun goToNext() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("cityName", cityName)
        startActivity(intent)
        finish()
    }
    private fun getCityName(lat: Double,long: Double):String{
        val cityName: String?
        val geoCoder = Geocoder(this, Locale.getDefault())
        val address = geoCoder.getFromLocation(lat,long,1)

        cityName = address!![0].adminArea
        return cityName
    }

    @SuppressLint("MissingPermission")
    private  fun getLastLocation() {
        if(checkPermission()){
            if(locationEnable()){
                mfusedlocation.lastLocation.addOnCompleteListener {
                        task->
                    val location: Location?=task.result
                    if(location == null){
                        newLocation()
                    }else{
                        Log.i("Location", location.longitude.toString())
                        Log.i("Location", location.latitude.toString())
                        cityName = getCityName(location.latitude, location.longitude)
                    }
                }
            }
            else{
                Toast.makeText(this, "Please turn your GPS location on", Toast.LENGTH_SHORT).show()
            }
        }else{
            requestPermission()
        }
    }

    private fun locationEnable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun newLocation() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        mfusedlocation = LocationServices.getFusedLocationProviderClient(this)
        mfusedlocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private val locationCallback=object : LocationCallback(){
        override fun onLocationResult(p0 : LocationResult){
            p0.lastLocation!!
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), myRequestCode)
    }

    private fun checkPermission(): Boolean {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == myRequestCode){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation()
            }
        }
    }
}