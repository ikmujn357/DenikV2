package com.example.denikv1.custom

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.example.denikv1.view.AddActivity

class LocationHelper(private val context: Context) {

    private var locationManager: LocationManager? = null

    init {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    fun requestLocationUpdates(locationListener: LocationListener) {
        try {
            // Přihlásit se k aktualizacím polohy
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun removeLocationUpdates(locationListener: LocationListener) {
        try {
            // Odhlašte se z aktualizací polohy
            locationManager?.removeUpdates(locationListener)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}

class MyLocationListener(private val activity: AddActivity) : LocationListener {
    var lastLocation: Location? = null
        private set

    override fun onLocationChanged(location: Location) {
        // Zastavte aktualizace polohy, pokud už máte polohu
        if (lastLocation == null) {
            // Zde máte aktualizovanou polohu
            lastLocation = location
            val latitude = location.latitude
            val longitude = location.longitude
            Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
            Log.d("last", "${lastLocation!!.latitude}")
            // Zde můžete dělat cokoliv s aktuální polohou
            activity.updateEditTextWithLastLocation()

            // Zastavte aktualizace polohy po prvním získání polohy
            activity.locationHelper.removeLocationUpdates(this)
        }
    }
}

