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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uragontv.app.data.model.Channel
import com.uragontv.app.ui.theme.DarkSurface
import com.uragontv.app.ui.theme.DarkSurfaceBorder
import com.uragontv.app.ui.theme.DarkSurfaceElevated
import com.uragontv.app.ui.theme.GoldGlow
import com.uragontv.app.ui.theme.LiveRed
import com.uragontv.app.ui.theme.MetallicGold
import com.uragontv.app.ui.theme.SoftAntiqueGold
import com.uragontv.app.ui.theme.TextMuted
import com.uragontv.app.ui.theme.TextPrimary

@Composable
fun TvChannelCard(
    channel: Channel,
    modifier: Modifier = Modifier,
    onChannelClick: (Channel) -> Unit,
    onChannelFocused: ((Channel) -> Unit)? = null,
    onToggleFavorite: ((Channel) -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "tv_card_scale"
    )

    val shape = RoundedCornerShape(12.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .width(200.dp)
            .height(130.dp)
            .scale(scale)
            .shadow(
                elevation = if (isFocused) 14.dp else 4.dp,
                shape = shape,
                ambientColor = if (isFocused) MetallicGold else Color.Black,
                spotColor = if (isFocused) MetallicGold else Color.Black
            )
            .clip(shape)
            .background(if (isFocused) DarkSurfaceElevated else DarkSurface)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) MetallicGold else DarkSurfaceBorder,
                shape = shape
            )
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    onChannelFocused?.invoke(channel)
                }
            }
            .focusable(interactionSource = interactionSource)
            .onKeyEvent { event ->
                if (event.key == Key.DirectionCenter || event.key == Key.Enter || event.key == Key.NumPadEnter) {
                    onChannelClick(channel)
                    true
                } else false
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onChannelClick(channel)
            }
            .testTag("channel_card_${channel.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Live badge + Favorite star
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live badge
                Box(
                    modifier = Modifier
                        .background(
                            color = LiveRed.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Favorite icon toggle
                if (channel.isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Bookmarked",
                        tint = SoftAntiqueGold,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onToggleFavorite?.invoke(channel) }
                    )
                } else if (isFocused) {
                    Icon(
                        imageVector = Icons.Outlined.StarOutline,
                        contentDescription = "Bookmark",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onToggleFavorite?.invoke(channel) }
                    )
                }
            }

            // Middle: Channel Logo / Centerpiece
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logoUrl.isNotBlank()) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Tv,
                        contentDescription = channel.name,
                        tint = if (isFocused) MetallicGold else TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Bottom: Channel Title & Category
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = channel.name,
                    color = if (isFocused) SoftAntiqueGold else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${channel.category} • ${channel.region}",
                    color = if (isFocused) TextPrimary else TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Focused glow accent bar at bottom
        if (isFocused) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, MetallicGold, SoftAntiqueGold, Color.Transparent)
                        )
                    )
            )
        }
    }
}
