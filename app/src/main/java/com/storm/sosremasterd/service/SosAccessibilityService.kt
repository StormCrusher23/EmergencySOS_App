package com.storm.sosremasterd.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.storm.sosremasterd.utils.PreferencesManager

class SosAccessibilityService : AccessibilityService() {
    private lateinit var preferencesManager: PreferencesManager
    private val keyPressBuffer = mutableListOf<Int>()
    
    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    Log.d("SosAccessibilityService", "Key pressed: ${event.keyCode}")
                    handleKeyPress(event.keyCode)
                    return true
                }
            }
        }
        return super.onKeyEvent(event)
    }

    private fun handleKeyPress(keyCode: Int) {
        keyPressBuffer.add(keyCode)
        Log.d("SosAccessibilityService", "Added key: $keyCode, Buffer size: ${keyPressBuffer.size}")
        
        // Check if the buffer matches the trigger combination
        val triggerCombination = preferencesManager.triggerCombination
        if (keyPressBuffer.size >= triggerCombination.size) {
            val lastKeys = keyPressBuffer.takeLast(triggerCombination.size)
            Log.d("SosAccessibilityService", "Checking combination - Last keys: $lastKeys, Trigger: $triggerCombination")
            if (lastKeys == triggerCombination) {
                Log.d("SosAccessibilityService", "Trigger combination detected! Starting SOS Service")
                // Start the SOS Service
                val intent = Intent(this, SOSService::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("TRIGGER_ACTIVATED", true)
                }
                startService(intent)
                keyPressBuffer.clear()
            }
        }

        // Keep buffer size manageable
        if (keyPressBuffer.size > 10) {
            keyPressBuffer.removeAt(0)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not needed for key event monitoring
    }

    override fun onInterrupt() {
        // Handle interruption if needed
    }

    override fun onServiceConnected() {
        Log.d("SosAccessibilityService", "Service connected")
    }
} 