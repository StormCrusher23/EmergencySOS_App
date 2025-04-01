package com.example.sosremasterd.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sosremasterd.navigation.Screen
import android.view.KeyEvent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sosremasterd.viewmodel.TriggerViewModel
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow
import com.example.sosremasterd.utils.PreferencesManager

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "SOS Logo",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Emergency SOS",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your trusted emergency response app for instant alerts and safety",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun HowToUseScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How to Use",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        InstructionStep(
            icon = Icons.Default.Build,
            title = "Set Your Trigger",
            description = "Customize how you activate the SOS alert by selecting a combination of volume and power buttons."
        )

        InstructionStep(
            icon = Icons.Default.Notifications,
            title = "Trigger Activation",
            description = "When triggered, the app will automatically start recording, enable GPS, and send alerts to your emergency contacts."
        )

        InstructionStep(
            icon = Icons.Default.Clear,
            title = "Cancel Option",
            description = "You can cancel an SOS alert within 5 seconds if triggered by mistake."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun InstructionStep(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LegalScreen(onAgree: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Legal Statements",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = "Privacy and Data Usage: Your data is only recorded and shared during active SOS situations. We prioritize your privacy and do not collect personal information outside of emergency events.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(
            text = "No Data Theft Assurance: All recorded data, including video, audio, and location, is encrypted and securely stored, accessible only by authorized personnel during emergencies.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(
            text = "Data Security Measures: Data stored on our servers is protected using industry-standard encryption protocols.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(
            text = "Third-Party Sharing Policy: Data may be shared with designated authorities or emergency contacts you provide to ensure rapid assistance during an SOS activation.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAgree,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Agree and Continue")
        }
    }
}

@Composable
fun SetupScreen(
    triggerViewModel: TriggerViewModel,
    preferencesManager: PreferencesManager,
    onFinish: () -> Unit
) {
    var showTriggerSelection by remember { mutableStateOf(false) }
    val recordedKeys by triggerViewModel.recordedKeys.collectAsState(initial = emptyList())

    // Load saved trigger when entering the screen
    LaunchedEffect(Unit) {
        if (recordedKeys.isEmpty()) {
            triggerViewModel.updateRecordedKeys(preferencesManager.triggerCombination)
        }
    }

    if (showTriggerSelection) {
        TriggerSelectionScreen(
            viewModel = triggerViewModel,
            onConfirm = { combination ->
                preferencesManager.triggerCombination = combination
                showTriggerSelection = false
            },
            onCancel = {
                // Restore the previous combination when canceling
                triggerViewModel.updateRecordedKeys(preferencesManager.triggerCombination)
                showTriggerSelection = false
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Setup Your SOS Trigger",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (recordedKeys.isNotEmpty()) {
                Text(
                    text = "Selected combination:",
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
                onClick = { showTriggerSelection = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(if (recordedKeys.isEmpty()) "Select Trigger Combination" else "Change Combination")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = recordedKeys.isNotEmpty()
            ) {
                Text("Finish Setup")
            }
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
                    KeyEvent.KEYCODE_VOLUME_UP -> "Vol Up"
                    KeyEvent.KEYCODE_VOLUME_DOWN -> "Vol Down"
                    KeyEvent.KEYCODE_POWER -> "Power"
                    else -> "Unknown"
                }
            )
        }
    }
} 