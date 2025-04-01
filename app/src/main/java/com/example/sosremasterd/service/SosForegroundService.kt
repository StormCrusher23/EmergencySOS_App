package com.example.sosremasterd.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.content.Context
import android.graphics.Color
import com.example.sosremasterd.MainActivity
import com.example.sosremasterd.R
import com.example.sosremasterd.utils.PreferencesManager
import java.util.concurrent.ConcurrentLinkedQueue
import kotlinx.coroutines.*
import android.view.KeyEvent
import android.util.Log

class SosForegroundService : Service() {
    private val serviceScope = CoroutineScope(Job() + Dispatchers.Default)
    private val buttonPressQueue = ConcurrentLinkedQueue<Int>()
    private lateinit var preferencesManager: PreferencesManager

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "sos_service_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startButtonDetection()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "SOS Service Channel",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "SOS monitoring service"
            enableLights(true)
            lightColor = Color.RED
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("SOS Monitor Active")
            .setContentText("Monitoring for emergency triggers")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
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
        Log.d("SosForegroundService", "Checking combination - Pressed: $pressedButtons, Saved: $savedCombination")
        if (pressedButtons == savedCombination) {
            Log.d("SosForegroundService", "Trigger combination matched! Starting SOS Service")
            // Trigger SOS
            val sosIntent = Intent(this, SOSService::class.java).apply {
                putExtra("TRIGGER_ACTIVATED", true)
            }
            startService(sosIntent)
        }
        buttonPressQueue.clear()
    }

    fun handleKeyEvent(keyCode: Int) {
        Log.d("SosForegroundService", "Received key event: $keyCode")
        buttonPressQueue.offer(keyCode)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If the service gets killed, restart it
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the SOSService when the foreground service is stopped
        val sosIntent = Intent(this, SOSService::class.java)
        stopService(sosIntent)
        serviceScope.cancel()
    }
} 