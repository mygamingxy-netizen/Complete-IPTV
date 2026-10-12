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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uragontv.app.ui.theme.DarkSurface
import com.uragontv.app.ui.theme.DarkSurfaceBorder
import com.uragontv.app.ui.theme.MetallicGold
import com.uragontv.app.ui.theme.ObsidianBlack
import com.uragontv.app.ui.theme.SoftAntiqueGold
import com.uragontv.app.ui.theme.TextPrimary

@Composable
fun VirtualTvKeyboard(
    onCharTyped: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSpace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("A", "B", "C", "D", "E", "F", "1", "2", "3"),
        listOf("G", "H", "I", "J", "K", "L", "4", "5", "6"),
        listOf("M", "N", "O", "P", "Q", "R", "7", "8", "9"),
        listOf("S", "T", "U", "V", "W", "X", "Y", "Z", "0")
    )

    Column(
        modifier = modifier.padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { charKey ->
                    KeyButton(
                        text = charKey,
                        width = 38.dp,
                        onClick = { onCharTyped(charKey[0]) }
                    )
                }
            }
        }

        // Action Row: Space, Backspace, Clear
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeyButton(
                text = "SPACE",
                width = 110.dp,
                onClick = onSpace
            )
            KeyButton(
                text = "DEL ⌫",
                width = 80.dp,
                onClick = onBackspace
            )
            KeyButton(
                text = "CLEAR",
                width = 75.dp,
                onClick = onClear
            )
        }
    }
}

@Composable
private fun KeyButton(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.12f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "key_scale"
    )

    val shape = RoundedCornerShape(6.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .width(width)
            .height(34.dp)
            .scale(scale)
            .clip(shape)
            .background(if (isFocused) MetallicGold else DarkSurface)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) SoftAntiqueGold else DarkSurfaceBorder,
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
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isFocused) ObsidianBlack else TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
