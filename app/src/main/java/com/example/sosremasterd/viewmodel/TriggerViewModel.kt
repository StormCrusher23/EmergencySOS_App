package com.example.sosremasterd.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TriggerViewModel : ViewModel() {
    private val _recordedKeys = MutableStateFlow<List<Int>>(emptyList())
    val recordedKeys: StateFlow<List<Int>> = _recordedKeys

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    fun updateRecordedKeys(keys: List<Int>) {
        _recordedKeys.value = keys
    }

    fun setRecording(recording: Boolean) {
        _isRecording.value = recording
    }

    fun clearRecordedKeys() {
        _recordedKeys.value = emptyList()
    }
} 