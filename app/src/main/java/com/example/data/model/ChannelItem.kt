package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "channels",
    indices = [
        Index("playlistId"),
        Index("groupTitle"),
        Index("streamType"),
        Index("isFavorite")
    ]
)
data class ChannelItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: Long = 0,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val groupTitle: String = "Genel",
    val streamType: String = "LIVE", // "LIVE", "MOVIE", "SERIES"
    val tvgId: String? = null,
    val tvgName: String? = null,
    val currentProgram: String? = null,
    val programTime: String? = null,
    val quality: String = "1080p", // "1080p", "4K", "HDR", "720p"
    val isFavorite: Boolean = false,
    val lastPlayedTimestamp: Long = 0,
    val posterUrl: String? = null,
    val playbackPositionMs: Long = 0L,
    val durationMs: Long = 0L
)

