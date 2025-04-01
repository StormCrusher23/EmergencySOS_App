package com.example.sosremasterd.data

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyContact(
    val name: String,
    val phoneNumber: String,
    val notifyByMessage: Boolean = true,
    val notifyByCall: Boolean = false
) 