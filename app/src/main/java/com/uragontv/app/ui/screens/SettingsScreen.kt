package com.uragontv.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uragontv.app.data.model.AspectRatioMode
import com.uragontv.app.data.repository.StreamSourceRepository
import com.uragontv.app.ui.components.TvFilterPill
import com.uragontv.app.ui.components.VirtualTvKeyboard
import com.uragontv.app.ui.theme.DarkSurface
import com.uragontv.app.ui.theme.DarkSurfaceBorder
import com.uragontv.app.ui.theme.DarkSurfaceElevated
import com.uragontv.app.ui.theme.MetallicGold
import com.uragontv.app.ui.theme.ObsidianBlack
import com.uragontv.app.ui.theme.SoftAntiqueGold
import com.uragontv.app.ui.theme.TextMuted
import com.uragontv.app.ui.theme.TextPrimary
import com.uragontv.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    repository: StreamSourceRepository,
    modifier: Modifier = Modifier
) {
    val customUrl by repository.customPlaylistUrl.collectAsState()
    val aspectRatio by repository.aspectRatio.collectAsState()
    val autoReconnect by repository.autoReconnect.collectAsState()
    val hwAcceleration by repository.hwAcceleration.collectAsState()

    var urlInput by remember(customUrl) { mutableStateOf(customUrl) }
    var showUrlKeyboard by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "UragonTv Settings & Source Provider",
                color = SoftAntiqueGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure dynamic M3U sources, streaming engine, and display preferences",
                color = TextMuted,
                fontSize = 13.sp
            )
        }

        // Section 1: Dynamic M3U Source Injector
        SettingsCard(title = "Dynamic Stream Source", icon = Icons.Filled.CloudDownload) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Current Source: " + if (customUrl.isBlank()) "Default Built-In Channels (Bicol & PH Live)" else customUrl,
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                // Input Box / Keyboard toggle
                val boxShape = RoundedCornerShape(8.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(boxShape)
                        .background(DarkSurface)
                        .border(1.dp, if (showUrlKeyboard) MetallicGold else DarkSurfaceBorder, boxShape)
                        .clickable { showUrlKeyboard = !showUrlKeyboard }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = if (urlInput.isBlank()) "Click to edit remote M3U URL…" else urlInput,
                        color = if (urlInput.isBlank()) TextMuted else TextPrimary,
                        fontSize = 13.sp
                    )
                }

                if (showUrlKeyboard) {
                    Text(
                        text = "Use on-screen keyboard to enter URL or select preset below:",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    VirtualTvKeyboard(
                        onCharTyped = { urlInput += it },
                        onBackspace = { if (urlInput.isNotEmpty()) urlInput = urlInput.dropLast(1) },
                        onClear = { urlInput = "" },
                        onSpace = { urlInput += " " }
                    )
                }

                // Preset & Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsActionButton(
                        text = "Apply URL",
                        icon = Icons.Filled.Check,
                        onClick = {
                            repository.setCustomPlaylistUrl(urlInput)
                            showUrlKeyboard = false
                        }
                    )

                    SettingsActionButton(
                        text = "Reset to Default M3U",
                        icon = Icons.Filled.Refresh,
                        onClick = {
                            urlInput = ""
                            repository.setCustomPlaylistUrl("")
                            showUrlKeyboard = false
                        }
                    )
                }
            }
        }

        // Section 2: Video Playback & Aspect Ratio
        SettingsCard(title = "Video Engine & Display", icon = Icons.Filled.AspectRatio) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Default Aspect Ratio:",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AspectRatioMode.entries.forEach { mode ->
                        TvFilterPill(
                            title = mode.label,
                            isSelected = aspectRatio == mode,
                            onClick = { repository.setAspectRatio(mode) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Toggle 1: Auto Reconnect
                SettingsToggleRow(
                    title = "Automatic Stream Reconnect",
                    subtitle = "Instantly retries connecting when live buffering stalls or network drops",
                    checked = autoReconnect,
                    onCheckedChange = { repository.setAutoReconnect(it) }
                )

                // Toggle 2: Hardware Acceleration
                SettingsToggleRow(
                    title = "Hardware Accelerated Codecs",
                    subtitle = "Use TV hardware decoders for smooth 60fps 1080p and 4K playback",
                    checked = hwAcceleration,
                    onCheckedChange = { repository.setHwAcceleration(it) }
                )
            }
        }

        // Section 3: About UragonTv
        SettingsCard(title = "About Platform", icon = Icons.Filled.Info) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "UragonTv Smart TV Live Broadcast Platform",
                    color = SoftAntiqueGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Version: 1.0.0-PRO (Android TV OS / Google TV Compatible)\n" +
                            "Engine: androidx.media3 (ExoPlayer HLS & DASH)\n" +
                            "Input: TV D-Pad Remote Navigation with Active Glow Focus\n" +
                            "Designed for regional Bicolano, national Philippine, and global audiences.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, shape)
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SoftAntiqueGold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        content()
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isFocused) DarkSurface else Color.Transparent)
            .border(1.dp, if (isFocused) MetallicGold else Color.Transparent, shape)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .onKeyEvent { event ->
                if (event.key == Key.DirectionCenter || event.key == Key.Enter || event.key == Key.NumPadEnter) {
                    onCheckedChange(!checked)
                    true
                } else false
            }
            .clickable { onCheckedChange(!checked) }
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.82f)) {
            Text(
                text = title,
                color = if (isFocused) SoftAntiqueGold else TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBlack,
                checkedTrackColor = MetallicGold,
                uncheckedTrackColor = DarkSurfaceBorder
            )
        )
    }
}

@Composable
private fun SettingsActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "act_scale"
    )
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(shape)
            .background(if (isFocused) MetallicGold else DarkSurface)
            .border(1.dp, if (isFocused) SoftAntiqueGold else DarkSurfaceBorder, shape)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .onKeyEvent { event ->
                if (event.key == Key.DirectionCenter || event.key == Key.Enter || event.key == Key.NumPadEnter) {
                    onClick()
                    true
                } else false
            }
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused) ObsidianBlack else SoftAntiqueGold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = if (isFocused) ObsidianBlack else TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
