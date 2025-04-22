package com.example.sosremasterd.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.view.KeyEvent
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import com.example.sosremasterd.R
import com.example.sosremasterd.viewmodel.TriggerViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TriggerSelectionScreen(
    viewModel: TriggerViewModel,
    onConfirm: (List<Int>) -> Unit,
    onCancel: () -> Unit
) {
    val recordedKeys by viewModel.recordedKeys.collectAsState()
    val isRecording = viewModel.isRecording.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.set_trigger_combination),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = if (isRecording) {
                stringResource(R.string.press_buttons_prompt)
            } else {
                stringResource(R.string.start_recording_prompt)
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(recordedKeys) { keyCode ->
                KeyButton(keyCode = keyCode)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (!isRecording) {
                    viewModel.clearRecordedKeys()
                }
                viewModel.toggleRecording()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isRecording) {
                    stringResource(R.string.stop_recording)
                } else {
                    stringResource(R.string.start_recording)
                }
            )
        }

        if (recordedKeys.isNotEmpty() && !isRecording) {
            Button(
                onClick = { onConfirm(recordedKeys) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.confirm_combination))
            }
        }

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.cancel))
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
                    KeyEvent.KEYCODE_POWER -> Icons.Default.Star
                    else -> Icons.Default.AccountBox
                },
                contentDescription = null
            )
            Text(
                text = when (keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP -> stringResource(R.string.volume_up)
                    KeyEvent.KEYCODE_VOLUME_DOWN -> stringResource(R.string.volume_down)
                    KeyEvent.KEYCODE_POWER -> stringResource(R.string.power_button)
                    else -> stringResource(R.string.unknown_button)
                }
            )
        }
    }
} 