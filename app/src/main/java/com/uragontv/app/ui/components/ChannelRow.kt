package com.uragontv.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uragontv.app.data.model.Channel
import com.uragontv.app.ui.theme.SoftAntiqueGold
import com.uragontv.app.ui.theme.TextMuted
import com.uragontv.app.ui.theme.TextPrimary

@Composable
fun ChannelRow(
    title: String,
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    onChannelFocused: ((Channel) -> Unit)? = null,
    onToggleFavorite: ((Channel) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (channels.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = SoftAntiqueGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
            Text(
                text = "${channels.size} channels",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
        ) {
            items(channels, key = { it.id }) { channel ->
                TvChannelCard(
                    channel = channel,
                    onChannelClick = onChannelClick,
                    onChannelFocused = onChannelFocused,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }
}
