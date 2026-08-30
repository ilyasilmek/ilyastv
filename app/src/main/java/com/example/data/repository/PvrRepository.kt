package com.example.data.repository

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.data.local.DownloadDao
import com.example.data.model.ChannelItem
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class PvrRecordingState(
    val isRecording: Boolean = false,
    val channel: ChannelItem? = null,
    val elapsedSeconds: Int = 0,
    val bytesRecorded: Long = 0L,
    val filePath: String? = null
) {
    val elapsedSecondsFormatted: String get() = formatDuration()
    val recordedBytesFormatted: String get() = formatSize()

    fun formatDuration(): String {
        val hrs = elapsedSeconds / 3600
        val mins = (elapsedSeconds % 3600) / 60
        val secs = elapsedSeconds % 60
        return if (hrs > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
        }
    }

    fun formatSize(): String {
        if (bytesRecorded <= 0) return "0 MB"
        val mb = bytesRecorded / (1024.0 * 1024.0)
        return if (mb >= 1024.0) {
            String.format(Locale.getDefault(), "%.2f GB", mb / 1024.0)
        } else {
            String.format(Locale.getDefault(), "%.1f MB", mb)
        }
    }
}

class PvrRepository(
    private val context: Context,
    private val downloadDao: DownloadDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var recordingJob: Job? = null
    private var timerJob: Job? = null

    private val _recordingState = MutableStateFlow(PvrRecordingState())
    val recordingState: StateFlow<PvrRecordingState> = _recordingState.asStateFlow()

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // Infinite read timeout for live streams
        .followRedirects(true)
        .build()

    fun startRecording(channel: ChannelItem): Boolean {
        if (_recordingState.value.isRecording) {
            return false // Already recording
        }

        val safeChannelName = channel.name.replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_").take(30)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "PVR_${safeChannelName}_$timestamp.ts"

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
        if (!dir.exists()) dir.mkdirs()
        val targetFile = File(dir, fileName)

        _recordingState.value = PvrRecordingState(
            isRecording = true,
            channel = channel,
            elapsedSeconds = 0,
            bytesRecorded = 0L,
            filePath = targetFile.absolutePath
        )

        // Timer job
        timerJob = scope.launch {
            while (isActive && _recordingState.value.isRecording) {
                delay(1000)
                _recordingState.value = _recordingState.value.copy(
                    elapsedSeconds = _recordingState.value.elapsedSeconds + 1
                )
            }
        }

        // Live stream stream recording job
        recordingJob = scope.launch {
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                val request = Request.Builder()
                    .url(channel.streamUrl)
                    .addHeader("User-Agent", "VLC/3.0.18 LibVLC/3.0.18 (Android 14; Mobile)")
                    .addHeader("Accept", "*/*")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful && response.body != null) {
                    inputStream = response.body!!.byteStream()
                    outputStream = FileOutputStream(targetFile)

                    val buffer = ByteArray(64 * 1024)
                    var bytesRead = 0
                    var total = 0L

                    while (isActive) {
                        bytesRead = inputStream.read(buffer)
                        if (bytesRead == -1) break
                        outputStream.write(buffer, 0, bytesRead)
                        total += bytesRead
                        _recordingState.value = _recordingState.value.copy(bytesRecorded = total)
                    }
                    outputStream.flush()
                }
            } catch (e: Exception) {
                Log.e("PvrRepository", "Recording stream stopped: ${e.message}")
            } finally {
                try { outputStream?.close() } catch (_: Exception) {}
                try { inputStream?.close() } catch (_: Exception) {}
            }
        }

        return true
    }

    suspend fun stopRecording(): DownloadItem? = withContext(Dispatchers.IO) {
        val state = _recordingState.value
        if (!state.isRecording || state.channel == null || state.filePath == null) {
            return@withContext null
        }

        timerJob?.cancel()
        recordingJob?.cancel()

        val file = File(state.filePath)
        val finalSize = if (file.exists()) file.length() else 0L

        val channel = state.channel
        val recordingTitle = "🔴 Canlı Kayıt: ${channel.name} (${state.formatDuration()})"

        val downloadItem = DownloadItem(
            id = 0L,
            channelId = channel.id,
            title = recordingTitle,
            streamUrl = "file://${file.absolutePath}",
            downloadManagerId = -1L,
            localFilePath = file.absolutePath,
            posterUrl = channel.logoUrl,
            groupTitle = "PVR Canlı Kayıtlar",
            streamType = "PVR",
            status = DownloadStatus.COMPLETED,
            progressPercent = 100,
            bytesDownloaded = finalSize,
            totalBytes = finalSize,
            completedAt = System.currentTimeMillis()
        )

        val insertedId = downloadDao.insertDownload(downloadItem)
        val savedItem = downloadItem.copy(id = insertedId)

        _recordingState.value = PvrRecordingState(isRecording = false)
        savedItem
    }

    fun cancelRecording() {
        val state = _recordingState.value
        timerJob?.cancel()
        recordingJob?.cancel()

        if (state.filePath != null) {
            val file = File(state.filePath)
            if (file.exists()) file.delete()
        }

        _recordingState.value = PvrRecordingState(isRecording = false)
    }
}
