package com.uragontv.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MetallicGold,
    onPrimary = ObsidianBlack,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = SoftAntiqueGold,
    secondary = SoftAntiqueGold,
    onSecondary = ObsidianBlack,
    tertiary = LiveRed,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceBorder,
    outlineVariant = MetallicGold
)

@Composable
fun UragonTvTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = UragonTvTypography,
        content = content
    )
}
