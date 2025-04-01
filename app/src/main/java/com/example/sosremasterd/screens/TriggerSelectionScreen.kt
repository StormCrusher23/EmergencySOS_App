package com.example.sosremasterd.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.view.KeyEvent
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import com.example.sosremasterd.viewmodel.TriggerViewModel

@Composable
fun TriggerSelectionScreen(
    viewModel: TriggerViewModel,
    onConfirm: (List<Int>) -> Unit,
    onCancel: () -> Unit
) {
    val recordedKeys by viewModel.recordedKeys.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Set Trigger Combination",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = if (isRecording) {
                "Press your desired combination of buttons..."
            } else {
                "Press 'Start Recording' to set your trigger combination"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        // Display recorded keys
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(recordedKeys) { keyCode ->
                KeyButton(keyCode = keyCode)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Record button
        Button(
            onClick = {
                if (!isRecording) {
                    viewModel.clearRecordedKeys()
                }
                viewModel.setRecording(!isRecording)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRecording) "Stop Recording" else "Start Recording")
        }

        // Confirm and Cancel buttons
        if (recordedKeys.isNotEmpty() && !isRecording) {
            Button(
                onClick = { onConfirm(recordedKeys) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Combination")
            }
        }

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

@Composable
private fun KeyButton(keyCode: Int) {
    Card(
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = when (keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP -> Icons.Default.KeyboardArrowUp
                    KeyEvent.KEYCODE_VOLUME_DOWN -> Icons.Default.KeyboardArrowDown
                    else -> Icons.Default.AccountBox
                },
                contentDescription = null
            )
            Text(
                text = when (keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP -> "Vol Up"
                    KeyEvent.KEYCODE_VOLUME_DOWN -> "Vol Down"
                    else -> "Unknown"
                }
            )
        }
    }
} 