package com.storm.sosremasterd.screens

import android.view.KeyEvent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.storm.sosremasterd.R
import com.storm.sosremasterd.utils.PreferencesManager
import com.storm.sosremasterd.viewmodel.TriggerViewModel

@Composable
fun SetupScreen(
    triggerViewModel: TriggerViewModel,
    preferencesManager: PreferencesManager,
    onFinish: () -> Unit
) {
    var showTriggerSelection by remember { mutableStateOf(false) }
    val recordedKeys by triggerViewModel.recordedKeys.collectAsState(initial = emptyList())
    val isRecording by triggerViewModel.isRecording

    LaunchedEffect(Unit) {
        if (recordedKeys.isEmpty()) {
            triggerViewModel.updateRecordedKeys(preferencesManager.triggerCombination)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.setup_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = stringResource(R.string.setup_description),
            style = MaterialTheme.typography.bodyLarge
        )

        if (recordedKeys.isNotEmpty()) {
            Text(
                text = stringResource(R.string.current_combination),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                items(recordedKeys) { keyCode ->
                    KeyButton(keyCode = keyCode)
                }
            }
        }

        Button(
            onClick = { triggerViewModel.startRecording() },
            enabled = !isRecording,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(stringResource(if (isRecording) R.string.stop_recording else R.string.record_combination))
        }

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth(),
            enabled = recordedKeys.isNotEmpty()
        ) {
            Text(stringResource(R.string.finish_setup))
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