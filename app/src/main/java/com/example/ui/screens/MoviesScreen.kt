package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChannelItem
import com.example.ui.components.ChannelEpgCard
import com.example.ui.components.ChannelGridCard
import com.example.ui.components.ChannelListCard
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HistoryItemManageDialog
import com.example.ui.components.tvFocusable
import com.example.ui.theme.ViewModeSetting

@Composable
fun MoviesScreen(
    movies: List<ChannelItem>,
    totalMovieCount: Int,
    categories: List<String>,
    selectedCategory: String,
    viewMode: ViewModeSetting,
    continueWatchingList: List<ChannelItem> = emptyList(),
    onResetProgress: (Long) -> Unit = {},
    onRemoveFromHistory: (Long) -> Unit = {},
    onMoveToTopHistory: (Long) -> Unit = {},
    onViewModeChange: (ViewModeSetting) -> Unit,
    onSelectCategory: (String) -> Unit,
    onMovieClick: (ChannelItem) -> Unit,
    onToggleFavorite: (ChannelItem) -> Unit,
    onNavigateToAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalMovieCount == 0 && movies.isEmpty()) {
        EmptyStateView(
            title = "Film İçeriği Bulunamadı",
            subtitle = "Filmleri listelemek ve izlemek için IPTV linkinizi (.m3u / Xtream) girin veya dosyanızı yükleyin.",
            icon = Icons.Default.Movie,
            buttonText = "+ Film / Liste Ekle",
            onActionClick = onNavigateToAdd,
            modifier = modifier
        )
        return
    }

    var selectedManageMovie by remember { mutableStateOf<ChannelItem?>(null) }

    // History & Continue Watching Management Dialog
    selectedManageMovie?.let { channel ->
        HistoryItemManageDialog(
            channel = channel,
            onDismiss = { selectedManageMovie = null },
            onPlay = { onMovieClick(channel) },
            onMoveToTop = { onMoveToTopHistory(channel.id) },
            onResetProgress = { onResetProgress(channel.id) },
            onToggleFavorite = { onToggleFavorite(channel) },
            onDeleteFromHistory = { onRemoveFromHistory(channel.id) }
        )
    }

    val movieContinueWatching = continueWatchingList.filter { it.streamType == "MOVIE" || it.streamType == "VOD" }

    val allCategoryOptions = buildList {
        add("Tümü")
        add("Favoriler")
        addAll(categories.filter { it != "Tümü" && it != "Favoriler" && it != "All" && it != "Favorites" })
    }.distinct()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("movies_screen")
    ) {
        // Controls Row: Count & View Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${movies.size} Film",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (selectedCategory != "Tümü") {
                    Text(
                        text = " • $selectedCategory",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val isEpg = (viewMode == ViewModeSetting.EPG)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isEpg) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .tvFocusable(shape = RoundedCornerShape(10.dp)) { onViewModeChange(ViewModeSetting.EPG) }
                            .clickable { onViewModeChange(ViewModeSetting.EPG) }
                            .testTag("movie_view_mode_epg")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subject,
                                contentDescription = "Detay",
                                tint = if (isEpg) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Detay",
                                color = if (isEpg) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val isGrid = (viewMode == ViewModeSetting.GRID)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isGrid) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .tvFocusable(shape = RoundedCornerShape(10.dp)) { onViewModeChange(ViewModeSetting.GRID) }
                            .clickable { onViewModeChange(ViewModeSetting.GRID) }
                            .testTag("movie_view_mode_grid")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Poster",
                                tint = if (isGrid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Poster",
                                color = if (isGrid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val isList = (viewMode == ViewModeSetting.LIST)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isList) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .tvFocusable(shape = RoundedCornerShape(10.dp)) { onViewModeChange(ViewModeSetting.LIST) }
                            .clickable { onViewModeChange(ViewModeSetting.LIST) }
                            .testTag("movie_view_mode_list")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewList,
                                contentDescription = "Liste",
                                tint = if (isList) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Liste",
                                color = if (isList) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Category Tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allCategoryOptions) { cat ->
                val isSelected = (cat == selectedCategory)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .tvFocusable(shape = RoundedCornerShape(20.dp)) { onSelectCategory(cat) }
                        .clickable { onSelectCategory(cat) }
                        .testTag("movie_category_tab_$cat")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (cat == "Favoriler") {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color(0xFFE50914),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = cat,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Content Area with Continue Watching carousel at top
        when (viewMode) {
            ViewModeSetting.EPG -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (movieContinueWatching.isNotEmpty()) {
                        item {
                            ContinueWatchingSection(
                                items = movieContinueWatching,
                                onMovieClick = onMovieClick,
                                onResetProgress = onResetProgress,
                                onManageItem = { selectedManageMovie = it }
                            )
                        }
                    }

                    itemsIndexed(movies, key = { _, item -> item.id }) { index, movie ->
                        ChannelEpgCard(
                            channel = movie,
                            channelIndex = index + 1,
                            onClick = { onMovieClick(movie) },
                            onToggleFavorite = { onToggleFavorite(movie) },
                            modifier = Modifier.tvFocusable(shape = RoundedCornerShape(16.dp), onClick = { onMovieClick(movie) })
                        )
                    }
                }
            }
            ViewModeSetting.GRID -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (movieContinueWatching.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ContinueWatchingSection(
                                items = movieContinueWatching,
                                onMovieClick = onMovieClick,
                                onResetProgress = onResetProgress,
                                onManageItem = { selectedManageMovie = it }
                            )
                        }
                    }

                    itemsIndexed(movies, key = { _, item -> item.id }) { _, movie ->
                        ChannelGridCard(
                            channel = movie,
                            onClick = { onMovieClick(movie) },
                            onToggleFavorite = { onToggleFavorite(movie) },
                            modifier = Modifier.tvFocusable(shape = RoundedCornerShape(14.dp), onClick = { onMovieClick(movie) })
                        )
                    }
                }
            }
            ViewModeSetting.LIST -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (movieContinueWatching.isNotEmpty()) {
                        item {
                            ContinueWatchingSection(
                                items = movieContinueWatching,
                                onMovieClick = onMovieClick,
                                onResetProgress = onResetProgress,
                                onManageItem = { selectedManageMovie = it }
                            )
                        }
                    }

                    itemsIndexed(movies, key = { _, item -> item.id }) { index, movie ->
                        ChannelListCard(
                            channel = movie,
                            channelIndex = index + 1,
                            onClick = { onMovieClick(movie) },
                            onToggleFavorite = { onToggleFavorite(movie) },
                            modifier = Modifier.tvFocusable(shape = RoundedCornerShape(12.dp), onClick = { onMovieClick(movie) })
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueWatchingSection(
    items: List<ChannelItem>,
    onMovieClick: (ChannelItem) -> Unit,
    onResetProgress: (Long) -> Unit,
    onManageItem: (ChannelItem) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Kaldığın Yerden Devam Et",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Düzenle (Basılı Tutun)",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(items, key = { it.id }) { item ->
                ContinueWatchingCard(
                    channel = item,
                    onClick = { onMovieClick(item) },
                    onResetProgress = { onResetProgress(item.id) },
                    onLongClick = { onManageItem(item) },
                    onOptionsClick = { onManageItem(item) }
                )
            }
        }
    }
}
