package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HistoryItemManageDialog
import com.example.ui.components.tvFocusable
import com.example.ui.theme.StreamFlowLiveRed
import com.example.ui.theme.ViewModeSetting

@Composable
fun LiveTvScreen(
    channels: List<ChannelItem>,
    totalLiveCount: Int,
    categories: List<String>,
    selectedCategory: String,
    viewMode: ViewModeSetting,
    watchHistoryList: List<ChannelItem> = emptyList(),
    onRemoveFromHistory: (Long) -> Unit = {},
    onMoveToTopHistory: (Long) -> Unit = {},
    onResetProgress: (Long) -> Unit = {},
    onViewModeChange: (ViewModeSetting) -> Unit,
    onSelectCategory: (String) -> Unit,
    onChannelClick: (ChannelItem) -> Unit,
    onToggleFavorite: (ChannelItem) -> Unit,
    onNavigateToAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalLiveCount == 0 && channels.isEmpty()) {
        EmptyStateView(
            title = "Canlı TV Kanalları Bulunamadı",
            subtitle = "Canlı yayınları izlemek için IPTV linkinizi (.m3u / Xtream) girin veya dosyanızı yükleyin.",
            icon = Icons.Default.LiveTv,
            buttonText = "+ Liste / Hesap Ekle",
            onActionClick = onNavigateToAdd,
            modifier = modifier
        )
        return
    }

    var selectedHistoryChannel by remember { mutableStateOf<ChannelItem?>(null) }

    // History Item Management Dialog (Sil, Başa Taşı, Favorilere Ekle/Çıkar, Oynat)
    selectedHistoryChannel?.let { channel ->
        HistoryItemManageDialog(
            channel = channel,
            onDismiss = { selectedHistoryChannel = null },
            onPlay = { onChannelClick(channel) },
            onMoveToTop = { onMoveToTopHistory(channel.id) },
            onResetProgress = { onResetProgress(channel.id) },
            onToggleFavorite = { onToggleFavorite(channel) },
            onDeleteFromHistory = { onRemoveFromHistory(channel.id) }
        )
    }

    val liveHistory = watchHistoryList.filter { it.streamType == "LIVE" }

    val allCategoryOptions = buildList {
        add("Tümü")
        add("Favoriler")
        addAll(categories.filter { it != "Tümü" && it != "Favoriler" && it != "All" && it != "Favorites" })
    }.distinct()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("live_tv_screen")
    ) {
        // Controls Row: Count & View Switcher (EPG Rehber / Izgara / Liste)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${channels.size} Kanal",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (selectedCategory != "Tümü") {
                    Text(
                        text = " • $selectedCategory",
                        color = StreamFlowLiveRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // View mode switch chips
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
                            .testTag("view_mode_epg")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subject,
                                contentDescription = "EPG Rehber",
                                tint = if (isEpg) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "EPG",
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
                            .testTag("view_mode_grid")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Izgara",
                                tint = if (isGrid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Izgara",
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
                            .testTag("view_mode_list")
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
                    color = if (isSelected) StreamFlowLiveRed else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .tvFocusable(shape = RoundedCornerShape(20.dp)) { onSelectCategory(cat) }
                        .clickable { onSelectCategory(cat) }
                        .testTag("category_tab_$cat")
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
                                tint = if (isSelected) Color.White else StreamFlowLiveRed,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Content Area depending on viewMode
        when (viewMode) {
            ViewModeSetting.EPG -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (liveHistory.isNotEmpty()) {
                        item {
                            RecentlyWatchedChannelsRow(
                                channels = liveHistory,
                                onChannelClick = onChannelClick,
                                onChannelLongClick = { selectedHistoryChannel = it }
                            )
                        }
                    }

                    itemsIndexed(channels, key = { _, item -> item.id }) { index, channel ->
                        ChannelEpgCard(
                            channel = channel,
                            channelIndex = index + 1,
                            onClick = { onChannelClick(channel) },
                            onToggleFavorite = { onToggleFavorite(channel) },
                            modifier = Modifier.tvFocusable(shape = RoundedCornerShape(16.dp), onClick = { onChannelClick(channel) })
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
                    if (liveHistory.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            RecentlyWatchedChannelsRow(
                                channels = liveHistory,
                                onChannelClick = onChannelClick,
                                onChannelLongClick = { selectedHistoryChannel = it }
                            )
                        }
                    }

                    itemsIndexed(channels, key = { _, item -> item.id }) { _, channel ->
                        ChannelGridCard(
                            channel = channel,
                            onClick = { onChannelClick(channel) },
                            onToggleFavorite = { onToggleFavorite(channel) },
                            modifier = Modifier.tvFocusable(shape = RoundedCornerShape(14.dp), onClick = { onChannelClick(channel) })
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
                    if (liveHistory.isNotEmpty()) {
                        item {
                            RecentlyWatchedChannelsRow(
                                channels = liveHistory,
                                onChannelClick = onChannelClick,
                                onChannelLongClick = { selectedHistoryChannel = it }
                            )
                        }
                    }

                    itemsIndexed(channels, key = { _, item -> item.id }) { index, channel ->
                        ChannelListCard(
                            channel = channel,
                            channelIndex = index + 1,
                            onClick = { onChannelClick(channel) },
                            onToggleFavorite = { onToggleFavorite(channel) },
                            modifier = Modifier.tvFocusable(shape = RoundedCornerShape(12.dp), onClick = { onChannelClick(channel) })
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentlyWatchedChannelsRow(
    channels: List<ChannelItem>,
    onChannelClick: (ChannelItem) -> Unit,
    onChannelLongClick: (ChannelItem) -> Unit
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
                    tint = StreamFlowLiveRed,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Son İzlenen Kanallar",
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
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(channels.take(12), key = { it.id }) { ch ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .width(175.dp)
                        .tvFocusable(shape = RoundedCornerShape(12.dp), onClick = { onChannelClick(ch) })
                        .combinedClickable(
                            onClick = { onChannelClick(ch) },
                            onLongClick = { onChannelLongClick(ch) }
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = ch.name.take(2).uppercase(),
                                    color = StreamFlowLiveRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ch.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = ch.quality,
                                color = StreamFlowLiveRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = { onChannelLongClick(ch) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Seçenekler",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
