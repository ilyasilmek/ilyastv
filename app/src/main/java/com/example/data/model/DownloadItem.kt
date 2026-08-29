package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Entity(
    tableName = "downloads",
    indices = [
        Index("channelId"),
        Index("status"),
        Index("streamType"),
        Index("downloadManagerId")
    ]
)
data class DownloadItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val channelId: Long = 0,
    val title: String,
    val streamUrl: String,
    val downloadManagerId: Long = -1L,
    val localFilePath: String? = null,
    val posterUrl: String? = null,
    val groupTitle: String = "Genel",
    val streamType: String = "MOVIE", // "MOVIE", "SERIES", "VOD"
    val status: DownloadStatus = DownloadStatus.PENDING,
    val progressPercent: Int = 0,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L
) {
    fun toChannelItem(): ChannelItem {
        return ChannelItem(
            id = channelId,
            name = title,
            streamUrl = localFilePath ?: streamUrl,
            posterUrl = posterUrl,
            groupTitle = groupTitle,
            streamType = streamType,
            quality = "İndirilen Dosya",
            isFavorite = false
        )
    }
}
