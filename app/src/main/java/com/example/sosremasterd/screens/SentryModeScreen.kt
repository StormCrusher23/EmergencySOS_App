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
        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(2f)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Shield Icon",
                modifier = Modifier.size(100.dp),
                tint = if (isSentryModeActive) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (!isSentryModeActive) {
                        if (!permissionsGranted) {
                            permissionLauncher.launch(permissions)
                        } else if (!isAccessibilityServiceEnabled(context)) {
                            showAccessibilityDialog = true
                        } else {
                            startSentryMode(context, true)
                            isSentryModeActive = true
                        }
                    } else {
                        startSentryMode(context, false)
                        isSentryModeActive = false
                    }
                },
                modifier = Modifier
                    .size(200.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSentryModeActive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isSentryModeActive)
                        MaterialTheme.colorScheme.onError
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    text = if (isSentryModeActive) "Stop\nSentry Mode" else "Start\nSentry Mode",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onSetupClick,
                modifier = Modifier.wrapContentSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("SOS Setup")
            }

            FilledTonalButton(
                onClick = onContactSetupClick,
                modifier = Modifier.wrapContentSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Contacts",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Emergency Contacts")
            }
        }
    }

    if (showAccessibilityDialog) {
        AlertDialog(
            onDismissRequest = { showAccessibilityDialog = false },
            title = { Text("Accessibility Service Required") },
            text = { 
                Text(
                    "The SOS service needs accessibility permissions to detect button presses " +
                    "when your device is locked. Please enable 'SOS Emergency Service' in the " +
                    "accessibility settings."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccessibilityDialog = false
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibilityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun startSentryMode(context: Context, start: Boolean) {
    val serviceIntent = Intent(context, SosForegroundService::class.java)
    if (start) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    } else {
        context.stopService(serviceIntent)
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