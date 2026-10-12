package com.uragontv.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uragontv.app.data.model.Channel
import com.uragontv.app.data.repository.StreamSourceRepository
import com.uragontv.app.ui.components.TvChannelCard
import com.uragontv.app.ui.components.VirtualTvKeyboard
import com.uragontv.app.ui.theme.DarkSurface
import com.uragontv.app.ui.theme.DarkSurfaceBorder
import com.uragontv.app.ui.theme.MetallicGold
import com.uragontv.app.ui.theme.ObsidianBlack
import com.uragontv.app.ui.theme.SoftAntiqueGold
import com.uragontv.app.ui.theme.TextMuted
import com.uragontv.app.ui.theme.TextPrimary

@Composable
fun SearchScreen(
    repository: StreamSourceRepository,
    onPlayChannel: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    val channels by repository.channels.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val searchResults = remember(channels, searchQuery) {
        if (searchQuery.isBlank()) {
            channels
        } else {
            val q = searchQuery.trim().lowercase()
            channels.filter { ch ->
                ch.name.lowercase().contains(q) ||
                        ch.category.lowercase().contains(q) ||
                        ch.region.lowercase().contains(q)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left Column: Search Bar & Virtual D-Pad Keyboard
        Column(
            modifier = Modifier
                .width(370.dp)
                .fillMaxHeight()
        ) {
            Text(
                text = "Channel Search",
                color = SoftAntiqueGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Query Display Box
            val boxShape = RoundedCornerShape(10.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(boxShape)
                    .background(DarkSurface)
                    .border(1.5.dp, if (searchQuery.isNotBlank()) MetallicGold else DarkSurfaceBorder, boxShape)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = if (searchQuery.isNotBlank()) SoftAntiqueGold else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "Use D-Pad keyboard below…" else searchQuery,
                        color = if (searchQuery.isEmpty()) TextMuted else TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.testTag("search_query_text")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // On-screen TV remote keyboard
            VirtualTvKeyboard(
                onCharTyped = { char ->
                    if (searchQuery.length < 30) {
                        searchQuery += char
                    }
                },
                onBackspace = {
                    if (searchQuery.isNotEmpty()) {
                        searchQuery = searchQuery.dropLast(1)
                    }
                },
                onClear = {
                    searchQuery = ""
                },
                onSpace = {
                    if (searchQuery.isNotEmpty() && !searchQuery.endsWith(" ")) {
                        searchQuery += " "
                    }
                }
            )
        }

        // Right Column: Filtered Results Grid
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "All Channels" else "Results for \"$searchQuery\"",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${searchResults.size} matches",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No channels match your query. Try searching for 'Bicol', 'News', 'Sports', or 'GMA'.",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 30.dp)
                ) {
                    items(searchResults, key = { it.id }) { channel ->
                        TvChannelCard(
                            channel = channel,
                            onChannelClick = onPlayChannel,
                            onToggleFavorite = { repository.toggleFavorite(it.id) }
                        )
                    }
                }
            }
        }
    }
}
