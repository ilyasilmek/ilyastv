package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ChannelItem
import com.example.ui.theme.StreamFlowLiveRed

/**
 * Dialog to manage a single item from the watch history / continue watching list.
 * Options: Play, Move to Top, Reset Progress, Toggle Favorite, Delete from History.
 */
@Composable
fun HistoryItemManageDialog(
    channel: ChannelItem,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onMoveToTop: () -> Unit,
    onResetProgress: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteFromHistory: () -> Unit
) {
    val isMovieOrSeries = channel.streamType == "MOVIE" || channel.streamType == "VOD" || channel.streamType == "SERIES"
    val hasProgress = channel.playbackPositionMs > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_manage_dialog_${channel.id}"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header: Logo/Poster + Channel Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Logo or Poster Thumbnail
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.size(52.dp)
                    ) {
                        if (!channel.posterUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = channel.posterUrl,
                                contentDescription = channel.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            )
                        } else if (!channel.logoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = channel.logoUrl,
                                contentDescription = channel.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.padding(4.dp).clip(RoundedCornerShape(6.dp))
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = channel.name.take(2).uppercase(),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Titles & Meta
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = channel.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (channel.streamType) {
                                    "LIVE" -> StreamFlowLiveRed.copy(alpha = 0.15f)
                                    "MOVIE", "VOD" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    "SERIES" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                }
                            ) {
                                Text(
                                    text = when (channel.streamType) {
                                        "LIVE" -> "CANLI YAYIN"
                                        "MOVIE", "VOD" -> "FİLM"
                                        "SERIES" -> "DİZİ"
                                        else -> channel.quality
                                    },
                                    color = when (channel.streamType) {
                                        "LIVE" -> StreamFlowLiveRed
                                        "MOVIE", "VOD" -> MaterialTheme.colorScheme.primary
                                        "SERIES" -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = channel.groupTitle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isMovieOrSeries && channel.playbackPositionMs > 0) {
                            val posMin = (channel.playbackPositionMs / 60000).toInt()
                            val totalMin = (channel.durationMs / 60000).toInt()
                            val progressText = if (totalMin > 0) "$posMin dk / $totalMin dk izlendi" else "$posMin. dakikada kaldı"
                            Text(
                                text = progressText,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                )

                Text(
                    text = "Geçmiş İşlemleri",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Actions List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1. Play Now
                    HistoryActionRow(
                        icon = Icons.Default.PlayArrow,
                        title = "Hemen İzle / Oynat",
                        subtitle = "Yayını veya içeriği başlat",
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = {
                            onDismiss()
                            onPlay()
                        }
                    )

                    // 2. Move to Top
                    HistoryActionRow(
                        icon = Icons.Default.ArrowUpward,
                        title = "En Başa Taşı",
                        subtitle = "Son izlenenler listesinde ilk sıraya yerleştir",
                        tint = MaterialTheme.colorScheme.secondary,
                        onClick = {
                            onMoveToTop()
                            onDismiss()
                        }
                    )

                    // 3. Toggle Favorite
                    HistoryActionRow(
                        icon = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        title = if (channel.isFavorite) "Favorilerden Çıkar" else "Favorilere Ekle",
                        subtitle = if (channel.isFavorite) "Favori listesinden kaldırır" else "Favoriler listenize ekler",
                        tint = if (channel.isFavorite) StreamFlowLiveRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = {
                            onToggleFavorite()
                            onDismiss()
                        }
                    )

                    // 4. Reset Progress (if VOD)
                    if (isMovieOrSeries && hasProgress) {
                        HistoryActionRow(
                            icon = Icons.Default.RestartAlt,
                            title = "İlerlemeyi Sıfırla",
                            subtitle = "İzleme süresini en başa (00:00) sar",
                            tint = MaterialTheme.colorScheme.tertiary,
                            onClick = {
                                onResetProgress()
                                onDismiss()
                            }
                        )
                    }

                    // 5. Remove from History
                    HistoryActionRow(
                        icon = Icons.Default.DeleteOutline,
                        title = "Son İzlenenlerden Kaldır (Sil)",
                        subtitle = "Bu içeriği izleme geçmişinizden siler",
                        tint = StreamFlowLiveRed,
                        onClick = {
                            onDeleteFromHistory()
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
}

@Composable
private fun HistoryActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tint.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (tint == StreamFlowLiveRed) StreamFlowLiveRed else MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
