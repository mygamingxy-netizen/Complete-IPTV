package com.uragontv.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uragontv.app.data.model.Channel
import com.uragontv.app.data.repository.StreamSourceRepository
import com.uragontv.app.ui.components.ChannelRow
import com.uragontv.app.ui.components.FeaturedHeroBanner
import com.uragontv.app.ui.theme.MetallicGold
import com.uragontv.app.ui.theme.ObsidianBlack

@Composable
fun HomeScreen(
    repository: StreamSourceRepository,
    onPlayChannel: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    val channels by repository.channels.collectAsState()
    val recentlyPlayed by repository.recentlyPlayed.collectAsState()
    val isLoading by repository.isLoading.collectAsState()

    var highlightedChannel by remember(channels) {
        mutableStateOf(channels.firstOrNull())
    }

    val categorized = remember(channels) {
        repository.getCategorizedChannels()
    }

    if (isLoading && channels.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ObsidianBlack),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MetallicGold)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top Featured Hero Banner
        FeaturedHeroBanner(
            channel = highlightedChannel ?: channels.firstOrNull(),
            onPlayChannel = onPlayChannel,
            onToggleFavorite = { repository.toggleFavorite(it.id) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Recently Played row (if available)
        if (recentlyPlayed.isNotEmpty()) {
            ChannelRow(
                title = "Recently Viewed",
                channels = recentlyPlayed,
                onChannelClick = onPlayChannel,
                onChannelFocused = { highlightedChannel = it },
                onToggleFavorite = { repository.toggleFavorite(it.id) }
            )
        }

        // Distinct category rows
        categorized.forEach { category ->
            ChannelRow(
                title = category.title,
                channels = category.channels,
                onChannelClick = onPlayChannel,
                onChannelFocused = { highlightedChannel = it },
                onToggleFavorite = { repository.toggleFavorite(it.id) }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
