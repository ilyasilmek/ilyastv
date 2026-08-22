package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChannelItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY id ASC")
    fun getAllChannels(): Flow<List<ChannelItem>>

    @Query("SELECT * FROM channels WHERE streamType = :streamType ORDER BY id ASC")
    fun getChannelsByStreamType(streamType: String): Flow<List<ChannelItem>>

    @Query("SELECT DISTINCT groupTitle FROM channels WHERE streamType = :streamType AND groupTitle IS NOT NULL AND groupTitle != '' ORDER BY groupTitle ASC")
    fun getCategoriesByStreamType(streamType: String): Flow<List<String>>

    @Query("SELECT * FROM channels WHERE streamType = :streamType AND groupTitle = :category ORDER BY id ASC")
    fun getChannelsByStreamTypeAndCategory(streamType: String, category: String): Flow<List<ChannelItem>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY id ASC")
    fun getChannelsByPlaylist(playlistId: Long): Flow<List<ChannelItem>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteChannels(): Flow<List<ChannelItem>>

    @Query("SELECT * FROM channels WHERE streamType = :streamType AND isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteChannelsByStreamType(streamType: String): Flow<List<ChannelItem>>

    @Query("SELECT * FROM channels WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentlyPlayedChannels(limit: Int = 10): Flow<List<ChannelItem>>

    @Query("SELECT DISTINCT groupTitle FROM channels WHERE groupTitle IS NOT NULL AND groupTitle != '' ORDER BY groupTitle ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM channels WHERE groupTitle = :category ORDER BY id ASC")
    fun getChannelsByCategory(category: String): Flow<List<ChannelItem>>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' OR groupTitle LIKE '%' || :query || '%'")
    fun searchChannels(query: String): Flow<List<ChannelItem>>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getChannelById(id: Long): ChannelItem?

    @Query("SELECT COUNT(*) FROM channels")
    suspend fun getChannelCount(): Int

    @Query("SELECT COUNT(*) FROM channels WHERE streamType = :streamType")
    suspend fun getChannelCountByType(streamType: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelItem>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelItem): Long

    @Update
    suspend fun updateChannel(channel: ChannelItem)

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :channelId")
    suspend fun updateFavorite(channelId: Long, isFavorite: Boolean)

    @Query("UPDATE channels SET lastPlayedTimestamp = :timestamp WHERE id = :channelId")
    suspend fun updateLastPlayed(channelId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylist(playlistId: Long)

    @Query("DELETE FROM channels")
    suspend fun clearAllChannels()
}

