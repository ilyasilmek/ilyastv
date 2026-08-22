package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.StreamPlayer
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.LiveTvScreen
import com.example.ui.screens.MoviesScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SeriesScreen
import com.example.ui.theme.AppThemeSetting
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ViewModeSetting
import com.example.ui.viewmodel.IptvViewModel

enum class NavigationTab(val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    LIVE_TV("Canlı TV", Icons.Filled.LiveTv, Icons.Outlined.LiveTv),
    MOVIES("Filmler", Icons.Filled.Movie, Icons.Outlined.Movie),
    SERIES("Diziler", Icons.Filled.Tv, Icons.Outlined.Tv),
    SEARCH("Arama", Icons.Filled.Search, Icons.Outlined.Search),
    ACCOUNT("Hesap & Liste", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong)
}

class MainActivity : ComponentActivity() {

    private val viewModel: IptvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeSetting by viewModel.themeSetting.collectAsStateWithLifecycle()
            MyApplicationTheme(themeSetting = themeSetting) {
                StreamFlowApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StreamFlowApp(viewModel: IptvViewModel) {
    val allChannels by viewModel.allChannels.collectAsStateWithLifecycle()
    val playlists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val accountInfo by viewModel.latestAccountInfo.collectAsStateWithLifecycle()

    val liveChannels by viewModel.liveChannels.collectAsStateWithLifecycle()
    val filteredLiveChannels by viewModel.filteredLiveChannels.collectAsStateWithLifecycle()
    val liveCategories by viewModel.liveCategories.collectAsStateWithLifecycle()
    val selectedLiveCategory by viewModel.selectedLiveCategory.collectAsStateWithLifecycle()

    val movieChannels by viewModel.movieChannels.collectAsStateWithLifecycle()
    val filteredMovieChannels by viewModel.filteredMovieChannels.collectAsStateWithLifecycle()
    val movieCategories by viewModel.movieCategories.collectAsStateWithLifecycle()
    val selectedMovieCategory by viewModel.selectedMovieCategory.collectAsStateWithLifecycle()

    val seriesChannels by viewModel.seriesChannels.collectAsStateWithLifecycle()
    val filteredSeriesChannels by viewModel.filteredSeriesChannels.collectAsStateWithLifecycle()
    val seriesCategories by viewModel.seriesCategories.collectAsStateWithLifecycle()
    val selectedSeriesCategory by viewModel.selectedSeriesCategory.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val globalSearchResults by viewModel.globalSearchResults.collectAsStateWithLifecycle()
    val currentlyPlayingChannel by viewModel.currentlyPlayingChannel.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val bufferSetting by viewModel.bufferSetting.collectAsStateWithLifecycle()
    val themeSetting by viewModel.themeSetting.collectAsStateWithLifecycle()
    val viewModeSetting by viewModel.viewModeSetting.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(NavigationTab.LIVE_TV) }

    // Intercept back button when player is open
    BackHandler(enabled = currentlyPlayingChannel != null) {
        viewModel.stopPlayback()
    }

    if (currentlyPlayingChannel != null) {
        // Fullscreen Player
        val currentPlayList = when (currentlyPlayingChannel?.streamType) {
            "MOVIE" -> filteredMovieChannels.ifEmpty { movieChannels }
            "SERIES" -> filteredSeriesChannels.ifEmpty { seriesChannels }
            else -> filteredLiveChannels.ifEmpty { liveChannels }
        }

        StreamPlayer(
            channel = currentlyPlayingChannel!!,
            allChannels = currentPlayList.ifEmpty { allChannels },
            bufferOption = bufferSetting,
            onClose = { viewModel.stopPlayback() },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onSelectChannel = { viewModel.playChannel(it) },
            onNextChannel = { viewModel.playNextChannel() },
            onPreviousChannel = { viewModel.playPreviousChannel() }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                // Top App Bar
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_ilyas_tv_logo),
                                contentDescription = "İlyasTV Logo",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "İlyas",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "TV",
                                    color = Color(0xFFE50914),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                )
                            }
                        }

                        // Right actions (Theme toggle, Remaining Time Badge & Quick Add)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Quick Theme Toggle Icon
                            IconButton(
                                onClick = {
                                    val nextTheme = if (themeSetting == AppThemeSetting.DARK) AppThemeSetting.LIGHT else AppThemeSetting.DARK
                                    viewModel.setThemeSetting(nextTheme)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                                    .testTag("quick_theme_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (themeSetting == AppThemeSetting.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Tema Değiştir",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Account expiration countdown badge
                            if (accountInfo?.remainingTimeText != null) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (accountInfo?.isExpired == true) Color(0xFFB71C1C).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (accountInfo?.isExpired == true) Color(0xFFE53935) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.clickable { currentTab = NavigationTab.ACCOUNT }
                                ) {
                                    Text(
                                        text = accountInfo?.remainingTimeText ?: "",
                                        color = if (accountInfo?.isExpired == true) Color(0xFFFF8A80) else MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            // Add playlist button
                            IconButton(
                                onClick = { currentTab = NavigationTab.ACCOUNT },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .testTag("top_add_playlist_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Liste Ekle",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Bottom Navigation Bar with 5 categories (Canlı TV - Filmler - Diziler - Arama - Hesap & Liste)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.98f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .height(64.dp)
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavigationTab.values().forEach { tab ->
                            val isSelected = (currentTab == tab)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { currentTab = tab }
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                                    .testTag("nav_tab_${tab.name.lowercase()}")
                            ) {
                                if (isSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = tab.activeIcon,
                                                contentDescription = tab.title,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(17.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = tab.title,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    Icon(
                                        imageVector = tab.inactiveIcon,
                                        contentDescription = tab.title,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tab.title,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { tab ->
                    when (tab) {
                        NavigationTab.LIVE_TV -> {
                            LiveTvScreen(
                                channels = filteredLiveChannels,
                                totalLiveCount = liveChannels.size,
                                categories = liveCategories,
                                selectedCategory = selectedLiveCategory,
                                viewMode = viewModeSetting,
                                onViewModeChange = { viewModel.setViewModeSetting(it) },
                                onSelectCategory = { viewModel.selectLiveCategory(it) },
                                onChannelClick = { viewModel.playChannel(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onNavigateToAdd = { currentTab = NavigationTab.ACCOUNT }
                            )
                        }
                        NavigationTab.MOVIES -> {
                            MoviesScreen(
                                movies = filteredMovieChannels,
                                totalMovieCount = movieChannels.size,
                                categories = movieCategories,
                                selectedCategory = selectedMovieCategory,
                                viewMode = viewModeSetting,
                                onViewModeChange = { viewModel.setViewModeSetting(it) },
                                onSelectCategory = { viewModel.selectMovieCategory(it) },
                                onMovieClick = { viewModel.playChannel(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onNavigateToAdd = { currentTab = NavigationTab.ACCOUNT }
                            )
                        }
                        NavigationTab.SERIES -> {
                            SeriesScreen(
                                series = filteredSeriesChannels,
                                totalSeriesCount = seriesChannels.size,
                                categories = seriesCategories,
                                selectedCategory = selectedSeriesCategory,
                                viewMode = viewModeSetting,
                                onViewModeChange = { viewModel.setViewModeSetting(it) },
                                onSelectCategory = { viewModel.selectSeriesCategory(it) },
                                onSeriesClick = { viewModel.playChannel(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onNavigateToAdd = { currentTab = NavigationTab.ACCOUNT }
                            )
                        }
                        NavigationTab.SEARCH -> {
                            SearchScreen(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                searchResults = globalSearchResults,
                                onChannelClick = { viewModel.playChannel(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) }
                            )
                        }
                        NavigationTab.ACCOUNT -> {
                            AccountScreen(
                                accountInfo = accountInfo,
                                playlists = playlists,
                                importState = importState,
                                bufferOption = bufferSetting,
                                themeSetting = themeSetting,
                                viewModeSetting = viewModeSetting,
                                onBufferOptionChange = { viewModel.setBufferSetting(it) },
                                onThemeSettingChange = { viewModel.setThemeSetting(it) },
                                onViewModeSettingChange = { viewModel.setViewModeSetting(it) },
                                onImportUrl = { name, url -> viewModel.importPlaylistFromUrl(name, url) },
                                onImportXtream = { server, user, pass -> viewModel.importXtreamAccount(server, user, pass) },
                                onImportContent = { name, content, isFile -> viewModel.importPlaylistFromContent(name, content, isFile) },
                                onImportStream = { name, stream -> viewModel.importPlaylistFromStream(name, stream) },
                                onDeletePlaylist = { viewModel.deletePlaylist(it) },
                                onClearAllData = { viewModel.clearAllData() },
                                onClearImportStatus = { viewModel.clearImportStatus() }
                            )
                        }
                    }
                }
            }
        }
    }
}
