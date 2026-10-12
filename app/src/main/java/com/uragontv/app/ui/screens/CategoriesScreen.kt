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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uragontv.app.data.model.Channel
import com.uragontv.app.data.repository.StreamSourceRepository
import com.uragontv.app.ui.components.TvChannelCard
import com.uragontv.app.ui.components.TvFilterPill
import com.uragontv.app.ui.theme.DarkSurface
import com.uragontv.app.ui.theme.DarkSurfaceBorder
import com.uragontv.app.ui.theme.DarkSurfaceElevated
import com.uragontv.app.ui.theme.MetallicGold
import com.uragontv.app.ui.theme.ObsidianBlack
import com.uragontv.app.ui.theme.SoftAntiqueGold
import com.uragontv.app.ui.theme.TextMuted
import com.uragontv.app.ui.theme.TextPrimary

@Composable
fun CategoriesScreen(
    repository: StreamSourceRepository,
    onPlayChannel: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    val channels by repository.channels.collectAsState()
    val categories = remember(channels) {
        listOf("All Channels") + channels.map { it.category }.distinct()
    }

    var selectedCategory by remember { mutableStateOf("All Channels") }

    val filteredChannels = remember(channels, selectedCategory) {
        if (selectedCategory == "All Channels") {
            channels
        } else {
            channels.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Channel Categories",
                    color = SoftAntiqueGold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select a genre to filter national, local, and premium live broadcasts",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
            Text(
                text = "${filteredChannels.size} Channels",
                color = TextMuted,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Category Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(categories) { category ->
                TvFilterPill(
                    title = category,
                    isSelected = selectedCategory == category,
                    onClick = { selectedCategory = category }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Channel Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 190.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            items(filteredChannels, key = { it.id }) { channel ->
                TvChannelCard(
                    channel = channel,
                    onChannelClick = onPlayChannel,
                    onToggleFavorite = { repository.toggleFavorite(it.id) }
                )
            }
        }
    }
}
