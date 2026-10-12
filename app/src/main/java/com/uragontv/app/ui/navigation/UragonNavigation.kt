package com.uragontv.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.uragontv.app.data.model.Channel
import com.uragontv.app.data.repository.StreamSourceRepository
import com.uragontv.app.ui.components.NavDestination
import com.uragontv.app.ui.components.NavigationSidebar
import com.uragontv.app.ui.screens.CategoriesScreen
import com.uragontv.app.ui.screens.FavoritesScreen
import com.uragontv.app.ui.screens.HomeScreen
import com.uragontv.app.ui.screens.PlayerScreen
import com.uragontv.app.ui.screens.RegionsScreen
import com.uragontv.app.ui.screens.SearchScreen
import com.uragontv.app.ui.screens.SettingsScreen
import com.uragontv.app.ui.theme.ObsidianBlack

@Composable
fun UragonTvMain(
    repository: StreamSourceRepository,
    modifier: Modifier = Modifier
) {
    var selectedNav by remember { mutableStateOf(NavDestination.HOME) }
    var activePlayerChannel by remember { mutableStateOf<Channel?>(null) }

    // If video player is active, render full-bleed video screen
    if (activePlayerChannel != null) {
        PlayerScreen(
            channel = activePlayerChannel!!,
            repository = repository,
            onBack = { activePlayerChannel = null }
        )
    } else {
        // Main TV dashboard with collapsible sidebar on left and content on right
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(ObsidianBlack)
        ) {
            NavigationSidebar(
                selectedDestination = selectedNav,
                onNavigate = { selectedNav = it }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (selectedNav) {
                    NavDestination.HOME -> HomeScreen(
                        repository = repository,
                        onPlayChannel = { activePlayerChannel = it }
                    )
                    NavDestination.CATEGORIES -> CategoriesScreen(
                        repository = repository,
                        onPlayChannel = { activePlayerChannel = it }
                    )
                    NavDestination.REGIONS -> RegionsScreen(
                        repository = repository,
                        onPlayChannel = { activePlayerChannel = it }
                    )
                    NavDestination.FAVORITES -> FavoritesScreen(
                        repository = repository,
                        onPlayChannel = { activePlayerChannel = it }
                    )
                    NavDestination.SEARCH -> SearchScreen(
                        repository = repository,
                        onPlayChannel = { activePlayerChannel = it }
                    )
                    NavDestination.SETTINGS -> SettingsScreen(
                        repository = repository
                    )
                }
            }
        }
    }
}
