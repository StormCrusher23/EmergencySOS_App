package com.example.sosremasterd.service

import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import com.example.sosremasterd.utils.AlertManager
import com.example.sosremasterd.utils.LocationManager
import com.example.sosremasterd.utils.PreferencesManager
import com.example.sosremasterd.utils.RecordingManager
import com.example.sosremasterd.utils.VideoRecordingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration.Companion.seconds
import androidx.lifecycle.LifecycleService

class SOSService : LifecycleService() {
    private val serviceScope = CoroutineScope(Job() + Dispatchers.Default)
    private val buttonPressQueue = ConcurrentLinkedQueue<Int>()
    private val _sosState = MutableStateFlow<SOSState>(SOSState.Idle)
    private lateinit var recordingManager: RecordingManager
    private lateinit var videoRecordingManager: VideoRecordingManager
    private lateinit var locationManager: LocationManager
    private lateinit var alertManager: AlertManager
    private var locationJob: Job? = null
    private lateinit var preferencesManager: PreferencesManager
    
    inner class LocalBinder : Binder() {
        fun getService(): SOSService = this@SOSService
    }

    private val binder = LocalBinder()
    
    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        recordingManager = RecordingManager(this)
        videoRecordingManager = VideoRecordingManager(this, this)
        locationManager = LocationManager(this)
        alertManager = AlertManager(this)
        startButtonDetection()
    }

    private fun startButtonDetection() {
        serviceScope.launch {
            while (true) {
                if (buttonPressQueue.size >= preferencesManager.triggerCombination.size) {
                    checkTriggerCombination()
                }
                delay(100) // Check every 100ms
            }
        }
    }

    private fun checkTriggerCombination() {
        val pressedButtons = buttonPressQueue.toList()
        val savedCombination = preferencesManager.triggerCombination
        if (pressedButtons == savedCombination) {
            activateSOS()
        }
        // Clear old button presses
        buttonPressQueue.clear()
    }

    private fun activateSOS() {
        if (_sosState.value != SOSState.Active) {  // Only activate if not already active
            Log.d("SOSService", "Activating SOS")
            _sosState.value = SOSState.Active
            // Start 5-second countdown for cancellation
            serviceScope.launch {
                Log.d("SOSService", "Starting 5-second countdown")
                delay(5.seconds)
                if (_sosState.value == SOSState.Active) {
                    Log.d("SOSService", "Countdown complete, starting emergency procedures")
                    startEmergencyProcedures()
                } else {
                    Log.d("SOSService", "SOS was cancelled during countdown")
                }
            }
        } else {
            Log.d("SOSService", "SOS already active, ignoring activation request")
        }
    }

    private fun startEmergencyProcedures() {
        serviceScope.launch {
            try {
                Log.d("SOSService", "Starting emergency procedures")
                startRecording()
                
                // Wait for a valid location
                var location: Location? = null
                var attempts = 0
                while (location == null && attempts < 5) {
                    Log.d("SOSService", "Attempting to get location, attempt ${attempts + 1}")
                    location = locationManager.getCurrentLocation()
                    if (location == null) {
                        delay(1000) // Wait 1 second before trying again
                        attempts++
                    }
                }

                location?.let {
                    Log.d("SOSService", "Got location: lat=${it.latitude}, lon=${it.longitude}")
                    sendLocationUpdate(it)
                } ?: Log.e("SOSService", "Failed to get location after $attempts attempts")

                startLocationUpdates()
            } catch (e: Exception) {
                Log.e("SOSService", "Error in emergency procedures", e)
            }
        }
    }

    private suspend fun startRecording() {
        _sosState.value = SOSState.Recording
        withContext(Dispatchers.Main) {
            recordingManager.startAudioRecording()
            videoRecordingManager.startVideoRecording()
        }
    }

    private suspend fun startLocationUpdates() {
        locationJob?.cancel() // Cancel any existing location updates
        locationJob = serviceScope.launch {
            locationManager.getLocationUpdates().collect { location ->
                sendLocationUpdate(location)
            }
        }
    }

    private suspend fun sendLocationUpdate(location: Location) {
        withContext(Dispatchers.IO) {
            try {
                val emergencyContacts = preferencesManager.emergencyContacts
                Log.d("SOSService", "Sending location to ${emergencyContacts.size} contacts")
                emergencyContacts.forEach { contact ->
                    Log.d("SOSService", "Contact: ${contact.name} - ${contact.phoneNumber}")
                }
                
                if (emergencyContacts.isNotEmpty()) {
                    alertManager.sendEmergencyAlert(location, emergencyContacts)
                } else {
                    Log.w("SOSService", "No emergency contacts configured")
                }
            } catch (e: Exception) {
                Log.e("SOSService", "Error sending location update", e)
                e.printStackTrace()
            }
        }
    }

    fun handleKeyEvent(keyCode: Int) {
        buttonPressQueue.offer(keyCode)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (_sosState.value == SOSState.Recording) {
            recordingManager.stopRecording()
            videoRecordingManager.stopRecording()
        }
        locationJob?.cancel()
        _sosState.value = SOSState.Idle
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("SOSService", "Service started with intent: $intent")
        if (intent?.getBooleanExtra("TRIGGER_ACTIVATED", false) == true) {
            Log.d("SOSService", "Trigger activation received, starting emergency procedures")
            activateSOS()
        }
        return START_STICKY
    }
}

sealed class SOSState {
    object Idle : SOSState()
    object Active : SOSState()
    object Recording : SOSState()
} 