package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val urlOrPath: String,
    val isLocalFile: Boolean = false,
    val channelCount: Int = 0,
    val liveCount: Int = 0,
    val movieCount: Int = 0,
    val seriesCount: Int = 0,
    val username: String? = null,
    val serverHost: String? = null,
    val status: String? = null, // "Aktif", "Pasif", "Süresi Doldu", "Bilinmiyor"
    val expDateTimestamp: Long? = null, // Unix timestamp in milliseconds
    val maxConnections: String? = null,
    val activeConnections: String? = null,
    val isTrial: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

