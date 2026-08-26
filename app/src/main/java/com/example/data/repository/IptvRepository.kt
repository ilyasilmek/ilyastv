package com.example.data.repository

import com.example.data.local.ChannelDao
import com.example.data.local.PlaylistDao
import com.example.data.model.AccountInfo
import com.example.data.model.ChannelItem
import com.example.data.model.PlaylistItem
import com.example.data.parser.M3uParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.InputStream

class IptvRepository(
    private val playlistDao: PlaylistDao,
    private val channelDao: ChannelDao
) {
    val allChannels: Flow<List<ChannelItem>> = channelDao.getAllChannels()
    val favoriteChannels: Flow<List<ChannelItem>> = channelDao.getFavoriteChannels()
    val recentChannels: Flow<List<ChannelItem>> = channelDao.getRecentlyPlayedChannels()
    val continueWatching: Flow<List<ChannelItem>> = channelDao.getContinueWatchingChannels()
    val watchHistory: Flow<List<ChannelItem>> = channelDao.getWatchHistory()
    val allCategories: Flow<List<String>> = channelDao.getAllCategories()
    val allPlaylists: Flow<List<PlaylistItem>> = playlistDao.getAllPlaylists()

    // Expose current account info from the latest playlist
    val latestAccountInfo: Flow<AccountInfo?> = playlistDao.getAllPlaylists().map { playlists ->
        val latest = playlists.firstOrNull() ?: return@map null
        AccountInfo(
            username = latest.username ?: (if (latest.isLocalFile) "Yerel M3U Dosyası" else "IPTV Hesabı"),
            serverHost = latest.serverHost ?: latest.name,
            status = latest.status ?: "Aktif",
            expDateTimestamp = latest.expDateTimestamp,
            maxConnections = latest.maxConnections ?: "1",
            activeConnections = latest.activeConnections ?: "1",
            isTrial = latest.isTrial
        )
    }

    // No demo data inserted by default - app starts clean & empty
    suspend fun ensureDefaultDataLoaded() = withContext(Dispatchers.IO) {
        // App starts with empty screen as requested by the user
    }

    suspend fun importPlaylistFromUrl(name: String, url: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val parseResult = M3uParser.parseFromUrl(url, 0)
            val channels = parseResult.channels
            if (channels.isEmpty()) {
                return@withContext Result.failure(Exception("M3U linkinde geçerli kanal bulunamadı."))
            }

            val liveCount = channels.count { it.streamType == "LIVE" }
            val movieCount = channels.count { it.streamType == "MOVIE" }
            val seriesCount = channels.count { it.streamType == "SERIES" }
            val acc = parseResult.accountInfo

            val playlist = PlaylistItem(
                name = name.ifBlank { acc.username?.let { "$it IPTV Listesi" } ?: "IPTV Kanal Listesi" },
                urlOrPath = url,
                isLocalFile = false,
                channelCount = channels.size,
                liveCount = liveCount,
                movieCount = movieCount,
                seriesCount = seriesCount,
                username = acc.username,
                serverHost = acc.serverHost,
                status = acc.status,
                expDateTimestamp = acc.expDateTimestamp,
                maxConnections = acc.maxConnections,
                activeConnections = acc.activeConnections,
                isTrial = acc.isTrial
            )
            val playlistId = playlistDao.insertPlaylist(playlist)
            val channelsWithPlaylist = channels.map { it.copy(playlistId = playlistId) }
            channelDao.insertChannels(channelsWithPlaylist)

            Result.success(channels.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importPlaylistFromContent(name: String, content: String, isFile: Boolean = true): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val parseResult = M3uParser.parseFromString(content, 0)
            val channels = parseResult.channels
            if (channels.isEmpty()) {
                return@withContext Result.failure(Exception("Dosya içeriğinde geçerli kanal bulunamadı."))
            }

            val liveCount = channels.count { it.streamType == "LIVE" }
            val movieCount = channels.count { it.streamType == "MOVIE" }
            val seriesCount = channels.count { it.streamType == "SERIES" }
            val acc = parseResult.accountInfo

            val playlist = PlaylistItem(
                name = name.ifBlank { if (isFile) "Yüklenen M3U Dosyası" else "Yapıştırılan M3U" },
                urlOrPath = if (isFile) "Yerel Dosya" else "Metin İçeriği",
                isLocalFile = isFile,
                channelCount = channels.size,
                liveCount = liveCount,
                movieCount = movieCount,
                seriesCount = seriesCount,
                username = acc.username,
                serverHost = acc.serverHost,
                status = acc.status,
                expDateTimestamp = acc.expDateTimestamp,
                maxConnections = acc.maxConnections,
                activeConnections = acc.activeConnections,
                isTrial = acc.isTrial
            )
            val playlistId = playlistDao.insertPlaylist(playlist)
            val channelsWithPlaylist = channels.map { it.copy(playlistId = playlistId) }
            channelDao.insertChannels(channelsWithPlaylist)

            Result.success(channels.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importPlaylistFromStream(name: String, inputStream: InputStream): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val parseResult = M3uParser.parseFromInputStream(inputStream, 0)
            val channels = parseResult.channels
            if (channels.isEmpty()) {
                return@withContext Result.failure(Exception("M3U dosyasında geçerli kanal bulunamadı."))
            }

            val liveCount = channels.count { it.streamType == "LIVE" }
            val movieCount = channels.count { it.streamType == "MOVIE" }
            val seriesCount = channels.count { it.streamType == "SERIES" }
            val acc = parseResult.accountInfo

            val playlist = PlaylistItem(
                name = name.ifBlank { "Yüklenen M3U Dosyası" },
                urlOrPath = "Yerel Dosya",
                isLocalFile = true,
                channelCount = channels.size,
                liveCount = liveCount,
                movieCount = movieCount,
                seriesCount = seriesCount,
                username = acc.username,
                serverHost = acc.serverHost,
                status = acc.status,
                expDateTimestamp = acc.expDateTimestamp,
                maxConnections = acc.maxConnections,
                activeConnections = acc.activeConnections,
                isTrial = acc.isTrial
            )
            val playlistId = playlistDao.insertPlaylist(playlist)
            val channelsWithPlaylist = channels.map { it.copy(playlistId = playlistId) }
            channelDao.insertChannels(channelsWithPlaylist)

            Result.success(channels.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getChannelsByStreamType(streamType: String): Flow<List<ChannelItem>> {
        return channelDao.getChannelsByStreamType(streamType)
    }

    fun getCategoriesByStreamType(streamType: String): Flow<List<String>> {
        return channelDao.getCategoriesByStreamType(streamType)
    }

    fun getChannelsByStreamTypeAndCategory(streamType: String, category: String): Flow<List<ChannelItem>> {
        return channelDao.getChannelsByStreamTypeAndCategory(streamType, category)
    }

    fun getFavoriteChannelsByStreamType(streamType: String): Flow<List<ChannelItem>> {
        return channelDao.getFavoriteChannelsByStreamType(streamType)
    }

    fun getChannelsByCategory(category: String): Flow<List<ChannelItem>> {
        return channelDao.getChannelsByCategory(category)
    }

    fun searchChannels(query: String): Flow<List<ChannelItem>> {
        return channelDao.searchChannels(query)
    }

    suspend fun getChannelById(id: Long): ChannelItem? = withContext(Dispatchers.IO) {
        channelDao.getChannelById(id)
    }

    suspend fun toggleFavorite(channel: ChannelItem) = withContext(Dispatchers.IO) {
        val newFav = !channel.isFavorite
        channelDao.updateFavorite(channel.id, newFav)
    }

    suspend fun recordChannelPlayed(channelId: Long) = withContext(Dispatchers.IO) {
        channelDao.updateLastPlayed(channelId)
    }

    suspend fun updatePlaybackProgress(channelId: Long, positionMs: Long, durationMs: Long) = withContext(Dispatchers.IO) {
        channelDao.updatePlaybackProgress(channelId, positionMs, durationMs)
    }

    suspend fun resetPlaybackProgress(channelId: Long) = withContext(Dispatchers.IO) {
        channelDao.resetPlaybackProgress(channelId)
    }

    suspend fun removeFromWatchHistory(channelId: Long) = withContext(Dispatchers.IO) {
        channelDao.removeFromWatchHistory(channelId)
    }

    suspend fun moveWatchHistoryToTop(channelId: Long) = withContext(Dispatchers.IO) {
        channelDao.moveWatchHistoryToTop(channelId)
    }

    suspend fun clearAllFavorites() = withContext(Dispatchers.IO) {
        channelDao.clearAllFavorites()
    }

    suspend fun clearWatchHistory() = withContext(Dispatchers.IO) {
        channelDao.clearWatchHistory()
    }

    suspend fun refreshPlaylist(playlistId: Long): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val playlist = playlistDao.getPlaylistById(playlistId)
                ?: return@withContext Result.failure(Exception("Oynatma listesi bulunamadı."))

            val url = playlist.urlOrPath
            if (playlist.isLocalFile || !url.startsWith("http", ignoreCase = true)) {
                return@withContext Result.failure(Exception("Yerel dosya kaynakları yalnızca yeni dosya seçilerek güncellenebilir."))
            }

            val parseResult = M3uParser.parseFromUrl(url, playlistId)
            val channels = parseResult.channels
            if (channels.isEmpty()) {
                return@withContext Result.failure(Exception("Sunucudan geçerli kanal verisi alınamadı."))
            }

            val liveCount = channels.count { it.streamType == "LIVE" }
            val movieCount = channels.count { it.streamType == "MOVIE" }
            val seriesCount = channels.count { it.streamType == "SERIES" }
            val acc = parseResult.accountInfo

            // Remove old channels for this playlist and insert fresh ones
            channelDao.deleteChannelsByPlaylist(playlistId)
            val channelsWithPlaylist = channels.map { it.copy(playlistId = playlistId) }
            channelDao.insertChannels(channelsWithPlaylist)

            val updatedPlaylist = playlist.copy(
                channelCount = channels.size,
                liveCount = liveCount,
                movieCount = movieCount,
                seriesCount = seriesCount,
                username = acc.username ?: playlist.username,
                serverHost = acc.serverHost ?: playlist.serverHost,
                status = acc.status ?: playlist.status,
                expDateTimestamp = acc.expDateTimestamp ?: playlist.expDateTimestamp,
                maxConnections = acc.maxConnections ?: playlist.maxConnections,
                activeConnections = acc.activeConnections ?: playlist.activeConnections,
                isTrial = acc.isTrial
            )
            playlistDao.updatePlaylist(updatedPlaylist)

            Result.success(channels.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePlaylist(playlistId: Long, newName: String, newUrl: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val playlist = playlistDao.getPlaylistById(playlistId)
                ?: return@withContext Result.failure(Exception("Oynatma listesi bulunamadı."))

            val cleanedUrl = newUrl.trim()
            val cleanedName = newName.trim().ifBlank { playlist.name }

            val urlChanged = cleanedUrl.isNotBlank() && cleanedUrl != playlist.urlOrPath && cleanedUrl.startsWith("http", ignoreCase = true)

            if (urlChanged) {
                val parseResult = M3uParser.parseFromUrl(cleanedUrl, playlistId)
                val channels = parseResult.channels
                if (channels.isEmpty()) {
                    return@withContext Result.failure(Exception("Yeni linkte geçerli kanal bulunamadı."))
                }

                val liveCount = channels.count { it.streamType == "LIVE" }
                val movieCount = channels.count { it.streamType == "MOVIE" }
                val seriesCount = channels.count { it.streamType == "SERIES" }
                val acc = parseResult.accountInfo

                channelDao.deleteChannelsByPlaylist(playlistId)
                val channelsWithPlaylist = channels.map { it.copy(playlistId = playlistId) }
                channelDao.insertChannels(channelsWithPlaylist)

                val updatedPlaylist = playlist.copy(
                    name = cleanedName,
                    urlOrPath = cleanedUrl,
                    isLocalFile = false,
                    channelCount = channels.size,
                    liveCount = liveCount,
                    movieCount = movieCount,
                    seriesCount = seriesCount,
                    username = acc.username ?: playlist.username,
                    serverHost = acc.serverHost ?: playlist.serverHost,
                    status = acc.status ?: playlist.status,
                    expDateTimestamp = acc.expDateTimestamp ?: playlist.expDateTimestamp,
                    maxConnections = acc.maxConnections ?: playlist.maxConnections,
                    activeConnections = acc.activeConnections ?: playlist.activeConnections,
                    isTrial = acc.isTrial
                )
                playlistDao.updatePlaylist(updatedPlaylist)
                Result.success(channels.size)
            } else {
                val updatedPlaylist = playlist.copy(
                    name = cleanedName,
                    urlOrPath = if (cleanedUrl.isNotBlank()) cleanedUrl else playlist.urlOrPath
                )
                playlistDao.updatePlaylist(updatedPlaylist)
                Result.success(playlist.channelCount)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        channelDao.deleteChannelsByPlaylist(playlistId)
        playlistDao.deletePlaylistById(playlistId)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        channelDao.clearAllChannels()
        playlistDao.clearAllPlaylists()
    }
}

