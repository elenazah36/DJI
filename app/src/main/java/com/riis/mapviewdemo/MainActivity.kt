package com.riis.mapviewdemo

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import dji.common.flightcontroller.FlightControllerState


class MainActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

        private lateinit var record: Button
        private lateinit var posText: TextView

        fun checkGpsCoordination(latitude: Double, longitude: Double): Boolean { // this will check if your gps coordinates are valid
                return latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180 && latitude != 0.0 && longitude != 0.0
        }

        private var droneLocationLat: Double = 15.0
        private var droneLocationLng: Double = 15.0
        private var droneMarker: Marker? = null
        private var mapboxMap: MapboxMap? = null

        private fun initFlightController() {
        // this will initialize the flight controller with predetermined data
        DJIDemoApplication.getFlightController()?.let { flightController ->
            flightController.setStateCallback { flightControllerState ->
                // set the latitude and longitude of the drone based on aircraft location
                droneLocationLat = flightControllerState.aircraftLocation.latitude
                droneLocationLng = flightControllerState.aircraftLocation.longitude
                updateDroneLocation() // this will be called on the main thread
                 }
             }
         }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            Mapbox.getInstance(this, getString(R.string.mapbox_access_token)) // this will get your mapbox instance using your access token
            setContentView(R.layout.activity_main) // use the activity layout
            initUi() // initialize the UI
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.onCreate(savedInstanceState)
            mapFragment.getMapAsync(this)
        }

        override fun onMapReady(mapboxMap: MapboxMap) {
            this.mapboxMap = mapboxMap // initialize the map
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { // set the view of the map
            }
        }

        override fun onResume() {
            super.onResume()
            initFlightController()
        }

        private fun initUi() {
            record = findViewById(R.id.btn_record);
            record.setOnClickListener(this)
        }

        private fun updateDroneLocation() { // this will draw the aircraft as it moves
            //Log.i(TAG, "Drone Lat: $droneLocationLat - Drone Lng: $droneLocationLng")
            if (droneLocationLat.isNaN() || droneLocationLng.isNaN())  { return }
            val sb = StringBuffer()
            val pos = LatLng(droneLocationLat, droneLocationLng)
            // the following will draw the aircraft on the screen
            val markerOptions = MarkerOptions()
                .position(pos)
                .icon(IconFactory.getInstance(this).fromResource(R.drawable.aircraft))
            runOnUiThread {
                droneMarker?.remove()
                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                    droneMarker = mapboxMap?.addMarker(markerOptions)
                    sb.append("Latitude:").append(pos.latitude).append("\n")
                    sb.append("Longitude:").append(pos.longitude).append("\n")
                    sb.append("Altitude:").append(pos.altitude).append("\n")
                    showToast(sb.toString())
                    posText.text = sb.toString() }
                }
            }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.btn_record -> { // will draw the drone and move camera to the position of the drone on the map
                    updateDroneLocation()
                    cameraUpdate()
                } else -> {}
            }
         }

        private fun cameraUpdate() { // update where you're looking on the map
            if (droneLocationLat.isNaN() || droneLocationLng.isNaN())  { return }
            val pos = LatLng(droneLocationLat, droneLocationLng)
            val zoomLevel = 18.0
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(pos, zoomLevel)
            mapboxMap?.moveCamera(cameraUpdate)
        }

        private fun showToast(msg: String?) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }


}