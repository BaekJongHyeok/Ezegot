package com.jonghyeok.ezegot.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.jonghyeok.ezegot.App.Companion.context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class LocationRepository(context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000).build()

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() = callbackFlow<Location> {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            close()
            return@callbackFlow
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    trySend(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}