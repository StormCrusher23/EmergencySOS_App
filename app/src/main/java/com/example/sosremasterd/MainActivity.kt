package com.example.sosremasterd

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.sosremasterd.navigation.NavigationGraph
import com.example.sosremasterd.ui.theme.SOSremasterdTheme
import android.content.Intent
import android.content.ServiceConnection
import android.content.ComponentName
import android.os.IBinder
import com.example.sosremasterd.service.SOSService
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sosremasterd.viewmodel.TriggerViewModel

class MainActivity : ComponentActivity() {
    private var sosService: SOSService? = null
    private var triggerViewModel: TriggerViewModel? = null
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SOSService.LocalBinder
            sosService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            sosService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start and bind to the SOS service
        Intent(this, SOSService::class.java).also { intent ->
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }

        setContent {
            triggerViewModel = viewModel()
            
            SOSremasterdTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationGraph(navController = rememberNavController())
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (triggerViewModel?.isRecording?.value == true) {
                    triggerViewModel?.updateRecordedKeys(
                        (triggerViewModel?.recordedKeys?.value ?: emptyList()) + keyCode
                    )
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (triggerViewModel?.isRecording?.value == true) {
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        stopService(Intent(this, SOSService::class.java))
        super.onDestroy()
    }
}
