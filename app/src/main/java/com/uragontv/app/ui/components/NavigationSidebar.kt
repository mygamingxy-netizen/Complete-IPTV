package com.uragontv.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import com.uragontv.app.ui.theme.DarkSurface
import com.uragontv.app.ui.theme.DarkSurfaceElevated
import com.uragontv.app.ui.theme.MetallicGold
import com.uragontv.app.ui.theme.ObsidianBlack
import com.uragontv.app.ui.theme.SoftAntiqueGold
import com.uragontv.app.ui.theme.TextMuted
import com.uragontv.app.ui.theme.TextPrimary

enum class NavDestination(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    CATEGORIES("Categories", Icons.Filled.Category),
    REGIONS("Regions", Icons.Filled.Explore),
    FAVORITES("Favorites", Icons.Filled.Star),
    SEARCH("Search", Icons.Filled.Search),
    SETTINGS("Settings", Icons.Filled.Settings)
}

@Composable
fun NavigationSidebar(
    selectedDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSidebarFocused by remember { mutableStateOf(false) }

    val sidebarWidth by animateDpAsState(
        targetValue = if (isSidebarFocused) 200.dp else 72.dp,
        animationSpec = tween(durationMillis = 240),
        label = "sidebar_width"
    )

    Column(
        modifier = modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .background(DarkSurface.copy(alpha = 0.95f))
            .onFocusChanged { focusState ->
                isSidebarFocused = focusState.hasFocus
            }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = if (isSidebarFocused) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Logo & Branding Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (isSidebarFocused) Alignment.Start else Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .border(1.5.dp, MetallicGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LiveTv,
                        contentDescription = "UragonTv Logo",
                        tint = SoftAntiqueGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isSidebarFocused,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = "URAGON",
                            color = SoftAntiqueGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "TV PH",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation Items
            NavDestination.entries.forEach { destination ->
                val isSelected = selectedDestination == destination
                NavItemButton(
                    destination = destination,
                    isSelected = isSelected,
                    isExpanded = isSidebarFocused,
                    onClick = { onNavigate(destination) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Bottom status badge
        AnimatedVisibility(
            visible = isSidebarFocused,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(ObsidianBlack, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "v1.0.0 • D-Pad Mode",
                    color = TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun NavItemButton(
    destination: NavDestination,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isFocused -> MetallicGold
            isSelected -> DarkSurfaceElevated
            else -> Color.Transparent
        },
        label = "nav_item_bg"
    )

    val contentColor = when {
        isFocused -> ObsidianBlack
        isSelected -> SoftAntiqueGold
        else -> TextPrimary
    }

    val shape = RoundedCornerShape(10.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (isSelected && !isFocused) {
                    Modifier.border(1.dp, MetallicGold.copy(alpha = 0.5f), shape)
                } else Modifier
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
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.title,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = destination.title,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier
                    .padding(start = 14.dp)
                    .testTag("nav_label_${destination.name.lowercase()}")
            )
        }
    }
}
