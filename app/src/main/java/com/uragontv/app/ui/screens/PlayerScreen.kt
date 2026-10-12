package com.uragontv.app.ui.screens

import android.app.Activity
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.uragontv.app.data.model.AspectRatioMode
import com.uragontv.app.data.model.Channel
import com.uragontv.app.data.repository.StreamSourceRepository
import com.uragontv.app.ui.components.TvActionButton
import com.uragontv.app.ui.theme.DarkSurface
import com.uragontv.app.ui.theme.DarkSurfaceBorder
import com.uragontv.app.ui.theme.DarkSurfaceElevated
import com.uragontv.app.ui.theme.LiveRed
import com.uragontv.app.ui.theme.MetallicGold
import com.uragontv.app.ui.theme.ObsidianBlack
import com.uragontv.app.ui.theme.SoftAntiqueGold
import com.uragontv.app.ui.theme.TextMuted
import com.uragontv.app.ui.theme.TextPrimary
import com.uragontv.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    channel: Channel,
    repository: StreamSourceRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allChannels by repository.channels.collectAsState()
    val savedAspectRatio by repository.aspectRatio.collectAsState()
    val autoReconnect by repository.autoReconnect.collectAsState()

    var currentChannel by remember(channel) { mutableStateOf(channel) }
    var aspectRatioMode by remember { mutableStateOf(savedAspectRatio) }
    var isOsdVisible by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var retryCount by remember { mutableIntStateOf(0) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Keep screen on during TV playback
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Auto-hide OSD after 5 seconds of inactivity
    LaunchedEffect(isOsdVisible, lastInteractionTime) {
        if (isOsdVisible) {
            delay(5000)
            isOsdVisible = false
        }
    }

    // Record watched channel
    LaunchedEffect(currentChannel) {
        repository.recordWatchedChannel(currentChannel)
    }

    BackHandler {
        if (isOsdVisible) {
            isOsdVisible = false
        } else {
            onBack()
        }
    }

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build().apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
            }
    }

    // Player event listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    isError = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isError = true
                errorMessage = error.localizedMessage ?: "Stream connection error"
                if (autoReconnect && retryCount < 4) {
                    retryCount++
                    // Attempt retry after 3 seconds
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Helper to build media source depending on URL type
    fun prepareStream(targetChannel: Channel) {
        isBuffering = true
        isError = false
        retryCount = 0

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(8000)
            .setReadTimeoutMs(8000)
            .setUserAgent("UragonTv/1.0 (Android TV ExoPlayer)")

        val uri = Uri.parse(targetChannel.streamUrl)
        val mediaItem = MediaItem.fromUri(uri)

        val mediaSource: MediaSource = when {
            targetChannel.streamUrl.contains(".m3u8", ignoreCase = true) -> {
                HlsMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItem)
            }
            targetChannel.streamUrl.contains(".mpd", ignoreCase = true) -> {
                DashMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItem)
            }
            else -> {
                ProgressiveMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItem)
            }
        }

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    // Load stream when channel changes or retry is triggered
    LaunchedEffect(currentChannel, retryCount) {
        prepareStream(currentChannel)
    }

    val playerFocusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(playerFocusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    lastInteractionTime = System.currentTimeMillis()
                    when (keyEvent.key) {
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                            isOsdVisible = !isOsdVisible
                            true
                        }
                        Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight -> {
                            if (!isOsdVisible) {
                                isOsdVisible = true
                                true
                            } else false
                        }
                        Key.Back -> {
                            if (isOsdVisible) {
                                isOsdVisible = false
                                true
                            } else {
                                onBack()
                                true
                            }
                        }
                        else -> false
                    }
                } else false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                lastInteractionTime = System.currentTimeMillis()
                isOsdVisible = !isOsdVisible
            }
    ) {
        // Video Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Custom OSD used instead
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    resizeMode = when (aspectRatioMode) {
                        AspectRatioMode.FIT_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        AspectRatioMode.ZOOM_FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                }
            },
            update = { playerView ->
                playerView.resizeMode = when (aspectRatioMode) {
                    AspectRatioMode.FIT_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    AspectRatioMode.ZOOM_FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    AspectRatioMode.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (isBuffering && !isError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MetallicGold,
                        modifier = Modifier.size(52.dp),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Buffering ${currentChannel.name}…",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Stream Error Overlay with Auto-Reconnect
        if (isError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBlack.copy(alpha = 0.88f))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(DarkSurfaceElevated, RoundedCornerShape(16.dp))
                        .border(1.dp, LiveRed, RoundedCornerShape(16.dp))
                        .padding(28.dp)
                ) {
                    Text(
                        text = "Live Stream Offline or Buffering Drop",
                        color = LiveRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage.ifEmpty { "Unable to connect to live stream source." },
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TvActionButton(
                            text = "RECONNECT",
                            icon = Icons.Filled.Refresh,
                            isPrimary = true,
                            onClick = {
                                isError = false
                                retryCount++
                            },
                            testTag = "btn_retry_stream"
                        )

                        TvActionButton(
                            text = "SWITCH CHANNEL",
                            icon = Icons.Filled.Tv,
                            isPrimary = false,
                            onClick = { isOsdVisible = true },
                            testTag = "btn_switch_channel"
                        )
                    }
                }
            }
        }

        // ON-SCREEN DISPLAY (OSD) OVERLAY
        AnimatedVisibility(
            visible = isOsdVisible,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                ObsidianBlack.copy(alpha = 0.85f),
                                Color.Transparent,
                                ObsidianBlack.copy(alpha = 0.95f)
                            )
                        )
                    )
            ) {
                // Top Header OSD Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back & Live Status Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkSurface)
                                .border(1.dp, MetallicGold, CircleShape)
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Exit Player",
                                tint = SoftAntiqueGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(LiveRed, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentChannel.name,
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${currentChannel.category} • ${currentChannel.region} • ${currentChannel.streamType}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Resolution & Bitrate Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
                                .border(1.dp, MetallicGold.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentChannel.resolution,
                                color = SoftAntiqueGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Adaptive HLS",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Bottom OSD Bar: Quick Actions + Quick Channels Carousel
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    // Quick Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Action buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TvActionButton(
                                text = if (currentChannel.isFavorite) "Favorited" else "Add to Favorites",
                                icon = if (currentChannel.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                isPrimary = currentChannel.isFavorite,
                                onClick = {
                                    repository.toggleFavorite(currentChannel.id)
                                    currentChannel = currentChannel.copy(isFavorite = !currentChannel.isFavorite)
                                },
                                testTag = "player_fav_btn"
                            )

                            TvActionButton(
                                text = aspectRatioMode.label,
                                icon = Icons.Filled.AspectRatio,
                                isPrimary = false,
                                onClick = {
                                    aspectRatioMode = when (aspectRatioMode) {
                                        AspectRatioMode.FIT_16_9 -> AspectRatioMode.ZOOM_FILL
                                        AspectRatioMode.ZOOM_FILL -> AspectRatioMode.STRETCH
                                        AspectRatioMode.STRETCH -> AspectRatioMode.FIT_16_9
                                    }
                                    repository.setAspectRatio(aspectRatioMode)
                                },
                                testTag = "player_aspect_btn"
                            )

                            TvActionButton(
                                text = "Reload",
                                icon = Icons.Filled.Refresh,
                                isPrimary = false,
                                onClick = {
                                    prepareStream(currentChannel)
                                },
                                testTag = "player_reload_btn"
                            )
                        }

                        Text(
                            text = "D-Pad Up/Down to toggle OSD • Left/Right to browse channels",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Channel Picker (Horizontally scrollable mini-channel list)
                    Text(
                        text = "Quick Channel Switcher",
                        color = SoftAntiqueGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 6.dp)
                    ) {
                        items(allChannels, key = { it.id }) { ch ->
                            QuickChannelPill(
                                channel = ch,
                                isCurrent = ch.id == currentChannel.id,
                                onSelect = {
                                    currentChannel = ch
                                    lastInteractionTime = System.currentTimeMillis()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickChannelPill(
    channel: Channel,
    isCurrent: Boolean,
    onSelect: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .width(170.dp)
            .height(48.dp)
            .clip(shape)
            .background(if (isCurrent) DarkSurfaceElevated else DarkSurface)
            .border(
                width = if (isFocused) 2.dp else if (isCurrent) 1.5.dp else 1.dp,
                color = if (isFocused) MetallicGold else if (isCurrent) SoftAntiqueGold.copy(alpha = 0.7f) else DarkSurfaceBorder,
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .onKeyEvent { event ->
                if (event.key == Key.DirectionCenter || event.key == Key.Enter || event.key == Key.NumPadEnter) {
                    onSelect()
                    true
                } else false
            }
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (channel.logoUrl.isNotBlank()) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Tv,
                    contentDescription = null,
                    tint = SoftAntiqueGold,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = channel.name,
                    color = if (isFocused) SoftAntiqueGold else TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = channel.category,
                    color = TextMuted,
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
        }
    }
}
