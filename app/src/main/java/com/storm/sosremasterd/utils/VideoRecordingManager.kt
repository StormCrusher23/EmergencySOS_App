package com.storm.sosremasterd.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class VideoRecordingManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private var recording: Recording? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecordingFile: File? = null
    private val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    private fun getOutputDirectory(): File {
        val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let {
            File(it, "SOS_Recordings").apply { mkdirs() }
        }
        val outputDir = if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
        Log.d("VideoRecordingManager", "Video output directory: ${outputDir.absolutePath}")
        return outputDir
    }

    suspend fun startVideoRecording() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                Log.e("VideoRecordingManager", "Device running Android version below 11, video recording not supported")
                return
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val cameraProvider = suspendCoroutine { continuation ->
                cameraProviderFuture.addListener({
                    continuation.resume(cameraProviderFuture.get())
                }, mainExecutor)
            }
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // Unbind all previous use cases
            cameraProvider.unbindAll()

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                videoCapture
            )

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
            currentRecordingFile = File(getOutputDirectory(), "SOS_VIDEO_$timestamp.mp4")
            Log.d("VideoRecordingManager", "Creating video file at: ${currentRecordingFile?.absolutePath}")

            val fileOutputOptions = FileOutputOptions.Builder(currentRecordingFile!!)
                .build()

            recording = videoCapture?.output?.prepareRecording(context, fileOutputOptions)
                ?.apply { 
                    withAudioEnabled()
                    Log.d("VideoRecordingManager", "Audio enabled for video recording")
                }
                ?.start(mainExecutor) { event ->
                    when(event) {
                        is VideoRecordEvent.Start -> {
                            Log.d("VideoRecordingManager", "Started recording video to ${currentRecordingFile?.absolutePath}")
                        }
                        is VideoRecordEvent.Finalize -> {
                            if (event.hasError()) {
                                Log.e("VideoRecordingManager", "Video recording failed: ${event.error}")
                            } else {
                                Log.d("VideoRecordingManager", "Video recording saved successfully to: ${currentRecordingFile?.absolutePath}")
                                Log.d("VideoRecordingManager", "Video file size: ${currentRecordingFile?.length() ?: 0} bytes")
                            }
                        }
                    }
                }

        } catch (e: Exception) {
            Log.e("VideoRecordingManager", "Error starting video recording", e)
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        try {
            recording?.stop()
            recording = null
            Log.d("VideoRecordingManager", "Stopped video recording at: ${currentRecordingFile?.absolutePath}")
        } catch (e: Exception) {
            Log.e("VideoRecordingManager", "Error stopping video recording", e)
            e.printStackTrace()
        }
    }

    fun getRecordingFile(): File? = currentRecordingFile
} 