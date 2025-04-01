package com.example.sosremasterd.utils

import android.content.Context
import android.location.Location
import android.telephony.SmsManager
import android.util.Log
import com.example.sosremasterd.data.EmergencyContact

class AlertManager(private val context: Context) {
    private val smsManager: SmsManager? by lazy {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
        } catch (e: Exception) {
            Log.e("AlertManager", "Failed to initialize SMS Manager", e)
            null
        }
    }

    fun sendEmergencyAlert(
        location: Location,
        contacts: List<EmergencyContact>
    ) {
        Log.d("AlertManager", "Attempting to send emergency alert")
        Log.d("AlertManager", "Number of contacts: ${contacts.size}")
        Log.d("AlertManager", "Location: lat=${location.latitude}, lon=${location.longitude}")
        
        if (smsManager == null) {
            Log.e("AlertManager", "SMS Manager not available, cannot send alerts")
            return
        }
        
        val message = createEmergencyMessage(location)
        Log.d("AlertManager", "Created message: $message")

        contacts.forEach { contact ->
            Log.d("AlertManager", "Processing contact: ${contact.name} (${contact.phoneNumber})")
            if (contact.notifyByMessage) {
                try {
                    Log.d("AlertManager", "Attempting to send SMS to ${contact.phoneNumber}")
                    sendSMS(contact.phoneNumber, message)
                    Log.d("AlertManager", "Successfully sent SMS to ${contact.phoneNumber}")
                } catch (e: Exception) {
                    Log.e("AlertManager", "Failed to send SMS to ${contact.phoneNumber}", e)
                }
            } else {
                Log.d("AlertManager", "Contact ${contact.name} has SMS notifications disabled")
            }
        }
    }

    private fun createEmergencyMessage(location: Location): String {
        return """
            EMERGENCY SOS ALERT!
            Location: https://www.google.com/maps?q=${location.latitude},${location.longitude}
            This is an automated emergency alert. The sender may be in danger and requires immediate assistance.
            
            Accuracy: ${location.accuracy}m
            Speed: ${if (location.hasSpeed()) "${location.speed}m/s" else "Unknown"}
            Time: ${java.util.Date(location.time)}
        """.trimIndent()
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            Log.d("AlertManager", "Dividing message into parts")
            val parts = smsManager?.divideMessage(message) ?: run {
                Log.e("AlertManager", "SMS Manager is null in sendSMS")
                return
            }
            Log.d("AlertManager", "Message divided into ${parts.size} parts")

            smsManager?.sendMultipartTextMessage(
                phoneNumber,
                null,
                parts,
                null,
                null
            )
            Log.d("AlertManager", "SMS sent successfully to $phoneNumber")
        } catch (e: Exception) {
            Log.e("AlertManager", "Error sending SMS to $phoneNumber", e)
            throw e
        }
    }
} 