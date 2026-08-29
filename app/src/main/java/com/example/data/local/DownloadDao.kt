package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY completedAt DESC")
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE channelId = :channelId LIMIT 1")
    fun getDownloadByChannelId(channelId: Long): Flow<DownloadItem?>

    @Query("SELECT * FROM downloads WHERE channelId = :channelId LIMIT 1")
    suspend fun getDownloadByChannelIdOnce(channelId: Long): DownloadItem?

    @Query("SELECT * FROM downloads WHERE downloadManagerId = :dmId LIMIT 1")
    suspend fun getDownloadByDownloadManagerId(dmId: Long): DownloadItem?

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: Long): DownloadItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadItem): Long

    @Update
    suspend fun updateDownload(item: DownloadItem)

    @Delete
    suspend fun deleteDownload(item: DownloadItem)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: Long)

    @Query("UPDATE downloads SET progressPercent = :progress, bytesDownloaded = :downloadedBytes, totalBytes = :totalBytes, status = :status WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Int, downloadedBytes: Long, totalBytes: Long, status: DownloadStatus)

    @Query("UPDATE downloads SET status = :status, localFilePath = :localPath, totalBytes = :totalBytes, completedAt = :completedAt, progressPercent = 100 WHERE id = :id")
    suspend fun markCompleted(id: Long, status: DownloadStatus = DownloadStatus.COMPLETED, localPath: String, totalBytes: Long, completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE downloads SET status = :status, errorMessage = :error WHERE id = :id")
    suspend fun markFailed(id: Long, status: DownloadStatus = DownloadStatus.FAILED, error: String?)

    @Query("DELETE FROM downloads WHERE status = :status")
    suspend fun deleteDownloadsByStatus(status: DownloadStatus)
}
