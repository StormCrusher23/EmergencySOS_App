package com.example.sosremasterd.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.sosremasterd.service.SosForegroundService
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import androidx.compose.material.icons.filled.Call
import androidx.core.content.ContextCompat
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityServiceInfo
import androidx.compose.ui.res.stringResource
import com.example.sosremasterd.R

private var permissionsGranted = false

@Composable
fun SentryModeScreen(
    context: Context,
    onSetupClick: () -> Unit,
    onContactSetupClick: () -> Unit
) {
    var isSentryModeActive by remember { mutableStateOf(false) }
    var showAccessibilityDialog by remember { mutableStateOf(false) }

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.SEND_SMS
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        if (allGranted) {
            if (isAccessibilityServiceEnabled(context)) {
                startSentryMode(context, true)
                isSentryModeActive = true
            } else {
                showAccessibilityDialog = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.sentry_mode),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Button(
            onClick = {
                if (!isSentryModeActive) {
                    if (permissions.all { permission ->
                        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                    }) {
                        if (!isAccessibilityServiceEnabled(context)) {
                            showAccessibilityDialog = true
                        } else {
                            startSentryMode(context, true)
                            isSentryModeActive = true
                        }
                    } else {
                        permissionLauncher.launch(permissions)
                    }
                } else {
                    startSentryMode(context, false)
                    isSentryModeActive = false
                }
            },
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp)
        ) {
            Text(
                text = if (isSentryModeActive) 
                    stringResource(R.string.stop_sentry)
                else 
                    stringResource(R.string.start_sentry),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onSetupClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.setup_trigger)
                )
            }
            IconButton(onClick = onContactSetupClick) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = stringResource(R.string.setup_contacts)
                )
            }
        }
    }

    if (showAccessibilityDialog) {
        AlertDialog(
            onDismissRequest = { showAccessibilityDialog = false },
            title = { Text(stringResource(R.string.accessibility_dialog_title)) },
            text = { Text(stringResource(R.string.accessibility_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        showAccessibilityDialog = false
                    }
                ) {
                    Text(stringResource(R.string.open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibilityDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private fun startSentryMode(context: Context, start: Boolean) {
    val intent = Intent(context, SosForegroundService::class.java)
    if (start) {
        context.startService(intent)
    } else {
        context.stopService(intent)
    }
}

// Update the accessibility check function to cache the result
private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    return enabledServices.any { 
        it.resolveInfo.serviceInfo.packageName == context.packageName &&
        it.resolveInfo.serviceInfo.name.contains("SosAccessibilityService", ignoreCase = true)
    }
} 