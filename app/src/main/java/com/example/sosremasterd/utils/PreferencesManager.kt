package com.example.sosremasterd.utils

import android.content.Context
import android.content.SharedPreferences
import android.view.KeyEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import com.example.sosremasterd.data.EmergencyContact

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { 
        ignoreUnknownKeys = true 
        prettyPrint = true
        isLenient = true
    }

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    var isSetupCompleted: Boolean
        get() = prefs.getBoolean(KEY_SETUP_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_SETUP_COMPLETED, value).apply()

    var triggerCombination: List<Int>
        get() {
            val combinationString = prefs.getString(KEY_TRIGGER_COMBINATION, "") ?: ""
            return if (combinationString.isEmpty()) {
                // Default combination if none is set
                listOf(KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN)
            } else {
                combinationString.split(",").map { it.toInt() }
            }
        }
        set(value) {
            val combinationString = value.joinToString(",")
            prefs.edit().putString(KEY_TRIGGER_COMBINATION, combinationString).apply()
        }

    var emergencyContacts: List<EmergencyContact>
        get() {
            val contactsJson = prefs.getString(KEY_EMERGENCY_CONTACTS, null)
            return if (contactsJson != null) {
                try {
                    json.decodeFromString(contactsJson)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
        set(value) {
            try {
                val contactsJson = json.encodeToString(value)
                prefs.edit().putString(KEY_EMERGENCY_CONTACTS, contactsJson).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    var hasCompletedSetup: Boolean
        get() = prefs.getBoolean("has_completed_setup", false)
        set(value) = prefs.edit().putBoolean("has_completed_setup", value).apply()

    var language: String
        get() = prefs.getString("language", "en") ?: "en"
        set(value) = prefs.edit().putString("language", value).apply()

    companion object {
        private const val PREFS_NAME = "sos_preferences"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_SETUP_COMPLETED = "setup_completed"
        private const val KEY_TRIGGER_COMBINATION = "trigger_combination"
        private const val KEY_EMERGENCY_CONTACTS = "emergency_contacts"
    }
} 