package com.example.sosremasterd.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        Log.d("LocationManager", "Starting location updates")
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    Log.d("LocationManager", "New location update: lat=${location.latitude}, lon=${location.longitude}")
                    trySend(location)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d("LocationManager", "Location updates requested successfully")
        } catch (e: Exception) {
            Log.e("LocationManager", "Error requesting location updates", e)
        }

        awaitClose {
            Log.d("LocationManager", "Stopping location updates")
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            Log.d("LocationManager", "Getting current location...")
            
            suspendCoroutine { continuation ->
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    5000
                ).build()

                var isLocationReceived = false
                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        if (!isLocationReceived) {
                            result.lastLocation?.let { location ->
                                Log.d("LocationManager", "Fresh location received: lat=${location.latitude}, lon=${location.longitude}")
                                isLocationReceived = true
                                fusedLocationClient.removeLocationUpdates(this)
                                continuation.resume(location)
                            }
                        }
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )

                // Fallback to last known location after 10 seconds
                android.os.Handler(Looper.getMainLooper()).postDelayed({
                    if (!isLocationReceived) {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location ->
                                if (location != null) {
                                    Log.d("LocationManager", "Using last known location: lat=${location.latitude}, lon=${location.longitude}")
                                    continuation.resume(location)
                                } else {
                                    Log.w("LocationManager", "No location available")
                                    continuation.resume(null)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("LocationManager", "Error getting last location", e)
                                continuation.resume(null)
                            }
                    }
                }, 10000) // 10 second timeout
            }
        } catch (e: Exception) {
            Log.e("LocationManager", "Error getting location", e)
            null
        }
    }
} 