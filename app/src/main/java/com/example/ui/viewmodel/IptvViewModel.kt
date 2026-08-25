package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AccountInfo
import com.example.data.model.ChannelItem
import com.example.data.model.PlaylistItem
import com.example.data.parser.M3uParser
import com.example.data.repository.IptvRepository
import com.example.ui.theme.AppThemeSetting
import com.example.ui.theme.ViewModeSetting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream

sealed interface ImportState {
    object Idle : ImportState
    object Loading : ImportState
    data class Success(val message: String, val accountInfo: AccountInfo? = null) : ImportState
    data class Error(val message: String) : ImportState
}

enum class BufferOption(val label: String, val description: String) {
    LOW_LATENCY("Düşük Gecikme", "Canlı yayınlar için hızlı başlangıç, düşük arabellek"),
    NORMAL("Dengeli (Önerilen)", "Akıcı yayın için standart arabellek boyutu"),
    MAX_BUFFER("Yüksek Arabellek", "Dalgalı internet bağlantıları için önceden yükleme")
}

class IptvViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IptvRepository
    private val prefs = application.getSharedPreferences("ilyastv_prefs", Context.MODE_PRIVATE)

    private val _hasAcceptedDisclaimer = MutableStateFlow(prefs.getBoolean("disclaimer_accepted", false))
    val hasAcceptedDisclaimer: StateFlow<Boolean> = _hasAcceptedDisclaimer.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(loadSearchHistory())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = IptvRepository(db.playlistDao(), db.channelDao())
        viewModelScope.launch {
            repository.ensureDefaultDataLoaded()
        }
    }

    private fun loadSearchHistory(): List<String> {
        val raw = prefs.getString("recent_searches", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|||").filter { it.isNotBlank() }
    }

    private fun saveSearchHistory(list: List<String>) {
        val raw = list.take(15).joinToString("|||")
        prefs.edit().putString("recent_searches", raw).apply()
        _searchHistory.value = list.take(15)
    }

    fun acceptDisclaimer() {
        prefs.edit().putBoolean("disclaimer_accepted", true).apply()
        _hasAcceptedDisclaimer.value = true
    }

    fun addSearchQuery(query: String) {
        val clean = query.trim()
        if (clean.length < 2) return
        val current = _searchHistory.value.toMutableList()
        current.remove(clean)
        current.add(0, clean)
        saveSearchHistory(current)
    }

    fun removeSearchQuery(query: String) {
        val current = _searchHistory.value.toMutableList()
        current.remove(query)
        saveSearchHistory(current)
    }

    fun clearSearchHistory() {
        prefs.edit().remove("recent_searches").apply()
        _searchHistory.value = emptyList()
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            repository.clearAllFavorites()
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearWatchHistory()
        }
    }

    val allPlaylists: StateFlow<List<PlaylistItem>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestAccountInfo: StateFlow<AccountInfo?> = repository.latestAccountInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allChannels: StateFlow<List<ChannelItem>> = repository.allChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteChannels: StateFlow<List<ChannelItem>> = repository.favoriteChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentChannels: StateFlow<List<ChannelItem>> = repository.recentChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val continueWatchingList: StateFlow<List<ChannelItem>> = repository.continueWatching
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistoryList: StateFlow<List<ChannelItem>> = repository.watchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId.asStateFlow()

    // Stream-type specific channels
    val liveChannels: StateFlow<List<ChannelItem>> = repository.getChannelsByStreamType("LIVE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movieChannels: StateFlow<List<ChannelItem>> = repository.getChannelsByStreamType("MOVIE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seriesChannels: StateFlow<List<ChannelItem>> = repository.getChannelsByStreamType("SERIES")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Categories per stream type
    val liveCategories: StateFlow<List<String>> = repository.getCategoriesByStreamType("LIVE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movieCategories: StateFlow<List<String>> = repository.getCategoriesByStreamType("MOVIE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seriesCategories: StateFlow<List<String>> = repository.getCategoriesByStreamType("SERIES")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category selections
    private val _selectedLiveCategory = MutableStateFlow("Tümü")
    val selectedLiveCategory: StateFlow<String> = _selectedLiveCategory.asStateFlow()

    private val _selectedMovieCategory = MutableStateFlow("Tümü")
    val selectedMovieCategory: StateFlow<String> = _selectedMovieCategory.asStateFlow()

    private val _selectedSeriesCategory = MutableStateFlow("Tümü")
    val selectedSeriesCategory: StateFlow<String> = _selectedSeriesCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentlyPlayingChannel = MutableStateFlow<ChannelItem?>(null)
    val currentlyPlayingChannel: StateFlow<ChannelItem?> = _currentlyPlayingChannel.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private fun loadThemeSetting(): AppThemeSetting {
        val name = prefs.getString("app_theme_setting", AppThemeSetting.SYSTEM.name) ?: AppThemeSetting.SYSTEM.name
        return try {
            AppThemeSetting.valueOf(name)
        } catch (_: Exception) {
            AppThemeSetting.SYSTEM
        }
    }

    private fun loadBufferSetting(): BufferOption {
        val name = prefs.getString("app_buffer_setting", BufferOption.NORMAL.name) ?: BufferOption.NORMAL.name
        return try {
            BufferOption.valueOf(name)
        } catch (_: Exception) {
            BufferOption.NORMAL
        }
    }

    private fun loadViewModeSetting(): ViewModeSetting {
        val name = prefs.getString("app_view_mode_setting", ViewModeSetting.EPG.name) ?: ViewModeSetting.EPG.name
        return try {
            ViewModeSetting.valueOf(name)
        } catch (_: Exception) {
            ViewModeSetting.EPG
        }
    }

    private val _bufferSetting = MutableStateFlow(loadBufferSetting())
    val bufferSetting: StateFlow<BufferOption> = _bufferSetting.asStateFlow()

    private val _themeSetting = MutableStateFlow(loadThemeSetting())
    val themeSetting: StateFlow<AppThemeSetting> = _themeSetting.asStateFlow()

    private val _viewModeSetting = MutableStateFlow(loadViewModeSetting())
    val viewModeSetting: StateFlow<ViewModeSetting> = _viewModeSetting.asStateFlow()

    private val _isExternalPlayerEnabled = MutableStateFlow(false)
    val isExternalPlayerEnabled: StateFlow<Boolean> = _isExternalPlayerEnabled.asStateFlow()

    // Filtered Live TV Channels (Isolated from search queries)
    val filteredLiveChannels: StateFlow<List<ChannelItem>> = combine(
        liveChannels,
        _selectedLiveCategory
    ) { channels, category ->
        filterChannelList(channels, category)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Movies (Isolated from search queries)
    val filteredMovieChannels: StateFlow<List<ChannelItem>> = combine(
        movieChannels,
        _selectedMovieCategory
    ) { channels, category ->
        filterChannelList(channels, category)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Series (Isolated from search queries)
    val filteredSeriesChannels: StateFlow<List<ChannelItem>> = combine(
        seriesChannels,
        _selectedSeriesCategory
    ) { channels, category ->
        filterChannelList(channels, category)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Global search results across all channels (Used by Search Tab)
    val globalSearchResults: StateFlow<List<ChannelItem>> = combine(
        allChannels,
        _searchQuery
    ) { channels, query ->
        val trimmed = query.trim()
        if (trimmed.isBlank()) emptyList()
        else channels.filter {
            it.name.contains(trimmed, ignoreCase = true) ||
            it.groupTitle.contains(trimmed, ignoreCase = true) ||
            it.currentProgram?.contains(trimmed, ignoreCase = true) == true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun filterChannelList(list: List<ChannelItem>, category: String): List<ChannelItem> {
        return when (category) {
            "Tümü", "All" -> list
            "Favoriler", "Favorites" -> list.filter { it.isFavorite }
            else -> list.filter { it.groupTitle.equals(category, ignoreCase = true) }
        }
    }

    fun selectLiveCategory(cat: String) { _selectedLiveCategory.value = cat }
    fun selectMovieCategory(cat: String) { _selectedMovieCategory.value = cat }
    fun selectSeriesCategory(cat: String) { _selectedSeriesCategory.value = cat }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playChannel(channel: ChannelItem) {
        _currentlyPlayingChannel.value = channel
        viewModelScope.launch {
            repository.recordChannelPlayed(channel.id)
        }
    }

    fun stopPlayback() {
        _currentlyPlayingChannel.value = null
    }

    fun playNextChannel() {
        val current = _currentlyPlayingChannel.value ?: return
        val list = when (current.streamType) {
            "MOVIE" -> filteredMovieChannels.value.ifEmpty { movieChannels.value }
            "SERIES" -> filteredSeriesChannels.value.ifEmpty { seriesChannels.value }
            else -> filteredLiveChannels.value.ifEmpty { liveChannels.value }
        }
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && list.isNotEmpty()) {
            val nextIndex = (currentIndex + 1) % list.size
            playChannel(list[nextIndex])
        }
    }

    fun playPreviousChannel() {
        val current = _currentlyPlayingChannel.value ?: return
        val list = when (current.streamType) {
            "MOVIE" -> filteredMovieChannels.value.ifEmpty { movieChannels.value }
            "SERIES" -> filteredSeriesChannels.value.ifEmpty { seriesChannels.value }
            else -> filteredLiveChannels.value.ifEmpty { liveChannels.value }
        }
        val currentIndex = list.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && list.isNotEmpty()) {
            val prevIndex = if (currentIndex - 1 < 0) list.size - 1 else currentIndex - 1
            playChannel(list[prevIndex])
        }
    }

    fun toggleFavorite(channel: ChannelItem) {
        viewModelScope.launch {
            repository.toggleFavorite(channel)
            if (_currentlyPlayingChannel.value?.id == channel.id) {
                _currentlyPlayingChannel.value = channel.copy(isFavorite = !channel.isFavorite)
            }
        }
    }

    fun importPlaylistFromUrl(name: String, url: String) {
        if (url.isBlank()) {
            _importState.value = ImportState.Error("Lütfen geçerli bir M3U veya Xtream linki girin.")
            return
        }
        _importState.value = ImportState.Loading
        viewModelScope.launch {
            val result = repository.importPlaylistFromUrl(name, url.trim())
            result.onSuccess { count ->
                _importState.value = ImportState.Success("Hesap ve oynatma listesi başarıyla bağlandı! Toplam $count içerik yüklendi.")
            }.onFailure { error ->
                _importState.value = ImportState.Error(error.localizedMessage ?: "Bağlantı kurulamadı veya liste okunamadı.")
            }
        }
    }

    fun importXtreamAccount(server: String, user: String, pass: String) {
        if (server.isBlank() || user.isBlank() || pass.isBlank()) {
            _importState.value = ImportState.Error("Lütfen sunucu adresi, kullanıcı adı ve şifreyi eksiksiz girin.")
            return
        }
        val cleanServer = if (!server.startsWith("http://") && !server.startsWith("https://")) "http://$server" else server
        val cleanServerNoSlash = cleanServer.trimEnd('/')
        val m3uUrl = "$cleanServerNoSlash/get.php?username=${user.trim()}&password=${pass.trim()}&type=m3u_plus&output=ts"

        _importState.value = ImportState.Loading
        viewModelScope.launch {
            val result = repository.importPlaylistFromUrl(user.trim(), m3uUrl)
            result.onSuccess { count ->
                _importState.value = ImportState.Success("Xtream IPTV hesabı bağlandı! Toplam $count içerik ve hesap bilgileri güncellendi.")
            }.onFailure { error ->
                _importState.value = ImportState.Error("Xtream hesabı bağlanamadı: ${error.localizedMessage}")
            }
        }
    }

    fun importPlaylistFromContent(name: String, content: String, isFile: Boolean = true) {
        if (content.isBlank()) {
            _importState.value = ImportState.Error("Dosya veya metin içeriği boş.")
            return
        }
        _importState.value = ImportState.Loading
        viewModelScope.launch {
            val result = repository.importPlaylistFromContent(name, content, isFile)
            result.onSuccess { count ->
                _importState.value = ImportState.Success("M3U dosyası yüklendi! Toplam $count içerik eklendi.")
            }.onFailure { error ->
                _importState.value = ImportState.Error(error.localizedMessage ?: "M3U içeriği ayrıştırılamadı.")
            }
        }
    }

    fun importPlaylistContent(name: String, content: String) = importPlaylistFromContent(name, content)

    fun importPlaylistFromStream(name: String, inputStream: InputStream) {
        _importState.value = ImportState.Loading
        viewModelScope.launch {
            val result = repository.importPlaylistFromStream(name, inputStream)
            result.onSuccess { count ->
                _importState.value = ImportState.Success("Dosya başarıyla yüklendi! Toplam $count içerik eklendi.")
            }.onFailure { error ->
                _importState.value = ImportState.Error(error.localizedMessage ?: "Dosya okunamadı.")
            }
        }
    }

    fun importPlaylistStream(name: String, inputStream: InputStream) = importPlaylistFromStream(name, inputStream)

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylistId.value == playlistId) {
                _selectedPlaylistId.value = null
            }
        }
    }

    fun selectPlaylist(playlistId: Long?) {
        _selectedPlaylistId.value = playlistId
    }

    fun updatePlaybackProgress(channelId: Long, positionMs: Long, durationMs: Long) {
        viewModelScope.launch {
            repository.updatePlaybackProgress(channelId, positionMs, durationMs)
        }
    }

    fun resetPlaybackProgress(channelId: Long) {
        viewModelScope.launch {
            repository.resetPlaybackProgress(channelId)
        }
    }

    fun refreshPlaylist(playlistId: Long) {
        _importState.value = ImportState.Loading
        viewModelScope.launch {
            val result = repository.refreshPlaylist(playlistId)
            result.onSuccess { count ->
                _importState.value = ImportState.Success("Liste başarıyla yenilendi! Toplam $count içerik güncellendi.")
            }.onFailure { error ->
                _importState.value = ImportState.Error("Liste yenilenemedi: ${error.localizedMessage}")
            }
        }
    }

    fun updatePlaylist(playlistId: Long, name: String, url: String) {
        _importState.value = ImportState.Loading
        viewModelScope.launch {
            val result = repository.updatePlaylist(playlistId, name, url)
            result.onSuccess { count ->
                _importState.value = ImportState.Success("Liste başarıyla güncellendi! ($count içerik)")
            }.onFailure { error ->
                _importState.value = ImportState.Error("Güncelleme başarısız: ${error.localizedMessage}")
            }
        }
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _currentlyPlayingChannel.value = null
        }
    }

    fun setBufferSetting(setting: BufferOption) {
        prefs.edit().putString("app_buffer_setting", setting.name).apply()
        _bufferSetting.value = setting
    }

    fun setThemeSetting(setting: AppThemeSetting) {
        prefs.edit().putString("app_theme_setting", setting.name).apply()
        _themeSetting.value = setting
    }

    fun setViewModeSetting(setting: ViewModeSetting) {
        prefs.edit().putString("app_view_mode_setting", setting.name).apply()
        _viewModeSetting.value = setting
    }

    fun setExternalPlayerEnabled(enabled: Boolean) {
        _isExternalPlayerEnabled.value = enabled
    }

    fun clearImportStatus() {
        _importState.value = ImportState.Idle
    }
}

