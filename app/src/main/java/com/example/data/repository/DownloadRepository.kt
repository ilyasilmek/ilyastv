package com.example.data.repository

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.util.Log
import com.example.data.local.DownloadDao
import com.example.data.model.ChannelItem
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class DownloadRepository(
    private val context: Context,
    private val downloadDao: DownloadDao
) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager

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
            if (downloadManager == null) {
                return@withContext Result.failure(Exception("Cihaz İndirme Yöneticisi (DownloadManager) bulunamadı."))
            }

            // Check if already in progress or completed
            val existing = downloadDao.getDownloadByChannelIdOnce(channel.id)
            if (existing != null && existing.status == DownloadStatus.COMPLETED && !existing.localFilePath.isNullOrBlank()) {
                val file = File(existing.localFilePath)
                if (file.exists()) {
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

            // Target storage directory (App external movies directory - requires no special permissions on modern Android)
            val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
            if (!moviesDir.exists()) {
                moviesDir.mkdirs()
            }
            val targetFile = File(moviesDir, fileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }

            val request = DownloadManager.Request(Uri.parse(channel.streamUrl)).apply {
                setTitle(channel.name)
                setDescription("İlyasTV Çevrimdışı İndirme")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                setDestinationUri(Uri.fromFile(targetFile))
            }

            val dmId = downloadManager.enqueue(request)

            val downloadItem = DownloadItem(
                id = existing?.id ?: 0L,
                channelId = channel.id,
                title = channel.name,
                streamUrl = channel.streamUrl,
                downloadManagerId = dmId,
                localFilePath = targetFile.absolutePath,
                posterUrl = channel.posterUrl ?: channel.logoUrl,
                groupTitle = channel.groupTitle,
                streamType = channel.streamType,
                status = DownloadStatus.DOWNLOADING,
                progressPercent = 0,
                bytesDownloaded = 0L,
                totalBytes = 0L,
                createdAt = System.currentTimeMillis()
            )

            val rowId = downloadDao.insertDownload(downloadItem)
            Result.success(rowId)
        } catch (e: Exception) {
            Log.e("DownloadRepository", "Download start error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun syncActiveDownloadsProgress() = withContext(Dispatchers.IO) {
        if (downloadManager == null) return@withContext

        try {
            val query = DownloadManager.Query()
            val cursor: Cursor? = downloadManager.query(query)

            cursor?.use { c ->
                val idCol = c.getColumnIndex(DownloadManager.COLUMN_ID)
                val statusCol = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val bytesDownloadedCol = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val totalBytesCol = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val reasonCol = c.getColumnIndex(DownloadManager.COLUMN_REASON)
                val uriCol = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                while (c.moveToNext()) {
                    val dmId = c.getLong(idCol)
                    val status = c.getInt(statusCol)
                    val downloadedBytes = c.getLong(bytesDownloadedCol)
                    val totalBytes = c.getLong(totalBytesCol)

                    val dbItem = downloadDao.getDownloadByDownloadManagerId(dmId) ?: continue

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val localUriStr = if (uriCol != -1) c.getString(uriCol) else null
                            val finalPath = if (!localUriStr.isNullOrBlank()) {
                                try {
                                    val uri = Uri.parse(localUriStr)
                                    uri.path ?: dbItem.localFilePath ?: ""
                                } catch (_: Exception) {
                                    dbItem.localFilePath ?: ""
                                }
                            } else {
                                dbItem.localFilePath ?: ""
                            }

                            val total = if (totalBytes > 0) totalBytes else (File(finalPath).length().takeIf { it > 0 } ?: downloadedBytes)
                            downloadDao.markCompleted(
                                id = dbItem.id,
                                localPath = finalPath,
                                totalBytes = total,
                                completedAt = System.currentTimeMillis()
                            )
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            val percent = if (totalBytes > 0) {
                                ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 99)
                            } else {
                                0
                            }
                            downloadDao.updateProgress(
                                id = dbItem.id,
                                progress = percent,
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes,
                                status = DownloadStatus.DOWNLOADING
                            )
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            val percent = if (totalBytes > 0) {
                                ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 99)
                            } else 0
                            downloadDao.updateProgress(
                                id = dbItem.id,
                                progress = percent,
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes,
                                status = DownloadStatus.PAUSED
                            )
                        }
                        DownloadManager.STATUS_FAILED -> {
                            val reason = if (reasonCol != -1) c.getInt(reasonCol) else -1
                            downloadDao.markFailed(
                                id = dbItem.id,
                                error = "İndirme başarısız oldu (Hata kodu: $reason)"
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("DownloadRepository", "Error syncing downloads: ${e.message}")
        }
    }

    suspend fun deleteDownload(item: DownloadItem) = withContext(Dispatchers.IO) {
        try {
            // Cancel from download manager if in progress
            if (item.downloadManagerId > 0 && downloadManager != null) {
                try {
                    downloadManager.remove(item.downloadManagerId)
                } catch (_: Exception) {}
            }

            // Remove file from disk
            if (!item.localFilePath.isNullOrBlank()) {
                val file = File(item.localFilePath)
                if (file.exists()) {
                    file.delete()
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

    suspend fun clearCompletedDownloads() = withContext(Dispatchers.IO) {
        val completed = downloadDao.getDownloadsByStatus(DownloadStatus.COMPLETED)
        // Handled through single delete iterations or mass delete
    }

    fun getStorageStats(): StorageStats {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
            val stat = StatFs(dir.path)
            val availableBytes = stat.availableBytes
            val totalBytes = stat.totalBytes
            
            // Calculate app downloaded files size
            val appDownloadsSize = dir.listFiles()?.sumOf { it.length() } ?: 0L
            
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
