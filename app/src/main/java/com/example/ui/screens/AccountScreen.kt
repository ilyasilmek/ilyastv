package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountInfo
import com.example.data.model.PlaylistItem
import com.example.ui.components.LegalDisclaimerDialog
import com.example.ui.components.tvFocusable
import com.example.ui.theme.AppThemeSetting
import com.example.ui.theme.StreamFlowAccentOrange
import com.example.ui.theme.StreamFlowLiveRed
import com.example.ui.theme.ViewModeSetting
import com.example.ui.viewmodel.BufferOption
import com.example.ui.viewmodel.ImportState
import java.io.InputStream

@Composable
fun AccountScreen(
    accountInfo: AccountInfo?,
    playlists: List<PlaylistItem>,
    selectedPlaylistId: Long? = null,
    importState: ImportState,
    bufferOption: BufferOption,
    themeSetting: AppThemeSetting,
    viewModeSetting: ViewModeSetting,
    onSelectPlaylist: (Long) -> Unit = {},
    onBufferOptionChange: (BufferOption) -> Unit,
    onThemeSettingChange: (AppThemeSetting) -> Unit,
    onViewModeSettingChange: (ViewModeSetting) -> Unit,
    onImportUrl: (name: String, url: String) -> Unit,
    onImportXtream: (server: String, user: String, pass: String) -> Unit,
    onImportContent: (name: String, content: String, isFile: Boolean) -> Unit,
    onImportStream: (name: String, inputStream: InputStream) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onRefreshPlaylist: (Long) -> Unit = {},
    onUpdatePlaylist: (playlistId: Long, name: String, url: String) -> Unit = { _, _, _ -> },
    onClearSearchHistory: () -> Unit = {},
    onClearFavorites: () -> Unit = {},
    onClearWatchHistory: () -> Unit = {},
    onClearAllData: () -> Unit,
    onClearImportStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Link, 1: Xtream, 2: Dosya

    // Form states
    var linkName by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }

    var xtreamServer by remember { mutableStateOf("") }
    var xtreamUser by remember { mutableStateOf("") }
    var xtreamPass by remember { mutableStateOf("") }

    var pasteName by remember { mutableStateOf("") }
    var pasteContent by remember { mutableStateOf("") }

    var showBufferDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showViewModeDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showClearFavoritesDialog by remember { mutableStateOf(false) }
    var showClearSearchDialog by remember { mutableStateOf(false) }
    var showClearWatchHistoryDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var playlistToEdit by remember { mutableStateOf<PlaylistItem?>(null) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Yüklenen Liste"
                    onImportStream(fileName, inputStream)
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("account_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Account & Expiration Countdown Card
        item {
            AccountStatusCard(accountInfo = accountInfo, playlistCount = playlists.size)
        }

        // 2. Add New Playlist / Xtream Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Yeni Liste veya Hesap Ekle",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "IPTV linkinizi girin, Xtream hesabınızı bağlayın ya da .m3u dosyası yükleyin.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                    )

                    // Tabs: Link / Xtream / Dosya
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("M3U Linki", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                            icon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Xtream", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                            icon = { Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("M3U Dosyası", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                            icon = { Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedTab) {
                        0 -> {
                            // Link Form
                            OutlinedTextField(
                                value = linkUrl,
                                onValueChange = { linkUrl = it },
                                label = { Text("M3U / M3U8 Playlist URL'si") },
                                placeholder = { Text("http://example.com/get.php?username=...") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = outlinedColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_m3u_url")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = linkName,
                                onValueChange = { linkName = it },
                                label = { Text("Liste Adı (İsteğe Bağlı)") },
                                placeholder = { Text("Örn: Kişisel IPTV") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = outlinedColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    if (linkUrl.isNotBlank()) {
                                        onImportUrl(linkName, linkUrl)
                                    }
                                },
                                enabled = linkUrl.isNotBlank() && importState !is ImportState.Loading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_m3u_url_button")
                            ) {
                                if (importState is ImportState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Bağlanıyor ve Kanallar Yükleniyor...", color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Listeyi İndir & Hesabı Doğrula", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        1 -> {
                            // Xtream Form
                            OutlinedTextField(
                                value = xtreamServer,
                                onValueChange = { xtreamServer = it },
                                label = { Text("Sunucu URL / Adresi") },
                                placeholder = { Text("http://iptvserver.com:8080") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = outlinedColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_xtream_server")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = xtreamUser,
                                onValueChange = { xtreamUser = it },
                                label = { Text("Kullanıcı Adı") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = outlinedColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_xtream_user")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = xtreamPass,
                                onValueChange = { xtreamPass = it },
                                label = { Text("Şifre") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = outlinedColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_xtream_pass")
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    onImportXtream(xtreamServer, xtreamUser, xtreamPass)
                                },
                                enabled = xtreamServer.isNotBlank() && xtreamUser.isNotBlank() && xtreamPass.isNotBlank() && importState !is ImportState.Loading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_xtream_button")
                            ) {
                                if (importState is ImportState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Xtream Hesabına Giriş Yapılıyor...", color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Icon(Icons.Default.Dns, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Xtream Hesabını Bağla", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        2 -> {
                            // File Picker & Raw Paste
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { filePickerLauncher.launch("*/*") },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("select_m3u_file_button")
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cihazdan .M3U / .M3U8 Dosyası Seç", fontWeight = FontWeight.Bold)
                                }

                                Text(
                                    text = "Veya M3U metin içeriğini yapıştırın:",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )

                                OutlinedTextField(
                                    value = pasteContent,
                                    onValueChange = { pasteContent = it },
                                    label = { Text("M3U Metni") },
                                    placeholder = { Text("#EXTM3U\n#EXTINF:-1, Kanal 1\nhttp://...") },
                                    shape = RoundedCornerShape(12.dp),
                                    maxLines = 4,
                                    colors = outlinedColors(),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        if (pasteContent.isNotBlank()) {
                                            onImportContent(pasteName.ifBlank { "Yapıştırılan M3U" }, pasteContent, false)
                                        }
                                    },
                                    enabled = pasteContent.isNotBlank() && importState !is ImportState.Loading,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text("Yapıştırılan Listeyi Yükle", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Import feedback (Success / Error)
                    when (importState) {
                        is ImportState.Success -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1B5E20).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = importState.message,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        is ImportState.Error -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = importState.message,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        // 3. Active Playlists List
        if (playlists.isNotEmpty()) {
            item {
                Text(
                    text = "Yüklü Oynatma Listeleri (${playlists.size})",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            items(playlists, key = { it.id }) { playlist ->
                val isSelected = (selectedPlaylistId == null && playlist == playlists.firstOrNull()) || (selectedPlaylistId == playlist.id)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPlaylist(playlist.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = playlist.name,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "AKTİF",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Toplam ${playlist.channelCount} İçerik  •  Canlı: ${playlist.liveCount}  •  Film: ${playlist.movieCount}  •  Dizi: ${playlist.seriesCount}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            if (!playlist.username.isNullOrBlank()) {
                                Text(
                                    text = "Kullanıcı: ${playlist.username}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (!isSelected) {
                                TextButton(
                                    onClick = { onSelectPlaylist(playlist.id) },
                                    modifier = Modifier.tvFocusable(shape = RoundedCornerShape(8.dp)) { onSelectPlaylist(playlist.id) }
                                ) {
                                    Text("Seç", fontWeight = FontWeight.Bold)
                                }
                            }

                            // Refresh button (Kaynağı Yenile)
                            IconButton(
                                onClick = { onRefreshPlaylist(playlist.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .tvFocusable(shape = CircleShape) { onRefreshPlaylist(playlist.id) }
                                    .testTag("refresh_playlist_${playlist.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Kaynağı Yenile",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Edit button (Kaynağı Düzenle)
                            IconButton(
                                onClick = { playlistToEdit = playlist },
                                modifier = Modifier
                                    .size(36.dp)
                                    .tvFocusable(shape = CircleShape) { playlistToEdit = playlist }
                                    .testTag("edit_playlist_${playlist.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Kaynağı Düzenle",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Delete button (Listeyi Sil)
                            IconButton(
                                onClick = { onDeletePlaylist(playlist.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .tvFocusable(shape = CircleShape) { onDeletePlaylist(playlist.id) }
                                    .testTag("delete_playlist_${playlist.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Listeyi Sil",
                                    tint = StreamFlowLiveRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Appearance & General Settings Card (Tema & Görünüm Modu)
        item {
            Text(
                text = "Görünüm & Uygulama Ayarları",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Theme Mode Selector
                    val themeIcon = when (themeSetting) {
                        AppThemeSetting.DARK -> Icons.Default.DarkMode
                        AppThemeSetting.LIGHT -> Icons.Default.LightMode
                        AppThemeSetting.SYSTEM -> Icons.Default.SettingsBrightness
                    }
                    SettingsItemRow(
                        icon = themeIcon,
                        title = "Tema Modu",
                        subtitle = themeSetting.title,
                        onClick = { showThemeDialog = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)))

                    // Default View Mode Selector
                    val viewIcon = when (viewModeSetting) {
                        ViewModeSetting.EPG -> Icons.Default.Subject
                        ViewModeSetting.GRID -> Icons.Default.GridView
                        ViewModeSetting.LIST -> Icons.Default.ViewList
                    }
                    SettingsItemRow(
                        icon = viewIcon,
                        title = "Varsayılan Ekran Görünümü",
                        subtitle = viewModeSetting.title,
                        onClick = { showViewModeDialog = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)))

                    // Buffer Setting
                    SettingsItemRow(
                        icon = Icons.Default.Speed,
                        title = "Arabellek & Yayın Ayarı",
                        subtitle = bufferOption.label,
                        onClick = { showBufferDialog = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)))

                    // Clear Search History
                    SettingsItemRow(
                        icon = Icons.Default.SearchOff,
                        title = "Arama Geçmişini Temizle",
                        subtitle = "Kaydedilen son arama sorgularını sıfırlar",
                        onClick = { showClearSearchDialog = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)))

                    // Clear Favorites
                    SettingsItemRow(
                        icon = Icons.Default.FavoriteBorder,
                        title = "Favori Listesini Temizle",
                        subtitle = "Tüm kanallardaki favori işaretlerini kaldırır",
                        onClick = { showClearFavoritesDialog = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)))

                    // Clear Watch History
                    SettingsItemRow(
                        icon = Icons.Default.History,
                        title = "İzleme Geçmişini Sıfırla",
                        subtitle = "Kaldığın yerden devam et ve son izlenenler geçmişini temizler",
                        onClick = { showClearWatchHistoryDialog = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)))

                    // Legal Disclaimer & Policy
                    SettingsItemRow(
                        icon = Icons.Default.Gavel,
                        title = "Yasal Uyarı & Sorumluluk Reddi",
                        subtitle = "Telif hakları bildirimi, kullanım şartları ve yasal sözleşme",
                        onClick = { showDisclaimerDialog = true }
                    )

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)))

                    // Clear All Data
                    SettingsItemRow(
                        icon = Icons.Default.DeleteOutline,
                        title = "Tüm Listeleri ve Verileri Temizle",
                        subtitle = "Tüm kanalları siler ve uygulamayı sıfırlar",
                        titleColor = StreamFlowLiveRed,
                        onClick = { showClearConfirmDialog = true }
                    )
                }
            }
        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(text = "Tema Seçimi", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppThemeSetting.values().forEach { option ->
                        val optIcon = when (option) {
                            AppThemeSetting.SYSTEM -> Icons.Default.SettingsBrightness
                            AppThemeSetting.DARK -> Icons.Default.DarkMode
                            AppThemeSetting.LIGHT -> Icons.Default.LightMode
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (themeSetting == option) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                            border = if (themeSetting == option) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeSettingChange(option)
                                    showThemeDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = themeSetting == option,
                                    onClick = {
                                        onThemeSettingChange(option)
                                        showThemeDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = optIcon,
                                    contentDescription = null,
                                    tint = if (themeSetting == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = option.title,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    if (option.subtitle.isNotBlank()) {
                                        Text(
                                            text = option.subtitle,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Tamam", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // View Mode Selection Dialog
    if (showViewModeDialog) {
        AlertDialog(
            onDismissRequest = { showViewModeDialog = false },
            title = {
                Text(text = "Varsayılan Görünüm Modu", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ViewModeSetting.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onViewModeSettingChange(mode)
                                    showViewModeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = viewModeSetting == mode,
                                onClick = {
                                    onViewModeSettingChange(mode)
                                    showViewModeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = mode.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = when (mode) {
                                        ViewModeSetting.EPG -> "Şimdiki & Sonraki program akışı ve ilerleme çubuğu"
                                        ViewModeSetting.GRID -> "Geniş küçük resim / poster kartları (2 sütun)"
                                        ViewModeSetting.LIST -> "Kompakt, hızlı kanal listesi satırları"
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showViewModeDialog = false }) {
                    Text("Tamam", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Buffer Settings Dialog
    if (showBufferDialog) {
        AlertDialog(
            onDismissRequest = { showBufferDialog = false },
            title = {
                Text(text = "Arabellek / Gecikme Ayarı", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BufferOption.values().forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onBufferOptionChange(option)
                                    showBufferDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = bufferOption == option,
                                onClick = {
                                    onBufferOptionChange(option)
                                    showBufferDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = option.label,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = option.description,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBufferDialog = false }) {
                    Text("Tamam", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Clear Search History Dialog
    if (showClearSearchDialog) {
        AlertDialog(
            onDismissRequest = { showClearSearchDialog = false },
            title = { Text("Arama Geçmişi Temizlensin mi?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Kaydedilmiş olan tüm arama geçmişi ve arama etiketleri temizlenecektir.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearSearchHistory()
                        showClearSearchDialog = false
                    }
                ) {
                    Text("Temizle", color = StreamFlowLiveRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearSearchDialog = false }) {
                    Text("İptal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Clear Favorites Dialog
    if (showClearFavoritesDialog) {
        AlertDialog(
            onDismissRequest = { showClearFavoritesDialog = false },
            title = { Text("Favori Listesi Temizlensin mi?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Tüm favori kanallar, filmler ve diziler favori listenizden çıkarılacaktır.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearFavorites()
                        showClearFavoritesDialog = false
                    }
                ) {
                    Text("Favorileri Temizle", color = StreamFlowLiveRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearFavoritesDialog = false }) {
                    Text("İptal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Clear Watch History Dialog
    if (showClearWatchHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearWatchHistoryDialog = false },
            title = { Text("İzleme Geçmişi Sıfırlansın mı?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Kaldığın yerden devam et listesi ve son izlenen içerik kayıtları sıfırlanacaktır.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearWatchHistory()
                        showClearWatchHistoryDialog = false
                    }
                ) {
                    Text("Sıfırla", color = StreamFlowLiveRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearWatchHistoryDialog = false }) {
                    Text("İptal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Legal Disclaimer Modal (Review Mode)
    if (showDisclaimerDialog) {
        LegalDisclaimerDialog(
            isFirstLaunch = false,
            onAccept = { showDisclaimerDialog = false },
            onDismiss = { showDisclaimerDialog = false }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Tüm Veriler Silinsin mi?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Tüm oynatma listeleri, kanallar ve hesap bilgileri silinecek ve uygulama boş başlangıç ekranına dönecektir.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllData()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Tümünü Sil", color = StreamFlowLiveRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("İptal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Edit Playlist Dialog (Kaynağı Düzenle)
    if (playlistToEdit != null) {
        val targetPlaylist = playlistToEdit!!
        var editedName by remember(targetPlaylist) { mutableStateOf(targetPlaylist.name) }
        var editedUrl by remember(targetPlaylist) { mutableStateOf(targetPlaylist.urlOrPath) }

        AlertDialog(
            onDismissRequest = { playlistToEdit = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kaynağı Düzenle",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Oynatma listesi adını veya bağlantı URL'sini güncelleyin:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Liste Adı") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_playlist_name_input")
                    )

                    if (!targetPlaylist.isLocalFile) {
                        OutlinedTextField(
                            value = editedUrl,
                            onValueChange = { editedUrl = it },
                            label = { Text("M3U / Xtream URL") },
                            singleLine = false,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = outlinedColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_playlist_url_input")
                        )
                    }

                    Text(
                        text = if (targetPlaylist.isLocalFile)
                            "ℹ️ Yerel M3U dosyalarının adı düzenlenebilir. İçeriği güncellemek için yeni dosya seçebilirsiniz."
                        else
                            "ℹ️ URL adresi güncellendiğinde veya kaydedildiğinde kanallar otomatik olarak yeniden çekilir.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdatePlaylist(targetPlaylist.id, editedName, editedUrl)
                        playlistToEdit = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("save_edit_playlist_button")
                ) {
                    Text("Kaydet & Güncelle", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToEdit = null }) {
                    Text("İptal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun AccountStatusCard(
    accountInfo: AccountInfo?,
    playlistCount: Int
) {
    val isExpired = accountInfo?.isExpired == true
    val statusBg = if (isExpired) Color(0xFFB71C1C).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    val statusBorder = if (isExpired) Color(0xFFE53935) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = statusBg,
        border = BorderStroke(1.dp, statusBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header row with Icon and Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (isExpired) Color(0xFFE53935).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isExpired) Icons.Default.Warning else Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (isExpired) Color(0xFFEF5350) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "IPTV Hesap & Süre Durumu",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (accountInfo != null) "Bağlı Hesap: ${accountInfo.username}" else if (playlistCount > 0) "Yerel Liste Yüklü ($playlistCount liste)" else "Hesap Bağlı Değil",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                // Live Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (accountInfo != null && !isExpired) Color(0xFF2E7D32).copy(alpha = 0.3f) else if (isExpired) Color(0xFFC62828).copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = if (accountInfo != null && !isExpired) "AKTİF" else if (isExpired) "SÜRESİ BİTTİ" else "BEKLENİYOR",
                        color = if (accountInfo != null && !isExpired) Color(0xFF81C784) else if (isExpired) Color(0xFFFF8A80) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Remaining Time Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "KALAN ABONELİK SÜRESİ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = accountInfo?.remainingTimeText ?: (if (playlistCount > 0) "Süresiz / Yerel Liste" else "Henüz Liste Eklenmedi"),
                        color = if (isExpired) Color(0xFFFF8A80) else MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (accountInfo?.expirationDateText != null) {
                        Text(
                            text = "Bitiş Tarihi: ${accountInfo.expirationDateText}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Sub details if Xtream / M3U account info exists
            if (accountInfo != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AccountMiniStat(label = "Eşzamanlı Bağlantı", value = "${accountInfo.activeConnections ?: "1"} / ${accountInfo.maxConnections ?: "1"}")
                    AccountMiniStat(label = "Durum", value = accountInfo.status.replaceFirstChar { it.uppercase() })
                    if (!accountInfo.serverHost.isNullOrBlank()) {
                        AccountMiniStat(label = "Sunucu", value = accountInfo.serverHost.substringAfter("://").substringBefore(':').take(14))
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountMiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (titleColor == StreamFlowLiveRed) StreamFlowLiveRed else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = titleColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
)
