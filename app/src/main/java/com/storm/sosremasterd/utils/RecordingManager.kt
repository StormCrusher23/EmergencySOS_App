package com.storm.sosremasterd.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class RecordingManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    
    private fun getOutputDirectory(): File {
        val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let {
            File(it, "SOS_Recordings").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }

    fun startAudioRecording() {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
            currentRecordingFile = File(getOutputDirectory(), "SOS_AUDIO_$timestamp.mp3")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(currentRecordingFile?.absolutePath)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                prepare()
                start()
            }
            Log.d("RecordingManager", "Started recording audio to ${currentRecordingFile?.absolutePath}")
        } catch (e: Exception) {
            Log.e("RecordingManager", "Error starting audio recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            Log.d("RecordingManager", "Stopped recording audio")
        } catch (e: Exception) {
            Log.e("RecordingManager", "Error stopping recording", e)
        } finally {
            mediaRecorder = null
        }
    }

    fun getRecordingFile(): File? = currentRecordingFile
} 