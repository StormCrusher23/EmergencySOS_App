package com.example.sosremasterd.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TriggerViewModel : ViewModel() {
    private val _recordedKeys = MutableStateFlow<List<Int>>(emptyList())
    val recordedKeys: StateFlow<List<Int>> = _recordedKeys

    private val _isRecording = mutableStateOf(false)
    val isRecording: State<Boolean> = _isRecording

    fun startRecording() {
        _isRecording.value = true
        _recordedKeys.value = emptyList()
    }

    fun stopRecording() {
        _isRecording.value = false
    }

    fun toggleRecording() {
        _isRecording.value = !_isRecording.value
    }

    fun clearRecordedKeys() {
        _recordedKeys.value = emptyList()
    }

    fun updateRecordedKeys(keys: List<Int>) {
        _recordedKeys.value = keys
    }

    fun addKey(keyCode: Int) {
        if (_isRecording.value) {
            val currentKeys = _recordedKeys.value.toMutableList()
            if (!currentKeys.contains(keyCode)) {
                currentKeys.add(keyCode)
                _recordedKeys.value = currentKeys
            }
        }
    }
} 