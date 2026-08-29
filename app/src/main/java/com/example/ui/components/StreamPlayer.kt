@file:OptIn(
    androidx.media3.common.util.UnstableApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.ChannelItem
import com.example.ui.theme.StreamFlowError
import com.example.ui.theme.StreamFlowLiveRed
import com.example.ui.theme.StreamFlowOnPrimary
import com.example.ui.theme.StreamFlowOnSurface
import com.example.ui.theme.StreamFlowOnSurfaceVariant
import com.example.ui.theme.StreamFlowPrimary
import com.example.ui.theme.StreamFlowPrimaryContainer
import com.example.ui.theme.StreamFlowSurfaceContainer
import com.example.ui.theme.StreamFlowSurfaceContainerHigh
import com.example.ui.theme.StreamFlowSurfaceVariant
import com.example.ui.viewmodel.BufferOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

data class AudioTrackOption(val id: String, val label: String, val language: String, val isSelected: Boolean, val groupIndex: Int, val trackIndex: Int)
data class SubtitleTrackOption(val id: String, val label: String, val language: String, val isSelected: Boolean, val groupIndex: Int, val trackIndex: Int)

@Composable
fun StreamPlayer(
    channel: ChannelItem,
    allChannels: List<ChannelItem>,
    bufferOption: BufferOption = BufferOption.NORMAL,
    isInPipMode: Boolean = false,
    onEnterPip: () -> Unit = {},
    onProgressUpdate: (positionMs: Long, durationMs: Long) -> Unit = { _, _ -> },
    onClose: () -> Unit,
    onToggleFavorite: (ChannelItem) -> Unit,
    onSelectChannel: (ChannelItem) -> Unit,
    onNextChannel: () -> Unit,
    onPreviousChannel: () -> Unit,
    onDownloadChannel: ((ChannelItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val focusRequester = remember { FocusRequester() }

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showChannelListSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }

    // Gesture States (HUD overlays)
    var brightnessLevel by remember {
        val window = context.findActivity()?.window
        val cur = window?.attributes?.screenBrightness ?: -1f
        mutableFloatStateOf(if (cur in 0.0f..1.0f) cur else 0.5f)
    }
    var showBrightnessHud by remember { mutableStateOf(false) }

    var volumeLevel by remember {
        val currentVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 7
        val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        mutableFloatStateOf(currentVol.toFloat() / maxVol.coerceAtLeast(1).toFloat())
    }
    var showVolumeHud by remember { mutableStateOf(false) }

    // Double tap feedback
    var doubleTapSeekText by remember { mutableStateOf<String?>(null) }
    var doubleTapSeekSide by remember { mutableStateOf<String?>(null) } // "left" or "right"

    // Resume playback notice
    var showResumeNotice by remember { mutableStateOf(channel.playbackPositionMs > 10000L && channel.streamType != "LIVE") }

    // Advanced settings
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var sleepTimerMinutes by remember { mutableIntStateOf(0) }
    var remainingSleepSeconds by remember { mutableIntStateOf(0) }

    // Tracks
    var availableAudioTracks by remember { mutableStateOf<List<AudioTrackOption>>(emptyList()) }
    var availableSubtitles by remember { mutableStateOf<List<SubtitleTrackOption>>(emptyList()) }
    var isSubtitleEnabled by remember { mutableStateOf(false) }

    // Auto-hide controls timer
    LaunchedEffect(areControlsVisible, isPlaying) {
        if (areControlsVisible && isPlaying) {
            delay(4500)
            areControlsVisible = false
        }
    }

    // Sleep timer countdown
    LaunchedEffect(sleepTimerMinutes) {
        if (sleepTimerMinutes > 0) {
            remainingSleepSeconds = sleepTimerMinutes * 60
            while (remainingSleepSeconds > 0) {
                delay(1000)
                remainingSleepSeconds--
            }
            // Timer expired: stop playback and close
            onClose()
        } else {
            remainingSleepSeconds = 0
        }
    }

    // Initialize ExoPlayer
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    // Refresh available tracks from player
    fun refreshTracks() {
        val currentTracks = exoPlayer.currentTracks
        val audios = mutableListOf<AudioTrackOption>()
        val subs = mutableListOf<SubtitleTrackOption>()

        var aIdx = 0
        var sIdx = 0
        for (g in 0 until currentTracks.groups.size) {
            val group = currentTracks.groups[g]
            if (group.type == C.TRACK_TYPE_AUDIO) {
                for (t in 0 until group.length) {
                    val format = group.getTrackFormat(t)
                    val lang = format.language ?: "und"
                    val label = format.label ?: "Ses #${aIdx + 1} (${lang.uppercase()})"
                    audios.add(
                        AudioTrackOption(
                            id = "audio_${g}_$t",
                            label = label,
                            language = lang,
                            isSelected = group.isTrackSelected(t),
                            groupIndex = g,
                            trackIndex = t
                        )
                    )
                    aIdx++
                }
            } else if (group.type == C.TRACK_TYPE_TEXT) {
                for (t in 0 until group.length) {
                    val format = group.getTrackFormat(t)
                    val lang = format.language ?: "und"
                    val label = format.label ?: "Altyazı #${sIdx + 1} (${lang.uppercase()})"
                    val isSelected = group.isTrackSelected(t)
                    if (isSelected) isSubtitleEnabled = true
                    subs.add(
                        SubtitleTrackOption(
                            id = "sub_${g}_$t",
                            label = label,
                            language = lang,
                            isSelected = isSelected,
                            groupIndex = g,
                            trackIndex = t
                        )
                    )
                    sIdx++
                }
            }
        }
        availableAudioTracks = audios
        availableSubtitles = subs
    }

    // Handle Channel Stream Loading & Resume Seek
    LaunchedEffect(channel.streamUrl) {
        playbackError = null
        isBuffering = true
        showResumeNotice = channel.playbackPositionMs > 10000L && channel.streamType != "LIVE"
        try {
            val mediaItem = MediaItem.fromUri(Uri.parse(channel.streamUrl))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()

            // Seek to saved position if VOD
            if (channel.playbackPositionMs > 5000L && channel.streamType != "LIVE") {
                exoPlayer.seekTo(channel.playbackPositionMs)
            }
        } catch (e: Exception) {
            playbackError = "Geçersiz yayın adresi: ${e.message}"
        }
    }

    // Player event listener & Screen wake-lock (keep screen on during playback)
    DisposableEffect(exoPlayer) {
        val activity = context.findActivity()
        try {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (_: Exception) {}

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                        playbackError = null
                    }
                    Player.STATE_READY -> {
                        isBuffering = false
                        playbackError = null
                        duration = exoPlayer.duration.coerceAtLeast(0L)
                        refreshTracks()
                    }
                    Player.STATE_ENDED -> {
                        isBuffering = false
                        onProgressUpdate(duration, duration)
                    }
                    Player.STATE_IDLE -> {
                        isBuffering = false
                    }
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                refreshTracks()
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                val cause = error.localizedMessage ?: "Yayın kaynağı çevrimdışı"
                playbackError = "Yayın oynatma hatası ($cause)"
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            try {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } catch (_: Exception) {}
            try {
                val finalPos = exoPlayer.currentPosition.coerceAtLeast(0L)
                val finalDur = exoPlayer.duration.coerceAtLeast(0L)
                if (finalDur > 0L || finalPos > 0L) {
                    onProgressUpdate(finalPos, finalDur)
                }
            } catch (_: Exception) {}
            try {
                exoPlayer.removeListener(listener)
                exoPlayer.release()
            } catch (_: Exception) {}
            try {
                context.findActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } catch (_: Exception) {}
        }
    }

    // Progress update loop (updates UI and periodically saves progress)
    LaunchedEffect(isPlaying) {
        var counter = 0
        while (isPlaying) {
            try {
                currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
                duration = exoPlayer.duration.coerceAtLeast(0L)
                counter++
                if (counter % 3 == 0 && (duration > 0 || currentPosition > 0)) {
                    onProgressUpdate(currentPosition, duration)
                }
            } catch (_: Exception) {}
            delay(1000)
        }
    }

    // Request TV focus when mounted
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            // Android TV Remote Key Handling (DPAD navigation & remote keys)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp) {
                    when (keyEvent.key) {
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                            if (!areControlsVisible) {
                                areControlsVisible = true
                            } else {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                            true
                        }
                        Key.DirectionUp -> {
                            onPreviousChannel()
                            true
                        }
                        Key.DirectionDown -> {
                            onNextChannel()
                            true
                        }
                        Key.DirectionLeft -> {
                            val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            doubleTapSeekText = "-10 sn"
                            doubleTapSeekSide = "left"
                            areControlsVisible = true
                            true
                        }
                        Key.DirectionRight -> {
                            val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(if (duration > 0) duration else Long.MAX_VALUE)
                            exoPlayer.seekTo(newPos)
                            doubleTapSeekText = "+10 sn"
                            doubleTapSeekSide = "right"
                            areControlsVisible = true
                            true
                        }
                        Key.Back, Key.Escape -> {
                            if (showChannelListSheet) {
                                showChannelListSheet = false
                            } else if (showSettingsSheet) {
                                showSettingsSheet = false
                            } else if (areControlsVisible) {
                                areControlsVisible = false
                            } else {
                                onClose()
                            }
                            true
                        }
                        Key.MediaPlayPause -> {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            true
                        }
                        Key.MediaPlay -> {
                            exoPlayer.play()
                            true
                        }
                        Key.MediaPause -> {
                            exoPlayer.pause()
                            true
                        }
                        Key.Menu -> {
                            showSettingsSheet = true
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .testTag("stream_player_container")
    ) {
        // 1. ExoPlayer Video Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    keepScreenOn = true
                    this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.resizeMode = resizeMode
                playerView.keepScreenOn = true
            },
            modifier = Modifier.fillMaxSize()
        )

        // If in PiP mode, hide all overlays for clean miniature playback
        if (!isInPipMode) {
            // 2. Gesture Detectors Layer (Vertical Drag for Volume / Brightness, Double Tap for Seek, Single Tap for Controls)
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                areControlsVisible = !areControlsVisible
                            },
                            onDoubleTap = { offset ->
                                val isRightSide = offset.x > size.width / 2
                                if (isRightSide) {
                                    val target = (exoPlayer.currentPosition + 10000L).coerceAtMost(if (duration > 0) duration else Long.MAX_VALUE)
                                    exoPlayer.seekTo(target)
                                    doubleTapSeekText = "+10 sn"
                                    doubleTapSeekSide = "right"
                                } else {
                                    val target = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                                    exoPlayer.seekTo(target)
                                    doubleTapSeekText = "-10 sn"
                                    doubleTapSeekSide = "left"
                                }
                                coroutineScope.launch {
                                    delay(1200)
                                    doubleTapSeekText = null
                                    doubleTapSeekSide = null
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                val isLeftSide = offset.x < size.width / 2
                                if (isLeftSide) showBrightnessHud = true else showVolumeHud = true
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    delay(1500)
                                    showBrightnessHud = false
                                    showVolumeHud = false
                                }
                            },
                            onDragCancel = {
                                showBrightnessHud = false
                                showVolumeHud = false
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                val isLeftSide = change.position.x < size.width / 2
                                val delta = -dragAmount / 350f

                                if (isLeftSide) {
                                    // Brightness Gesture
                                    showBrightnessHud = true
                                    val act = context.findActivity()
                                    if (act != null) {
                                        val newBrightness = (brightnessLevel + delta).coerceIn(0.01f, 1.0f)
                                        brightnessLevel = newBrightness
                                        val lp = act.window.attributes
                                        lp.screenBrightness = newBrightness
                                        act.window.attributes = lp
                                    }
                                } else {
                                    // Volume Gesture
                                    showVolumeHud = true
                                    if (audioManager != null) {
                                        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                                        val newVolFraction = (volumeLevel + delta).coerceIn(0f, 1f)
                                        volumeLevel = newVolFraction
                                        val targetVolInt = (newVolFraction * maxVol).toInt()
                                        try {
                                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolInt, 0)
                                        } catch (_: Exception) {}
                                    }
                                }
                            }
                        )
                    }
            )

        // 3. Double-Tap Seek Ripples & Feedback Overlay
        if (doubleTapSeekText != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp),
                contentAlignment = if (doubleTapSeekSide == "right") Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, StreamFlowPrimary),
                    modifier = Modifier.size(90.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (doubleTapSeekSide == "right") Icons.Default.Forward10 else Icons.Default.Replay10,
                            contentDescription = null,
                            tint = StreamFlowPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = doubleTapSeekText ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 4. Brightness Gesture HUD
        if (showBrightnessHud) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.width(56.dp).padding(vertical = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                brightnessLevel > 0.65f -> Icons.Default.BrightnessHigh
                                brightnessLevel > 0.35f -> Icons.Default.BrightnessMedium
                                else -> Icons.Default.BrightnessLow
                            },
                            contentDescription = "Parlaklık",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "${(brightnessLevel * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(90.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((90 * brightnessLevel).dp)
                                    .align(Alignment.BottomCenter)
                                    .background(StreamFlowPrimary)
                            )
                        }
                    }
                }
            }
        }

        // 5. Volume Gesture HUD
        if (showVolumeHud) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.width(56.dp).padding(vertical = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                volumeLevel <= 0.01f -> Icons.Default.VolumeOff
                                volumeLevel > 0.65f -> Icons.Default.VolumeUp
                                else -> Icons.Default.VolumeDown
                            },
                            contentDescription = "Ses",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "${(volumeLevel * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(90.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((90 * volumeLevel).dp)
                                    .align(Alignment.BottomCenter)
                                    .background(StreamFlowPrimary)
                            )
                        }
                    }
                }
            }
        }

        // 6. Continue Watching Notice / Floating Resume Banner
        if (showResumeNotice && channel.playbackPositionMs > 10000L) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StreamFlowPrimary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 70.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        tint = StreamFlowPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Kaldığınız yerden devam ediliyor: ${formatTime(channel.playbackPositionMs)}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StreamFlowPrimaryContainer,
                        modifier = Modifier.clickable {
                            exoPlayer.seekTo(0)
                            showResumeNotice = false
                        }
                    ) {
                        Text(
                            text = "Baştan Başlat",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(
                        onClick = { showResumeNotice = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 7. Buffering Indicator
        if (isBuffering && playbackError == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = StreamFlowPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${channel.name} yükleniyor...",
                            color = StreamFlowOnSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 8. Error Message Overlay
        if (playbackError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Hata",
                        tint = StreamFlowError,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Yayın Başlatılamadı",
                        color = StreamFlowOnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Yayın kaynağı çevrimdışı olabilir veya internet bağlantısı kesilmiş olabilir.",
                        color = StreamFlowOnSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StreamFlowPrimaryContainer,
                            modifier = Modifier
                                .tvFocusable(shape = RoundedCornerShape(20.dp)) {
                                    playbackError = null
                                    isBuffering = true
                                    exoPlayer.prepare()
                                    exoPlayer.play()
                                }
                        ) {
                            Text(
                                text = "Tekrar Dene",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StreamFlowSurfaceContainerHigh,
                            modifier = Modifier
                                .tvFocusable(shape = RoundedCornerShape(20.dp)) { onNextChannel() }
                        ) {
                            Text(
                                text = "Sonraki Kanal",
                                color = StreamFlowOnSurface,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // 9. Full Glassmorphic Controls Overlay
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.88f)
                            )
                        )
                    )
            ) {
                // Top Info Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .tvFocusable(shape = CircleShape, onClick = onClose)
                                .testTag("player_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Live Badge
                                if (channel.streamType == "LIVE") {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = StreamFlowLiveRed.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, StreamFlowLiveRed.copy(alpha = 0.6f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(StreamFlowLiveRed, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "CANLI",
                                                color = StreamFlowLiveRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = StreamFlowPrimary.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, StreamFlowPrimary.copy(alpha = 0.6f))
                                    ) {
                                        Text(
                                            text = if (channel.streamType == "SERIES") "DİZİ" else "FİLM",
                                            color = StreamFlowPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Quality Badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = channel.quality,
                                        color = StreamFlowOnSurfaceVariant,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                // Sleep timer active indicator
                                if (remainingSleepSeconds > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFFFB300).copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.6f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bedtime,
                                                contentDescription = null,
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${remainingSleepSeconds / 60} dk",
                                                color = Color(0xFFFFB300),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = channel.name,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!channel.currentProgram.isNullOrBlank()) {
                                Text(
                                    text = "Şimdi: ${channel.currentProgram}",
                                    color = StreamFlowOnSurfaceVariant,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Top Right Action Buttons (PiP, Favorite, Settings)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // PiP (Picture-in-Picture) Button
                        IconButton(
                            onClick = onEnterPip,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .tvFocusable(shape = CircleShape, onClick = onEnterPip)
                                .testTag("player_pip_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "PiP Modu",
                                tint = Color.White
                            )
                        }

                        // Download Button (for Movies and Series)
                        if (channel.streamType != "LIVE" && !channel.streamUrl.startsWith("file://") && onDownloadChannel != null) {
                            IconButton(
                                onClick = { onDownloadChannel(channel) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                    .tvFocusable(shape = CircleShape) { onDownloadChannel(channel) }
                                    .testTag("player_download_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "İndir",
                                    tint = Color.White
                                )
                            }
                        }

                        // Favorite Button
                        IconButton(
                            onClick = { onToggleFavorite(channel) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .tvFocusable(shape = CircleShape) { onToggleFavorite(channel) }
                                .testTag("player_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favori",
                                tint = if (channel.isFavorite) StreamFlowLiveRed else Color.White
                            )
                        }

                        // Advanced Player Settings (Audio, Subtitles, Speed, Sleep Timer, Aspect Ratio)
                        IconButton(
                            onClick = { showSettingsSheet = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .tvFocusable(shape = CircleShape) { showSettingsSheet = true }
                                .testTag("player_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ayarlar",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Center Play / Pause Big Button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        modifier = Modifier
                            .size(76.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .tvFocusable(shape = CircleShape) {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                            .testTag("player_center_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }

                // Bottom Controls Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Seekable Timeline / Progress Slider
                    if (duration > 0L) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(currentPosition),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Slider(
                                value = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f),
                                onValueChange = { frac ->
                                    val targetMs = (frac * duration).toLong()
                                    exoPlayer.seekTo(targetMs)
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = StreamFlowPrimary,
                                    activeTrackColor = StreamFlowPrimary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatTime(duration),
                                color = StreamFlowOnSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // Live Stream Indicator Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Canlı Yayın",
                                color = StreamFlowOnSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.96f)
                                        .fillMaxSize()
                                        .background(StreamFlowLiveRed)
                                    )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CANLI",
                                color = StreamFlowLiveRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons Row (Glass Panel)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left actions: Skip prev, Replay 10, Play/Pause, Forward 10, Skip next
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onPreviousChannel,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .tvFocusable(shape = CircleShape, onClick = onPreviousChannel)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = "Önceki Kanal",
                                        tint = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                                        exoPlayer.seekTo(newPos)
                                    },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .tvFocusable(shape = CircleShape) {
                                            val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                                            exoPlayer.seekTo(newPos)
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Replay10,
                                        contentDescription = "10sn Geri",
                                        tint = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                    },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .tvFocusable(shape = CircleShape) {
                                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                        }
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Oynat / Duraklat",
                                        tint = StreamFlowPrimary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(if (duration > 0) duration else Long.MAX_VALUE)
                                        exoPlayer.seekTo(newPos)
                                    },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .tvFocusable(shape = CircleShape) {
                                            val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(if (duration > 0) duration else Long.MAX_VALUE)
                                            exoPlayer.seekTo(newPos)
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Forward10,
                                        contentDescription = "10sn İleri",
                                        tint = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = onNextChannel,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .tvFocusable(shape = CircleShape, onClick = onNextChannel)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Sonraki Kanal",
                                        tint = Color.White
                                    )
                                }
                            }

                            // Right actions: Aspect Ratio, Channel Switcher, Fullscreen
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Channels quick switcher button
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = StreamFlowPrimaryContainer.copy(alpha = 0.25f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StreamFlowPrimary.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .tvFocusable(shape = RoundedCornerShape(20.dp)) { showChannelListSheet = true }
                                        .clickable { showChannelListSheet = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FormatListBulleted,
                                            contentDescription = "Kanal Listesi",
                                            tint = StreamFlowPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Kanallar",
                                            color = StreamFlowPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Fullscreen orientation toggle
                                IconButton(
                                    onClick = {
                                        val activity = context.findActivity()
                                        if (activity != null) {
                                            isFullscreen = !isFullscreen
                                            activity.requestedOrientation = if (isFullscreen) {
                                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                            } else {
                                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .tvFocusable(shape = CircleShape) {
                                            val activity = context.findActivity()
                                            if (activity != null) {
                                                isFullscreen = !isFullscreen
                                                activity.requestedOrientation = if (isFullscreen) {
                                                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                                } else {
                                                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                                }
                                            }
                                        }
                                ) {
                                    Icon(
                                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = "Tam Ekran",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 10. Advanced Player Settings Modal Sheet (Audio, Subtitles, Speed, Sleep Timer, Aspect Ratio)
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                containerColor = StreamFlowSurfaceContainer,
                contentColor = StreamFlowOnSurface,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                var selectedTab by remember { mutableIntStateOf(0) }
                val tabs = listOf("Ses & Altyazı", "Hız & Boyut", "Uyku Zamanlayıcı")

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Oynatıcı Ayarları",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = StreamFlowOnSurface
                        )
                        IconButton(onClick = { showSettingsSheet = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = StreamFlowOnSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = StreamFlowPrimary
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedTab) {
                        0 -> {
                            // Ses ve Altyazı Seçenekleri
                            Text(
                                text = "Ses Dili / Parçası",
                                color = StreamFlowPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (availableAudioTracks.isEmpty()) {
                                Text(
                                    text = "Yalnızca varsayılan ses akışı mevcut.",
                                    color = StreamFlowOnSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    availableAudioTracks.forEach { track ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (track.isSelected) StreamFlowPrimaryContainer.copy(alpha = 0.25f) else StreamFlowSurfaceContainerHigh,
                                            border = if (track.isSelected) androidx.compose.foundation.BorderStroke(1.dp, StreamFlowPrimary) else null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    try {
                                                        val tracks = exoPlayer.currentTracks
                                                        if (track.groupIndex in 0 until tracks.groups.size) {
                                                            val group = tracks.groups[track.groupIndex]
                                                            if (track.trackIndex in 0 until group.length) {
                                                                val override = TrackSelectionOverride(group.mediaTrackGroup, listOf(track.trackIndex))
                                                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                                    .buildUpon()
                                                                    .setOverrideForType(override)
                                                                    .build()
                                                                refreshTracks()
                                                            }
                                                        }
                                                    } catch (_: Exception) {}
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Audiotrack,
                                                        contentDescription = null,
                                                        tint = if (track.isSelected) StreamFlowPrimary else StreamFlowOnSurfaceVariant,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = track.label,
                                                        color = if (track.isSelected) StreamFlowPrimary else StreamFlowOnSurface,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (track.isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                                if (track.isSelected) {
                                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = StreamFlowPrimary, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Altyazı",
                                color = StreamFlowPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (!isSubtitleEnabled) StreamFlowPrimaryContainer.copy(alpha = 0.25f) else StreamFlowSurfaceContainerHigh,
                                border = if (!isSubtitleEnabled) androidx.compose.foundation.BorderStroke(1.dp, StreamFlowPrimary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                            .buildUpon()
                                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                                            .build()
                                        isSubtitleEnabled = false
                                        refreshTracks()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Kapalı",
                                        color = if (!isSubtitleEnabled) StreamFlowPrimary else StreamFlowOnSurface,
                                        fontSize = 13.sp,
                                        fontWeight = if (!isSubtitleEnabled) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (!isSubtitleEnabled) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = StreamFlowPrimary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            availableSubtitles.forEach { sub ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (sub.isSelected) StreamFlowPrimaryContainer.copy(alpha = 0.25f) else StreamFlowSurfaceContainerHigh,
                                    border = if (sub.isSelected) androidx.compose.foundation.BorderStroke(1.dp, StreamFlowPrimary) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            try {
                                                val tracks = exoPlayer.currentTracks
                                                if (sub.groupIndex in 0 until tracks.groups.size) {
                                                    val group = tracks.groups[sub.groupIndex]
                                                    if (sub.trackIndex in 0 until group.length) {
                                                        val override = TrackSelectionOverride(group.mediaTrackGroup, listOf(sub.trackIndex))
                                                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                                            .buildUpon()
                                                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                                            .setOverrideForType(override)
                                                            .build()
                                                        isSubtitleEnabled = true
                                                        refreshTracks()
                                                    }
                                                }
                                            } catch (_: Exception) {}
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = sub.label,
                                            color = if (sub.isSelected) StreamFlowPrimary else StreamFlowOnSurface,
                                            fontSize = 13.sp,
                                            fontWeight = if (sub.isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (sub.isSelected) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = StreamFlowPrimary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Hız ve En Boy Oranı
                            Text(
                                text = "Oynatma Hızı",
                                color = StreamFlowPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(speeds) { speed ->
                                    val isSel = playbackSpeed == speed
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSel) StreamFlowPrimary else StreamFlowSurfaceContainerHigh,
                                        modifier = Modifier.clickable {
                                            playbackSpeed = speed
                                            exoPlayer.playbackParameters = PlaybackParameters(speed)
                                        }
                                    ) {
                                        Text(
                                            text = "${speed}x",
                                            color = if (isSel) Color.White else StreamFlowOnSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Ekran / En Boy Oranı",
                                color = StreamFlowPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val modes = listOf(
                                "Orijinal (Fit)" to AspectRatioFrameLayout.RESIZE_MODE_FIT,
                                "Doldur (Fill)" to AspectRatioFrameLayout.RESIZE_MODE_FILL,
                                "Yakınlaştır (Zoom)" to AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                                "Sabit Genişlik" to AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH,
                                "Sabit Yükseklik" to AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                modes.forEach { (name, mode) ->
                                    val isSel = resizeMode == mode
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSel) StreamFlowPrimaryContainer.copy(alpha = 0.25f) else StreamFlowSurfaceContainerHigh,
                                        border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, StreamFlowPrimary) else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { resizeMode = mode }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = name,
                                                color = if (isSel) StreamFlowPrimary else StreamFlowOnSurface,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (isSel) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = StreamFlowPrimary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Uyku Zamanlayıcısı
                            Text(
                                text = "Uyku Zamanlayıcısı",
                                color = StreamFlowPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Belirlediğiniz süre sonunda oynatıcı otomatik olarak durdurulur.",
                                color = StreamFlowOnSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val timerOptions = listOf(0 to "Kapalı", 15 to "15 Dakika", 30 to "30 Dakika", 45 to "45 Dakika", 60 to "60 Dakika (1 Saat)", 90 to "90 Dakika", 120 to "120 Dakika (2 Saat)")

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                timerOptions.forEach { (minutes, label) ->
                                    val isSel = sleepTimerMinutes == minutes
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSel) StreamFlowPrimaryContainer.copy(alpha = 0.25f) else StreamFlowSurfaceContainerHigh,
                                        border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, StreamFlowPrimary) else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { sleepTimerMinutes = minutes }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSel) StreamFlowPrimary else StreamFlowOnSurface,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (isSel) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = StreamFlowPrimary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // 11. Channels Quick Switcher Bottom Sheet
        if (showChannelListSheet) {
            ModalBottomSheet(
                onDismissRequest = { showChannelListSheet = false },
                containerColor = StreamFlowSurfaceContainer,
                contentColor = StreamFlowOnSurface,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                ) {
                    Text(
                        text = "Hızlı Kanal Değiştirici",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StreamFlowOnSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allChannels, key = { it.id }) { ch ->
                            val isSelected = ch.id == channel.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) StreamFlowPrimaryContainer.copy(alpha = 0.25f) else StreamFlowSurfaceContainerHigh,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, StreamFlowPrimary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .tvFocusable(shape = RoundedCornerShape(12.dp)) {
                                        onSelectChannel(ch)
                                        showChannelListSheet = false
                                    }
                                    .clickable {
                                        onSelectChannel(ch)
                                        showChannelListSheet = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ch.name,
                                            color = if (isSelected) StreamFlowPrimary else StreamFlowOnSurface,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${ch.groupTitle} • ${ch.quality}",
                                            color = StreamFlowOnSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(StreamFlowPrimary, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
