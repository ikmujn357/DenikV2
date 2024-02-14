package com.usbapps.climbingdiary.custom

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import com.usbapps.climbingdiary.view.AddActivity

/**
 * Pomocní třída pro správu polohy.
 * Poskytuje metody pro požadování aktualizací polohy a odstraňování posluchačů polohy.
 *
 * @param context Kontext aplikace.
 */
class LocationHelper(private val context: Context) {
    private var locationManager: LocationManager? = null

    /**
     * Inicializuje správce polohy při vytvoření instance LocationHelper.
     * Pokusí se získat referenci na LocationManager pomocí systémové služby LOCATION_SERVICE.
     *
     * @param context Kontext aplikace, který je potřeba k získání systémových služeb.
     */
    init {
        // Inicializace správce polohy
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    /**
     * Požaduje aktualizace polohy od správce polohy.
     *
     * @param locationListener Posluchač polohy, který zpracovává aktualizace polohy.
     */
    fun requestLocationUpdates(locationListener: LocationListener) {
        try {
            // Přihlásit se k aktualizacím polohy od GPS poskytovatele
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

    /**
     * Odstraní aktualizace polohy od správce polohy.
     *
     * @param locationListener Posluchač polohy, který je odstraněn z aktualizací polohy.
     */
    fun removeLocationUpdates(locationListener: LocationListener) {
        try {
            // Odhlašte se z aktualizací polohy
            locationManager?.removeUpdates(locationListener)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}

/**
 * Třída implementující rozhraní LocationListener pro sledování změn polohy.
 * Uchovává poslední získanou polohu a aktualizuje UI aktivity s touto polohou při změně polohy.
 *
 * @property activity Aktivita, která bude aktualizována s novou polohou.
 */
class MyLocationListener(private val activity: AddActivity) : LocationListener {
    // Poslední získaná poloha
    var lastLocation: Location? = null
        private set

    /**
     * Metoda volaná při změně aktuální polohy.
     * Pokud ještě není známa poslední poloha, uloží aktuální polohu jako poslední získanou polohu a
     * aktualizuje UI aktivity s touto polohou. Poté zastaví aktualizace polohy.
     *
     * @param location Nová poloha.
     */
    override fun onLocationChanged(location: Location) {
        // Zastavte aktualizace polohy, pokud už máte polohu
        if (lastLocation == null) {
            // Uložte aktuální polohu jako poslední získanou polohu
            lastLocation = location
            val latitude = location.latitude
            val longitude = location.longitude
            Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
            Log.d("last", "${lastLocation!!.latitude}")
            // Aktualizujte UI aktivity s poslední polohou
            activity.updateEditTextWithLastLocation()

            // Zastavte aktualizace polohy po prvním získání polohy
            activity.locationHelper.removeLocationUpdates(this)
        }
    }
}
