package com.example.draggps

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private lateinit var locationManager: LocationManager
    private lateinit var speedText: TextView
    private lateinit var timeText: TextView
    private lateinit var distanceText: TextView

    private var running = false
    private var startTime = 0L
    private var selectedDistance = 60.0

    private val handler = Handler(Looper.getMainLooper())

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {

            if (!running) return

            // Kecepatan GPS dalam km/jam
            if (location.hasSpeed()) {
                val speedKmh = location.speed * 3.6
                speedText.text = String.format("%.1f km/h", speedKmh)
            }

            val elapsed = System.currentTimeMillis() - startTime
            val seconds = elapsed / 1000.0

            timeText.text = String.format("%.2f s", seconds)

            // Estimasi jarak berdasarkan GPS
            if (location.hasAccuracy()) {
                distanceText.text =
                    String.format("GPS ± %.1f m", location.accuracy)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER_HORIZONTAL
        layout.setPadding(30, 40, 30, 30)

        val title = TextView(this)
        title.text = "DRAG GPS"
        title.textSize = 32f
        title.gravity = Gravity.CENTER
        title.setTextColor(Color.WHITE)

        val subtitle = TextView(this)
        subtitle.text = "GPS PERFORMANCE METER"
        subtitle.textSize = 14f
        subtitle.gravity = Gravity.CENTER
        subtitle.setTextColor(Color.LTGRAY)

        val spinner = Spinner(this)

        val distances = arrayOf(
            "60 m",
            "100 m",
            "201 m",
            "203 m",
            "402 m"
        )

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            distances
        )

        spinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    selectedDistance = distances[position]
                        .replace(" m", "")
                        .toDouble()

                    distanceText.text =
                        "Jarak: ${selectedDistance.toInt()} m"
                }
            }

        distanceText = TextView(this)
        distanceText.text = "Jarak: 60 m"
        distanceText.textSize = 22f
        distanceText.gravity = Gravity.CENTER
        distanceText.setPadding(0, 30, 0, 10)

        timeText = TextView(this)
        timeText.text = "0.00 s"
        timeText.textSize = 55f
        timeText.gravity = Gravity.CENTER

        speedText = TextView(this)
        speedText.text = "0.0 km/h"
        speedText.textSize = 35f
        speedText.gravity = Gravity.CENTER

        val startButton = Button(this)
        startButton.text = "START"

        val stopButton = Button(this)
        stopButton.text = "STOP / RESET"

        startButton.setOnClickListener {
            startMeasurement()
        }

        stopButton.setOnClickListener {
            stopMeasurement()
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(spinner)
        layout.addView(distanceText)
        layout.addView(timeText)
        layout.addView(speedText)
        layout.addView(startButton)
        layout.addView(stopButton)

        layout.setBackgroundColor(Color.BLACK)

        setContentView(layout)

        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )
        }
    }

    private fun startMeasurement() {

        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }

        running = true
        startTime = System.currentTimeMillis()

        timeText.text = "0.00 s"

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            100L,
            0.5f,
            locationListener
        )

        updateTimer()
    }

    private fun updateTimer() {
        if (!running) return

        val elapsed =
            System.currentTimeMillis() - startTime

        timeText.text =
            String.format("%.2f s", elapsed / 1000.0)

        handler.postDelayed({
            updateTimer()
        }, 50)
    }

    private fun stopMeasurement() {
        running = false

        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.removeUpdates(locationListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.removeUpdates(locationListener)
        }
    }
}
