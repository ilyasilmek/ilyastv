package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.StreamFlowLiveRed
import com.example.ui.theme.ViewModeSetting

@Composable
fun LiveTvScreen(
    channels: List<ChannelItem>,
    totalLiveCount: Int,
    categories: List<String>,
    selectedCategory: String,
    viewMode: ViewModeSetting,
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
        // Controls Row: Channel Count & View Mode Switcher
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
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // View Mode Selector Segmented Control (EPG, Grid, List)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // EPG Mode
                    val isEpg = (viewMode == ViewModeSetting.EPG)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isEpg) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
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

                    // Grid Mode
                    val isGrid = (viewMode == ViewModeSetting.GRID)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isGrid) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .clickable { onViewModeChange(ViewModeSetting.GRID) }
                            .testTag("view_mode_grid")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Küçük Resim",
                                tint = if (isGrid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Kart",
                                color = if (isGrid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // List Mode
                    val isList = (viewMode == ViewModeSetting.LIST)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isList) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .clickable { onViewModeChange(ViewModeSetting.LIST) }
                            .testTag("view_mode_list")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewList,
                                contentDescription = "Kompakt Liste",
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

        // Category Chips (Horizontal scroll)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allCategoryOptions) { cat ->
                val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = if (isSelected) {
                        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    },
                    modifier = Modifier
                        .clickable { onSelectCategory(cat) }
                        .testTag("category_chip_$cat")
                ) {
                    Text(
                        text = cat.uppercase(),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Channels Content with Selected View Mode
        if (channels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (selectedCategory == "Favoriler") Icons.Default.LiveTv else Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedCategory == "Favoriler") "Henüz Favori Kanal Eklenmedi" else "\"$selectedCategory\" Kategorisinde Kanal Yok",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (selectedCategory == "Favoriler") "Kanal kartlarındaki kalp simgesine basarak favorilerinize ekleyebilirsiniz." else "Başka bir kategori seçebilir veya yeni IPTV listesi yükleyebilirsiniz.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            AnimatedContent(
                targetState = viewMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "view_mode_anim"
            ) { mode ->
                when (mode) {
                    ViewModeSetting.EPG -> {
                        // EPG Guide View (Detailed timeline, current & next show)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(channels, key = { _, item -> item.id }) { index, channel ->
                                ChannelEpgCard(
                                    channel = channel,
                                    channelIndex = index + 1,
                                    onClick = { onChannelClick(channel) },
                                    onToggleFavorite = { onToggleFavorite(channel) }
                                )
                            }
                        }
                    }
                    ViewModeSetting.GRID -> {
                        // Grid / Thumbnail View (Poster cards)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            itemsIndexed(channels, key = { _, item -> item.id }) { _, channel ->
                                ChannelGridCard(
                                    channel = channel,
                                    onClick = { onChannelClick(channel) },
                                    onToggleFavorite = { onToggleFavorite(channel) }
                                )
                            }
                        }
                    }
                    ViewModeSetting.LIST -> {
                        // Compact List View
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(channels, key = { _, item -> item.id }) { index, channel ->
                                ChannelListCard(
                                    channel = channel,
                                    channelIndex = index + 1,
                                    onClick = { onChannelClick(channel) },
                                    onToggleFavorite = { onToggleFavorite(channel) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
