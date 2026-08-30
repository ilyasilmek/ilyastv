package com.example.data.repository

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.util.Log
import com.example.data.local.DownloadDao
import com.example.data.model.ChannelItem
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DownloadRepository(
    private val context: Context,
    private val downloadDao: DownloadDao
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeDownloadJobs = ConcurrentHashMap<Long, Job>()

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()

    val allDownloads: Flow<List<DownloadItem>> = downloadDao.getAllDownloads()
    val completedDownloads: Flow<List<DownloadItem>> = downloadDao.getDownloadsByStatus(DownloadStatus.COMPLETED)

    fun getDownloadForChannel(channelId: Long): Flow<DownloadItem?> {
        return downloadDao.getDownloadByChannelId(channelId)
    }

    suspend fun isChannelDownloaded(channelId: Long): Boolean = withContext(Dispatchers.IO) {
        val item = downloadDao.getDownloadByChannelIdOnce(channelId)
        item != null && item.status == DownloadStatus.COMPLETED && !item.localFilePath.isNullOrBlank() && File(item.localFilePath).exists()
    }

    suspend fun startDownload(channel: ChannelItem): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // Check if already completed and file exists
            val existing = downloadDao.getDownloadByChannelIdOnce(channel.id)
            if (existing != null && existing.status == DownloadStatus.COMPLETED && !existing.localFilePath.isNullOrBlank()) {
                val file = File(existing.localFilePath)
                if (file.exists() && file.length() > 0) {
                    return@withContext Result.success(existing.id)
                }
            }

            // Clean title for safe filesystem filename
            val sanitizedTitle = channel.name
                .replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_")
                .take(40)
            val extension = when {
                channel.streamUrl.contains(".mp4", ignoreCase = true) -> "mp4"
                channel.streamUrl.contains(".mkv", ignoreCase = true) -> "mkv"
                channel.streamUrl.contains(".ts", ignoreCase = true) -> "ts"
                channel.streamUrl.contains(".m3u8", ignoreCase = true) -> "ts"
                else -> "mp4"
            }
            val fileName = "IlyasTV_${sanitizedTitle}_${channel.id}.$extension"

            // Target storage directory (App external movies directory - accessible without runtime permission)
            val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
            if (!moviesDir.exists()) {
                moviesDir.mkdirs()
            }
            val targetFile = File(moviesDir, fileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }

            val downloadItem = DownloadItem(
                id = existing?.id ?: 0L,
                channelId = channel.id,
                title = channel.name,
                streamUrl = channel.streamUrl,
                downloadManagerId = -1L,
                localFilePath = targetFile.absolutePath,
                posterUrl = channel.posterUrl ?: channel.logoUrl,
                groupTitle = channel.groupTitle,
                streamType = channel.streamType,
                status = DownloadStatus.DOWNLOADING,
                progressPercent = 0,
                bytesDownloaded = 0L,
                totalBytes = 0L,
                errorMessage = null,
                createdAt = System.currentTimeMillis()
            )

            val rowId = downloadDao.insertDownload(downloadItem)
            val finalId = if (downloadItem.id > 0) downloadItem.id else rowId

            // Cancel any previous running job for this ID
            activeDownloadJobs[finalId]?.cancel()

            // Launch background download task
            val job = repositoryScope.launch {
                executeDownloadTask(finalId, channel.streamUrl, targetFile)
            }
            activeDownloadJobs[finalId] = job

            Result.success(finalId)
        } catch (e: Exception) {
            Log.e("DownloadRepository", "Download start error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun executeDownloadTask(downloadId: Long, url: String, targetFile: File) {
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
        if (tempFile.exists()) {
            tempFile.delete()
        }

        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "VLC/3.0.18 LibVLC/3.0.18 (Android 14; Mobile)")
                .addHeader("Accept", "*/*")
                .addHeader("Connection", "keep-alive")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorMsg = when (response.code) {
                    403 -> "Sunucu erişimi reddetti (HTTP 403 - Yetkisiz)."
                    404 -> "Yayın dosyası sunucuda bulunamadı (HTTP 404)."
                    500, 502, 503 -> "IPTV sunucu hatası (HTTP ${response.code})."
                    else -> "İndirme başlatılamadı (HTTP ${response.code})."
                }
                downloadDao.markFailed(downloadId, error = errorMsg)
                response.close()
                return
            }

            val body = response.body
            if (body == null) {
                downloadDao.markFailed(downloadId, error = "Sunucudan veri akışı alınamadı.")
                return
            }

            val totalBytes = body.contentLength()
            inputStream = body.byteStream()
            outputStream = FileOutputStream(tempFile)

            val buffer = ByteArray(64 * 1024) // 64 KB buffer for high throughput
            var bytesRead: Int
            var totalDownloaded = 0L
            var lastUpdateBytes = 0L
            var lastUpdateTime = System.currentTimeMillis()

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalDownloaded += bytesRead

                val currentTime = System.currentTimeMillis()
                // Update progress every 500KB or every 1000ms
                if (totalDownloaded - lastUpdateBytes >= 500 * 1024 || currentTime - lastUpdateTime >= 1000) {
                    val progress = if (totalBytes > 0) {
                        ((totalDownloaded * 100) / totalBytes).toInt().coerceIn(0, 99)
                    } else {
                        0
                    }
                    downloadDao.updateProgress(
                        id = downloadId,
                        progress = progress,
                        downloadedBytes = totalDownloaded,
                        totalBytes = totalBytes,
                        status = DownloadStatus.DOWNLOADING
                    )
                    lastUpdateBytes = totalDownloaded
                    lastUpdateTime = currentTime
                }
            }

            outputStream.flush()
            outputStream.close()
            outputStream = null
            inputStream.close()
            inputStream = null

            // Rename .tmp to final target file
            if (tempFile.exists()) {
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                val renamed = tempFile.renameTo(targetFile)
                val finalFile = if (renamed) targetFile else tempFile
                val finalSize = finalFile.length()

                downloadDao.markCompleted(
                    id = downloadId,
                    localPath = finalFile.absolutePath,
                    totalBytes = finalSize,
                    completedAt = System.currentTimeMillis()
                )
            } else {
                downloadDao.markFailed(downloadId, error = "Dosya kaydedilemedi.")
            }

        } catch (e: Exception) {
            Log.e("DownloadRepository", "Download task failed: ${e.message}", e)
            if (tempFile.exists()) {
                tempFile.delete()
            }
            if (e is kotlinx.coroutines.CancellationException) {
                downloadDao.deleteDownloadById(downloadId)
            } else {
                val readableMessage = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> "Bağlantı zaman aşımına uğradı."
                    e.message?.contains("ENOSPC", ignoreCase = true) == true -> "Cihazda yeterli hafıza alanı yok."
                    e.message?.contains("ECONNRESET", ignoreCase = true) == true -> "Sunucu bağlantıyı kesti."
                    else -> "İndirme hatası: ${e.localizedMessage ?: "Bilinmeyen hata"}"
                }
                downloadDao.markFailed(downloadId, error = readableMessage)
            }
        } finally {
            try { outputStream?.close() } catch (_: Exception) {}
            try { inputStream?.close() } catch (_: Exception) {}
            activeDownloadJobs.remove(downloadId)
        }
    }

    suspend fun deleteDownload(item: DownloadItem) = withContext(Dispatchers.IO) {
        try {
            // Cancel running job if any
            activeDownloadJobs[item.id]?.cancel()
            activeDownloadJobs.remove(item.id)

            // Remove file from disk
            if (!item.localFilePath.isNullOrBlank()) {
                val file = File(item.localFilePath)
                if (file.exists()) {
                    file.delete()
                }
                // Also clean up any .tmp
                val tmpFile = File("${item.localFilePath}.tmp")
                if (tmpFile.exists()) {
                    tmpFile.delete()
                }
            }

            // Remove record from database
            downloadDao.deleteDownload(item)
        } catch (e: Exception) {
            Log.e("DownloadRepository", "Error deleting download: ${e.message}")
        }
    }

    suspend fun cancelDownload(item: DownloadItem) = withContext(Dispatchers.IO) {
        deleteDownload(item)
    }

    fun getStorageStats(): StorageStats {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
            val stat = StatFs(dir.path)
            val availableBytes = stat.availableBytes
            val totalBytes = stat.totalBytes
            
            // Calculate app downloaded files size
            val appDownloadsSize = dir.listFiles()?.filter { !it.name.endsWith(".tmp") }?.sumOf { it.length() } ?: 0L
            
            StorageStats(
                freeBytes = availableBytes,
                totalBytes = totalBytes,
                appDownloadsBytes = appDownloadsSize
            )
        } catch (e: Exception) {
            StorageStats(0L, 0L, 0L)
        }
    }
}

data class StorageStats(
    val freeBytes: Long,
    val totalBytes: Long,
    val appDownloadsBytes: Long
) {
    fun formatAppSize(): String = formatBytes(appDownloadsBytes)
    fun formatFreeSize(): String = formatBytes(freeBytes)
    fun formatTotalSize(): String = formatBytes(totalBytes)

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1024.0) {
            String.format("%.1f GB", mb / 1024.0)
        } else {
            String.format("%.1f MB", mb)
        }
    }
}
