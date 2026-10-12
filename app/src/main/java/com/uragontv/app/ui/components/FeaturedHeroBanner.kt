package com.uragontv.app.ui.components

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uragontv.app.data.model.Channel
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

@Composable
fun FeaturedHeroBanner(
    channel: Channel?,
    onPlayChannel: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (channel == null) return

    val heroShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(heroShape)
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, heroShape)
    ) {
        // Background preview image with dark gradient overlay
        AsyncImage(
            model = channel.logoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(heroShape)
        )

        // Gradient Scrim (darkens image from left to right)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            ObsidianBlack.copy(alpha = 0.96f),
                            ObsidianBlack.copy(alpha = 0.82f),
                            ObsidianBlack.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Content on the left
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.70f)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Badges row
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Live indicator
                Box(
                    modifier = Modifier
                        .background(LiveRed, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE ON AIR",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Resolution badge
                Box(
                    modifier = Modifier
                        .background(DarkSurface, RoundedCornerShape(4.dp))
                        .border(0.8.dp, MetallicGold.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = channel.resolution,
                        color = SoftAntiqueGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Region Tag
                Box(
                    modifier = Modifier
                        .background(DarkSurface, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = channel.region,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // Channel Title & Category Description
            Column {
                Text(
                    text = channel.name,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Streaming live broadcast in ${channel.category}. Tap OK to watch fullscreen or browse rows below.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2
                )
            }

            // Quick Actions Buttons Row (TV D-Pad optimized)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvActionButton(
                    text = "WATCH LIVE",
                    icon = Icons.Filled.PlayArrow,
                    isPrimary = true,
                    onClick = { onPlayChannel(channel) },
                    testTag = "hero_watch_live"
                )

                TvActionButton(
                    text = if (channel.isFavorite) "BOOKMARKED" else "FAVORITE",
                    icon = if (channel.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    isPrimary = false,
                    onClick = { onToggleFavorite(channel) },
                    testTag = "hero_toggle_favorite"
                )
            }
        }
    }
}

@Composable
fun TvActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPrimary: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 180),
        label = "btn_scale"
    )

    val shape = RoundedCornerShape(8.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(
                when {
                    isFocused -> MetallicGold
                    isPrimary -> SoftAntiqueGold.copy(alpha = 0.9f)
                    else -> DarkSurface
                }
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) MetallicGold else DarkSurfaceBorder,
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(interactionSource = interactionSource)
            .onKeyEvent { event ->
                if (event.key == Key.DirectionCenter || event.key == Key.Enter || event.key == Key.NumPadEnter) {
                    onClick()
                    true
                } else false
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFocused || isPrimary) ObsidianBlack else TextPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = if (isFocused || isPrimary) ObsidianBlack else TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
